
package org.zerorm.core;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.MaybeHasAlias;
import org.zerorm.core.interfaces.Schema;
import org.zerorm.core.interfaces.SimpleTable;

/**
 * A class for defining a Table. Useful at runtime or at compile time.
 * Table(){} is finally called, either from this class or a subclass,
 * initAnnotatedColumns will look for columns defined in this table
 * annotated with {@link org.zerorm.core.interfaces.Schema}, and 
 * initialize any columns that are found with the canonical name of
 * that Column.
 * @author bvan
 */
public class Table implements SimpleTable<Table> {
    private String table;
    private String alias = "";
    
    // Column cache/list of columns defined for this table
    private Map<String,Column> columns = new LinkedHashMap<String,Column>(){
        // Override so columns returns the value just inserted
        @Override
        public Column put(String key, Column value){
            super.put(key,value);
            return value;
        }
    };
    
    public static class CTE extends Table {
        private Select definition;

        public CTE(){
            super();
        }

        public CTE(String table){
            super(table);
        }

        public CTE(String table, String alias){
            super( table, alias );
        }
        
        public void setExpression(Select stmt){
            stmt.setWrapped( true );
            this.definition = stmt;
        }
        
        public Select getExpression(){
            return this.definition;
        }
        
        @Override
        public List<Column> getColumns(){
            List<Column> derived = new ArrayList<>();
            List<? extends MaybeHasAlias> columns = 
                    super.getColumns().isEmpty() ? definition.getColumns() : super.getColumns();
            for(MaybeHasAlias alias: columns){
                derived.add( new Column(alias.canonical(), this));
            }
            return derived;
        }
    }

    public Table() {
        Schema tableInfo = getClass().getAnnotation( Schema.class );
        if(tableInfo != null && !( tableInfo.name().isEmpty() && tableInfo.alias().isEmpty())){
            this.table = tableInfo.name();
            this.alias = tableInfo.alias();
        } else {
            this.table = getClass().getSimpleName();
        }
        columns.putAll(getSchemaColumns( getClass(), this ));
    }
    
    public Table(String table){
        this();
        this.table = table;
    }
    
    public Table(String table, String alias){
        this(table);
        this.alias = alias;
    }

    /**
     * Get Table name
     * @return 
     */
    public String getTable(){
        return table;
    }
        
    /**
     * returns a column / new column
     * @param column Name of Column
     * @return 
     */
    public Column getColumn(String column) {
        return columns.get(column);
    }
    
    /**
     * returns a column or merges a new column
     * @param column Name of Column
     * @return 
     */
    public Column mergeColumn(String column) {
        return columns.containsKey(column)
                ? columns.get(column)
                : columns.put(column, new Column(column, this));
    }
    
    public Column mergeColumn(MaybeHasAlias column) {
        return columns.containsKey(column.canonical())
                ? columns.get(column.canonical())
                : columns.put(column.canonical(), new Column(column, this));
    }
    
    /**
     * Shortened/Convenience function for mergeColumn
     * @param column Name of Column
     * @return 
     */
    public Column $(String column) {
        return mergeColumn(column);
    }
    
    public Column $(String column, Class<?> javaType) {
        return mergeColumn(new Column(column, javaType, this));
    }
    
    /**
     * Convenience function to adopt and merge a column
     * @param column Name of Column
     * @return 
     */
    public Column $(MaybeHasAlias column) {
        return mergeColumn(column);
    }
    
    /**
     * Merges columns from the collection
     * @return 
     */
    public void addAllColumns(List<? extends MaybeHasAlias> columns) {
        for(MaybeHasAlias item: columns){
            mergeColumn(item);
        }
    }

    /**
     * returns a column / new column
     * @return 
     */
    @Override
    public List<Column> getColumns() {
        List<Column> cols = new ArrayList<>();
        for(Column c: columns.values()){
            if(!cols.contains( c )){
                cols.add( c );
            }
        }
        return cols;
    }
    
    /**
     * Construct Select statement with this table as the primary Table
     * @return new Select statement from this table
     */
    public Select select(){
        return new Select().from( this );
    }
    
    /**
     * Construct Select statement with this table as the primary Table.
     * @return new Select statement with all columns selected.
     */
    public Select selectAllColumns(){
        return select().selection( getColumns() );
    }

    /**
     * @param columns
     * @return new Select statement from this table with columns as the selection
     */
    public Select select(MaybeHasAlias... columns){
        return new Select( columns ).from( this );
    }
    
    /**
     * Construct Select statement with this table as the primary Table,
     * and all defined columns for the selection
     * @param exprs
     * @return new Select statement
     */
    public Select where(Expr... exprs){
        return new Select( getColumns() ).from( this ).where( exprs );
    }
    
    @Override
    public Table as(String alias){
        this.alias = alias;
        return this;
    }
    
    @Override
    public Table asExact(String alias){
        this.alias = '"' + alias + '"';
        return this;
    }
    
    public <T> T as(String alias, Class<T> clazz){
        this.alias = alias;
        return (T) this;
    }

    @Override
    public String alias() {
        return alias != null ? alias : "";
    }

    @Override
    public String formatted() {
        return formatted(AbstractSQLFormatter.getDefault());
    }
    
    @Override
    public String formatted(AbstractSQLFormatter fmtr) {
        return getTable();
    }

    @Override
    public String canonical(){
        return !alias().isEmpty() ? alias : table;
    }
    
    // Initiate annotated Columns
    public static Map<String, Column> getSchemaColumns(Class<?> clazz, SimpleTable table) {
        Map<String, Column> schemaColumns = new LinkedHashMap<>();
        // Initiate Columns for this class and all parents that are of type Table
        for(;Table.class.isAssignableFrom( clazz ); clazz = clazz.getSuperclass()){
            schemaColumns.putAll(getClassSchemaColumns(clazz, table));
        }
        return schemaColumns;
    }
    
    private static Map<String, Column> getClassSchemaColumns(Class<?> clazz, SimpleTable table){
        Map<String, Column> schemaColumns = new LinkedHashMap<>();
        for(Field field: clazz.getDeclaredFields()){
            if(field.getAnnotation( Schema.class ) == null){
                continue;
            }
            Schema schemaColumn = field.getAnnotation( Schema.class );
            String name = schemaColumn.name();
            name = name != null && !name.isEmpty() ? name : field.getName();
            try {
                Class type = Object.class;
                if(field.getGenericType() instanceof ParameterizedType){
                    ParameterizedType t = (ParameterizedType) field.getGenericType();
                    if(t.getActualTypeArguments().length == 1){
                        Type columnType = t.getActualTypeArguments()[0];
                        if(columnType instanceof Class){
                            type = (Class) columnType;
                        } else if(columnType instanceof ParameterizedType){
                            type = (Class) ((ParameterizedType) columnType).getActualTypeArguments()[0];
                        }
                        
                    }
                }
                Column c = schemaColumn.alias().isEmpty() ? 
                        new Column(name, type, table) : 
                        new Column(name, type, table, schemaColumn.alias());
                if(clazz.isAssignableFrom(table.getClass())){
                    field.setAccessible( true );
                    field.set(table, c );   
                    field.setAccessible( false );
                }
                schemaColumns.put(c.canonical(), c);
            } catch(IllegalAccessException | IllegalArgumentException | SecurityException ex) {
                throw new RuntimeException("Unable to bind Column " + name, ex);
            }
        }
        return schemaColumns;
    }
}
