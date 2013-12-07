
package org.zerorm.core;

import java.util.List;
import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.Executable;

/**
 * DELETE statement
 * @author bvan
 */
public class Delete extends Executable {

    private Table from;
    private Expr where = new Expr();
    private boolean protectedTable = true;

    /**
     * Construct initial DELETE statement.
     */
    public Delete() { }

    /**
     * Construct initial DELETE statement with the table to be deleted from
     * @param table
     */
    public Delete(Table table) {
        this.from = table;
    }

    public Delete(String tableName) {
        this.from = new Table(tableName);
    }
    
    /**
     * FROM
     * Add primary table for selection by table name
     * Does not support implicit joins (i.e. 'FROM table1, table2' )
     * @param tableName
     * @param tableAlias
     * @return this
     */
    public Delete from(Table table) {
        from = table;
        return this;
    }

    public Delete from(String tableName) {
        return from(new Table(tableName));
    }
    
    public Table getFrom(){
        return this.from;
    }
    
    /**
     * WHERE statement.
     * @param predicates
     * @return 
     */
    public Delete where(Expr... predicates) {
        if(predicates.length == 1 && predicates[0] != null){
            if(where.isEmpty()){
                where = Expr.and( predicates );
            } else {
                where = Expr.and( where, Expr.and( predicates ) );
            }
            where.setWrapped( false );
        }
        return this;
    }
    
    public Expr getWhere(){
        return this.where;
    }
   
    @Override
    public String formatted(AbstractSQLFormatter fmtr) {
        return fmtr.format( this );
    }

    @Override
    public List<Param> getParams() {
        return where.getParams();
    }
    
    public boolean getProtected(){
        return this.protectedTable;
    }

    public void setProtected(boolean protect){
        this.protectedTable = protect;
    }
}
