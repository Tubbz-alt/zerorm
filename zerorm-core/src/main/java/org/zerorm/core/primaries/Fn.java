
package org.zerorm.core.primaries;

import java.util.ArrayList;
import java.util.List;
import org.zerorm.core.Param;
import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.MaybeHasAlias;
import org.zerorm.core.interfaces.MaybeHasParams;
import org.zerorm.core.interfaces.Primary;

/**
 *
 * @author bvan
 */
public class Fn extends Primary<Fn> {
    private final String function;
    private final MaybeHasAlias valueExpr;
    private final String fnFormat;
    public static final String FN_DEFAULT_FMT = "%s( %s )";
    
    public static final Fn AVG = new Fn( "AVG" );
    public static final Fn MAX = new Fn( "MAX" );
    public static final Fn MIN = new Fn( "MIN" );
    public static final Fn SUM = new Fn( "SUM" );
    public static final Fn EVERY = new Fn( "EVERY" );
    public static final Fn ANY = new Fn( "ANY" );
    public static final Fn SOME = new Fn( "SOME" );
    public static final Fn COUNT = new Fn( "COUNT" );
    public static final Fn DISTINCT = new Fn( "DISTINCT", "%s %s" );
    
    private Fn(String function){
        this.fnFormat = FN_DEFAULT_FMT;
        this.function = function;
        this.valueExpr = null;
    }
    
    private Fn(String function, String format){
        this.fnFormat = format;
        this.function = function;
        this.valueExpr = null;
    }
    
    private Fn(String function, MaybeHasAlias col, String format){
        this.fnFormat = format;
        this.function = function;
        this.valueExpr = col;
    }
    
    public Primary<Fn> of(MaybeHasAlias col){
        return new Fn(function, col, fnFormat);
    }
    
    public static Primary<Fn> of(String function, MaybeHasAlias col){
        return new Fn(function, col, FN_DEFAULT_FMT);
    }
    
    public MaybeHasAlias getValueExpression(){
        return this.valueExpr;
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
    public String getName(){ return valueExpr.canonical(); }
    
    @Override
    public String formatted(AbstractSQLFormatter formatter){
        return String.format( fnFormat, function, valueExpr.formatted( formatter ) );
    }
    
}
