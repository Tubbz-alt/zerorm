/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zerorm.core.format;

import org.zerorm.core.Column;
import org.zerorm.core.Delete;
import org.zerorm.core.Expr;
import org.zerorm.core.Insert;
import org.zerorm.core.Param;
import org.zerorm.core.Select;
import org.zerorm.core.Sql;
import org.zerorm.core.Update;
import org.zerorm.core.Val;
import org.zerorm.core.format.db.DB.DBType;
import org.zerorm.core.interfaces.MaybeHasAlias;

/**
 *
 * @author bvan
 */
public abstract class AbstractSQLFormatter {
    private static AbstractSQLFormatter defaultFormatter;
    
    public static AbstractSQLFormatter getDefault(){
        if(defaultFormatter == null){
            defaultFormatter = setDefault(DBType.MYSQL);
        }
        return defaultFormatter;
    }
    
    public static AbstractSQLFormatter setDefault(DBType dbType){
        defaultFormatter = new SQLFormatter(dbType.getInstance());
        return defaultFormatter;
    }

    public static AbstractSQLFormatter setDefault(String dbType){
        return setDefault(DBType.valueOf( dbType.toUpperCase() ));
    }

    boolean debugParams = false;
    public boolean getDebugParams(){
        return this.debugParams;
    }
    
    public void setDebugParams(boolean debug){
        this.debugParams = debug;
    }
    
    public String aliased(String formatted, MaybeHasAlias aliased){
        return formatted + (!aliased.alias().isEmpty() ? " " + aliased.alias() : "");
    }
    
    public abstract String format(Insert stmt);
    public abstract String format(Select stmt);
    public abstract String format(Update stmt);
    public abstract String format(Delete stmt);
    public abstract String format(Param param);
    public abstract String format(Val val);
    public String format(Sql sql){ return sql.toString(); }
    public abstract String format(Column col);
    public abstract String format(Expr expr);
    public abstract String formatAsSafeString(Object value);
}
