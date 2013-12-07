
package org.zerorm.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.Executable;
import org.zerorm.core.interfaces.MaybeHasAlias;
import org.zerorm.core.interfaces.MaybeHasParams;
import org.zerorm.core.interfaces.SimpleTable;

/**
 * Construct a query.
 * A Select object implements SimpleTable so that it can be used as a 
 * derived table.
 * @author bvan
 */
public class Select extends Executable implements SimpleTable<Select> {    
    
    protected class Scope {
        private LinkedHashMap<MaybeHasAlias, MaybeHasAlias> exportedMapping;

        protected Map<MaybeHasAlias, MaybeHasAlias> getMappings(){ return exportedMapping; }
        
        private Scope(){ exportedMapping = new LinkedHashMap<>(); }
        
        private List<MaybeHasAlias> getAvailableColumns(){
            List<MaybeHasAlias> embedded = new ArrayList<>( from.getColumns() );
            
            for(SimpleTable e: joinList.values()){
                embedded.addAll( e.getColumns() );
            }
            return embedded;
        }
    }

    public static class JoinExpr {
        private Join type;
        private Expr test;
        public JoinExpr(Join type, Expr test){
            this.type = type;
            this.test = test;
            test.setWrapped( true );
        }
        public Expr test() {
            return test;
        }
        public String op() {
            return type.op;
        }
        
    };
    
    protected static class Union extends Select {
        List<Select> unions = new ArrayList<>();

        protected Union(Select... selects){
            unions.addAll( Arrays.asList( selects) );
        }
        
        @Override
        public boolean hasParams(){
            for(Select s: unions){
                if(s.hasParams())
                    return true;
            }
            return false;
        }
        
        @Override
        public Select where(Expr... exprs){
            for(Select sel: unions){
                sel.where(exprs);
            }
            return this;
        }

        @Override
        public List<Param> getParams(){
            List<Param> list = new ArrayList<>();
            for(Select s: unions){
                list.addAll( s.getParams() );
            }
            return list;
        }

        @Override
        public List<MaybeHasAlias> getColumns(){
            LinkedHashMap<String, MaybeHasAlias> mapList = new LinkedHashMap<>();
            for(Select s: unions){
                if(mapList.isEmpty()){
                    for(MaybeHasAlias alias: s.getColumns()){
                        mapList.put( alias.canonical(), new Column(alias.canonical(), this));
                    }
                }
            }
            return new ArrayList<>(mapList.values());
        }

        @Override
        public String formatted(){
            return formatted(AbstractSQLFormatter.getDefault());
        }
        
        @Override
        public String formatted(AbstractSQLFormatter fmtr){
            StringBuilder sql = new StringBuilder();
            for(Iterator<Select> itr = unions.iterator(); itr.hasNext();){
                Select s = itr.next();
                sql.append( s.formatted( fmtr ) );
                if(itr.hasNext()){
                    sql.append( " UNION ALL ");
                }
            }
            return getWrapped() ? "(" + sql.toString() + " )" : sql.toString();
        }
    }

    //  The enum will hold a Expr for use when constructing JOIN statements
    enum Join {
        JOIN("JOIN"),
        RIGHT("RIGHT JOIN"),
        LEFT_OUTER("LEFT OUTER JOIN"),
        RIGHT_OUTER("RIGHT OUTER JOIN");
        
        private String op;

        private Join(String op) {
            this.op = op;
        }

        public JoinExpr with(Expr test) {
            return new JoinExpr(this, test);
        }
    };
    
    protected Scope scope = new Scope();
    private String alias = "";
    private boolean wrapped;
    
    private SimpleTable from;
    private LinkedHashMap<JoinExpr, SimpleTable> joinList = new LinkedHashMap<>();
    
    private Expr where = new Expr();
    private List<Column> groupBys = new ArrayList<>();
    private Expr having = new Expr();
    private List<Column> orderBys = new ArrayList<>();

    /**
     * Construct initial SELECT statement.
     */
    public Select() { }

    /**
     * Construct initial select statement with the columns to be selected from
     * Objects may be either a String or of type MaybeHasAlias
     * @param columns
     */
    public Select(MaybeHasAlias... columns) {
        checkSelections( Arrays.asList( columns ) );
    }
    
    public Select(List<? extends MaybeHasAlias> columns ) {
        checkSelections( columns );
    }

    /**
     * Append to SELECT clause
     * Objects may be either a String or of type MaybeHasAlias
     * @param columns
     * @return
     */
    public Select selection( MaybeHasAlias... columns ) {
        return checkSelections( Arrays.asList( columns ) );
    }
    
    public Select selection( List<? extends MaybeHasAlias> columns ) {
        return checkSelections( columns );
    }
    
    /**
     * returns a column / new column
     * @param column Name of Column
     * @return 
     */
    public Column getSelection(Column column) {
        return checkColumn(column);
    }
    
    /**
     * Shortened/Convenience function for getColumn
     * @param column Name of Column
     * @return 
     */
    public Column $(String column) {
        return getSelection(new Column(column, this));
    }
    
    /**
     * Returns any columns that have been defined to be
     * exported for this class, with this class as the parent
     * @return
     */
    @Override
    public List<MaybeHasAlias> getColumns() {
        List<MaybeHasAlias> exported = new ArrayList<>();
        for(MaybeHasAlias a: scope.exportedMapping.keySet()){
            exported.add( new Column(a.canonical(), this));
        }
        return exported;
    }
    
    /**
     * Returns any columns that have been added for selection.
     * This is called when formatting the columns for selection.
     * @return
     */
    public List<MaybeHasAlias> getSelections() {
        return new ArrayList<>(scope.exportedMapping.values());
    }

    public Select clearSelections(){
        scope.exportedMapping.clear();
        return this;
    }
    
    /**
     * Returns any columns that currently be selected, based off of 
     * the FROM table and any joins.
     * @param columns
     * @return
     */
    public List<MaybeHasAlias> getAvailableSelections() {
        return scope.getAvailableColumns();
    }

    /**
     * FROM
     * Add primary table for selection by table name
     * Does not support implicit joins (i.e. 'FROM table1, table2' )
     * @param primaryTable
     * @return this
     */
    public Select from(SimpleTable primaryTable) {
        if (primaryTable instanceof Select) {
            // TODO : Determine if this is really needed. In general, I think yes, as explicit 
            // is better than implict.
            if (primaryTable.alias().isEmpty()) {
                throw new RuntimeException("Sub-Selects must have an alias");
            }
            ((Select) primaryTable).setWrapped( true );
        }
        from = primaryTable;
        return this;
    }

    public Select from(String tableName) {
        return from(new Table(tableName));
    }
    
    public SimpleTable getFrom() {
        return from;
    }

    /**
     * JOIN a SimpleTable to this Select statement.
     * For columns in the test that reference a table name/alias, 
     * that name/alias must be present in this table.
     * @param table
     * @param test
     * @return
     */
    public Select join(SimpleTable table, Expr test) {
        return join(table, test, Join.JOIN); 
    }

    public Select rightJoin(SimpleTable table, Expr test) {
        return join(table, test, Join.RIGHT);
    }

    public Select leftOuterJoin(SimpleTable table, Expr test) {
        return join(table, test, Join.LEFT_OUTER);
    }

    public Select rightOuterJoin(SimpleTable table, Expr test) {
        return join(table, test, Join.RIGHT_OUTER);
    }
    
    public Select join(SimpleTable table, JoinExpr expr) {
        return join(table, expr.test, expr.type); 
    }
    
    /**
     * get list of JOIN expressions.
     * @return FROM SimpleTable
     */
    public LinkedHashMap<JoinExpr, SimpleTable> getJoins() {
        return joinList;
    }
    
    public void setJoins(LinkedHashMap<JoinExpr, SimpleTable> joinList) {
        this.joinList = joinList;
    }

    /**
     * WHERE statement.
     * @param predicates
     * @return 
     */
    public Select where(Expr... predicates){
        where = Expr.collapse( where, predicates );
        return this;
    }
    
    public Expr getWhere(){
        return this.where;
    }

    /**
     * GROUP BY statement
     * @param columns
     * @return 
     */
    public Select groupBy(Column... columns) {
        addAllIfNotNull(groupBys, columns);
        return this;
    }
    
    public List<? extends MaybeHasAlias> getGroupBys(){
        return this.groupBys;
    }

    /**
     * Having statement.
     * @param predicates
     * @return 
     */
    public Select having(Expr... predicates){
        having = Expr.collapse( having, predicates );
        return this;
    }
    
    public Expr getHaving(){
        return this.having;
    }
    
    private void addAllIfNotNull(List list, Object[] objects){
        if(objects.length > 1 || (objects.length == 1 && objects[0] != null)){
            list.addAll(Arrays.asList(objects));
        }
    }

    public Select orderBy(Column... columns) {
        addAllIfNotNull(orderBys, columns);
        return this;
    }
    
    public List<? extends MaybeHasAlias> getOrderBys(){
        return this.orderBys;
    }
    
    public static Select unionAll(Select... selects){
        return new Union(selects);
    }

    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
    }
    
    public boolean getWrapped() {
        return this.wrapped;
    }

    @Override
    public String formatted(AbstractSQLFormatter fmtr) {
        return fmtr.format( this );
    }

    @Override
    public List<Param> getParams() {
        List<Param> params = new ArrayList<>();

        if (from instanceof MaybeHasParams) {
            params.addAll( ((MaybeHasParams) from).getParams() );
        }

        for (Entry<JoinExpr, SimpleTable> e : joinList.entrySet()) {
            if (e.getValue() instanceof MaybeHasParams) {
                params.addAll( ((MaybeHasParams) e.getValue()).getParams() );
            }
            params.addAll( e.getKey().test().getParams() );
        }

        params.addAll( where.getParams() );
        params.addAll( having.getParams() );

        return params;
    }

    @Override
    public String alias() { return alias != null ? alias : ""; }

    @Override
    public Select as(String alias) {
        this.alias = alias;
        return this;
    }
    
    @Override
    public Select asExact(String alias){
        this.alias = '"' + alias + '"';
        return this;
    }
    
    @Override
    public String canonical(){
        return alias;
    }
    
    private Select join(SimpleTable table, Expr test, Join join) {

        if(table instanceof Select){
            ((Select) table).setWrapped( true );
        }
        // Add first, remove if bogus.
        joinList.put( join.with( test ), table);
        for (Object o : test.getValues()) {
            // If columns, verify all join tables are present
            if (!allTablesPresent(o)) {
                joinList.remove(join.with( test ));
                throw new RuntimeException("Unable to join table: Table not present" + 
                        ((SimpleTable) o).canonical() );
            }
        }
        return this;
    }

    private Select checkSelections(Collection<? extends MaybeHasAlias> columns){
        for(MaybeHasAlias c: columns){
            checkColumn(c);
        }
        return this;
    }
    
    private Column checkColumn(MaybeHasAlias c){
        if(c instanceof SimpleTable){
            return null;
        } else {
            Column neue = new Column( c.canonical(), this );
            for(MaybeHasAlias colx: scope.exportedMapping.keySet()){
                if(colx.canonical().equals( c.canonical())){
                    return colx instanceof Column ? (Column) colx : neue;
                }
            }
            scope.exportedMapping.put( neue, c );
            return neue;
        }
    }

    private boolean allTablesPresent(Object c){
        if(c instanceof Column){
            SimpleTable prnt = ((Column) c).getParent();
            return prnt != null ? getAllTableNames().contains( prnt.canonical() ) : true;
        }
        // Ok
        return true;
    }

    private List<String> getAllTableNames() {
        ArrayList<String> tables = new ArrayList<>();

        tables.add(!from.alias().isEmpty() ? from.alias() : ((Table) from).getTable() );

        for (SimpleTable e : joinList.values()) {
            tables.add( !e.alias().isEmpty() ? e.alias() : ((Table) e).getTable() );
        }
        return tables;
    }
}
