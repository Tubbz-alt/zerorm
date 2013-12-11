
package org.zerorm.core.primaries;

import java.util.List;
import org.zerorm.core.Expr;
import org.zerorm.core.Param;
import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.MaybeHasParams;
import org.zerorm.core.interfaces.Primary;

/**
 *
 * @author bvan
 */ 
public class ValExpr extends Primary<ValExpr> implements MaybeHasParams {
    private final Expr valueExpression;

    public ValExpr(Expr expression){
        this.valueExpression = expression;
    }
    
    public Expr getExpression(){
        return valueExpression;
    }
    
    @Override
    public String getName(){
        return "";
    }

    @Override
    public String formatted(AbstractSQLFormatter formatter){
        return valueExpression.formatted( formatter );
    }

    @Override
    public boolean hasParams(){
        return !getParams().isEmpty();
    }

    @Override
    public List<Param> getParams(){
        return valueExpression.getParams();
    }

}
