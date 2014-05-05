
package org.zerorm.core;

import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.SimplePrimary;

/**
 * Raw SQL. Useful for anything that's not coded in (database specific code)
 * @author bvan
 */
public class Sql extends SimplePrimary<Sql> {
    private String sql;
    
    public Sql(){}
    
    public Sql(String str){
        this.sql = str;
    }
    
    @Override
    public String getName(){
        return sql != null ? sql : "";
    }
    
    @Override
    public String formatted(AbstractSQLFormatter fmtr){
        return getName();
    }
}
