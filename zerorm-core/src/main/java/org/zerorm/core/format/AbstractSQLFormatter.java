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
import org.zerorm.core.format.dialect.DB.DBType;
import org.zerorm.core.interfaces.MaybeHasAlias;

/**
 *
 * @author bvan
 */
public abstract class AbstractSQLFormatter {
    private static DBType defaultType = DBType.MYSQL;
    private static boolean defaultDebugParams = false;
    private boolean debugParams = false;

    public static AbstractSQLFormatter getDefault(){
        SQLFormatter fmtr = new SQLFormatter(defaultType.getInstance());
        fmtr.setDebugParams( defaultDebugParams );
        return fmtr;
    }
    
    public static void setDefault(DBType dbType){
        defaultType = dbType;
    }

    public static void setDefault(String dbType){
        setDefault(DBType.valueOf( dbType.toUpperCase() ));
    }
    
    public static boolean getDefaultDebugParams(){
        return defaultDebugParams;
    }
    
    public static void setDefaultDebugParams(boolean debug){
        defaultDebugParams = debug;
    }

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
