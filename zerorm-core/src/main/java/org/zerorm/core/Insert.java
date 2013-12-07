
package org.zerorm.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.Executable;
import org.zerorm.core.interfaces.MaybeHasParams;
import org.zerorm.core.interfaces.SimpleTable;

/**
 *
 * @author bvan
 */
public class Insert extends Executable {
    private Table into;
    private final List<Column> columns = new ArrayList<>();
    private Object source;  // List or Select
    
    public Insert() { }

    /**
     * Construct initial INSERT statement with the table to be inserted into
     * @param table
     */
    public Insert(Table table) {
        this.into = table;
    }
    
    public Insert(String tableName) {
        this.into = new Table(tableName);
    }
    
    public Insert into(Table into){
        this.into = into;
        return this;
    }

    public Insert into(String into){
        this.into = new Table(into);
        return this;
    }

    public Table getInto(){
        return into;
    }
    
    /**
     * Columns of the table that should be inserted into.
     * @param columns
     */
    public Insert columns(Column... columns){
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    public List<Column> getColumns(){
        return columns;
    }
    
    /**
     * If inserting values, source is now a list
     * @param values
     * @return 
     */
    public Insert values(Object... values){
        List l = new ArrayList();
        l.addAll(Arrays.asList(values));
        this.source = l;
        return this;
    }
    
    @Override
    public String formatted(AbstractSQLFormatter fmtr) {
        return fmtr.format( this );
    }
    
    public Object getSource(){
        return this.source;
    }
    
    /**
     * Select source for the insertion.
     * This should either be a List or a SimpleTable (Table or Select objects)
     * @param source
     * @return 
     */
    public Insert source(Object source){
        if( !((source instanceof List) || (source instanceof SimpleTable))){
            throw new RuntimeException("Value must be list or SimpleTable (table or query)");
        }
        this.source = source;
        return this;
    }
    
    @Override
    public List<Param> getParams() {
        ArrayList<Param> params = new ArrayList<>();
        if(getSource() instanceof List){
            for(Object o: (List) getSource()){
                if(o instanceof MaybeHasParams){
                    params.addAll( ((MaybeHasParams) o).getParams());
                }
            }
        } else if(getSource() instanceof MaybeHasParams){
            params.addAll( ((MaybeHasParams) getSource()).getParams());
        }
        return params;
    }
}
