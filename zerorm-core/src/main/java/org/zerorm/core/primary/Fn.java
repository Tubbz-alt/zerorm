
package org.zerorm.core.primary;

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
public class Fn extends Primary<Fn> implements MaybeHasParams {
    private final String function;
    private final MaybeHasAlias valueExpr;
    
    public static final Fn AVG = new Fn( "AVG" );
    public static final Fn MAX = new Fn( "MAX" );
    public static final Fn MIN = new Fn( "MIN" );
    public static final Fn SUM = new Fn( "SUM" );
    public static final Fn EVERY = new Fn( "EVERY" );
    public static final Fn ANY = new Fn( "ANY" );
    public static final Fn SOME = new Fn( "SOME" );
    public static final Fn COUNT = new Fn( "COUNT" );
    
    private Fn(String function){
        this.function = function;
        this.valueExpr = null;
    }
    
    private Fn(String function, MaybeHasAlias col){
        this.function = function;
        this.valueExpr = col;
    }
    
    public Primary<Fn> of(MaybeHasAlias col){
        return new Fn(this.function, col);
    }
    
    public static Primary<Fn> of(String function, MaybeHasAlias col){
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
    public String getName(){ return valueExpr.canonical(); }
    
    @Override
    public String formatted(AbstractSQLFormatter formatter){
        return function + "( " + valueExpr.formatted( formatter ) + " )";
    }
    
}
