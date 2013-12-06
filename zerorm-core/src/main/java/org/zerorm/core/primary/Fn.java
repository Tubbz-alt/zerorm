
package org.zerorm.core.primary;

import java.util.ArrayList;
import java.util.List;
import org.zerorm.core.Param;
import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.MaybeHasAlias;
import org.zerorm.core.interfaces.MaybeHasParams;

/**
 *
 * @author bvan
 */
public class Fn implements MaybeHasParams, MaybeHasAlias<Fn> {
    private final String function;
    private final MaybeHasAlias valueExpr;
    private String alias = "";

    public static enum FnType {
        AVG, MAX, MIN, SUM, EVERY, ANY, SOME, COUNT;
        
        public Fn of(MaybeHasAlias col){
            return new Fn(this.name(), col);
        }
    }
    
    public Fn(String function, MaybeHasAlias col){
        this.function = function;
        this.valueExpr = col;
    }
    
    public static Fn of(FnType fnType, MaybeHasAlias col){
        return new Fn(fnType.name(), col);
    }
    
    public static Fn of(String function, MaybeHasAlias col){
        return new Fn(function, col);
    }
    
    @Override
    public boolean hasParams(){
        if(valueExpr instanceof MaybeHasParams){
            return ((MaybeHasParams) valueExpr).hasParams();
        }
        return false;
    }

    @Override
    public List<Param> getParams(){
        if(valueExpr instanceof MaybeHasParams){
            return ((MaybeHasParams) valueExpr).getParams();
        }
        return new ArrayList<>();
    }
    
    @Override
    public String alias(){ return alias == null ? "" : alias; }

    @Override
    public String canonical(){ return alias().isEmpty() ? valueExpr.canonical() : alias; }

    @Override
    public Fn as(String alias){ this.alias = alias; return this; }

    @Override
    public Fn asExact(String alias){ this.alias = '"' + alias + '"'; return this; }

    @Override
    public String formatted(){ return formatted(AbstractSQLFormatter.getDefault()); }
    
    @Override
    public String formatted(AbstractSQLFormatter formatter){
        return function + "( " + valueExpr.formatted( formatter ) + " )";
    }

}
