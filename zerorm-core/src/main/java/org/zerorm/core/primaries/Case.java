
package org.zerorm.core.primaries;

import java.util.ArrayList;
import java.util.List;
import org.zerorm.core.Expr;
import org.zerorm.core.Param;
import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.Formattable;
import org.zerorm.core.interfaces.MaybeHasParams;
import org.zerorm.core.interfaces.Primary;

/**
 *
 * @author bvan
 */
public class Case extends Primary<Case> {
    private Expr expression;
    private Formattable tClause;
    private Formattable eClause;
    
    public Case(Expr caseExpression, Formattable thenClause, Formattable elseClause){
        this.expression = caseExpression;
        this.tClause = thenClause;
        this.eClause = elseClause;
        this.expression.setWrapped( false );
    }
    
    @Override
    public String getName(){
        return "";
    }

    @Override
    public String formatted(AbstractSQLFormatter fmtr){
        StringBuilder sb = new StringBuilder();
        sb.append( "CASE WHEN ").append( expression.formatted( fmtr ) );
        sb.append( " THEN " ).append( tClause.formatted() );
        sb.append( " ELSE " ).append( eClause.formatted() );
        sb.append( " END");
        return sb.toString();
    }

    @Override
    public List<Param> getParams(){
        ArrayList<Param> params = new ArrayList<>();
        params.addAll( expression.getParams() );
        if(tClause instanceof MaybeHasParams)
            params.addAll( ((MaybeHasParams) tClause).getParams() );
        if(eClause instanceof MaybeHasParams)
            params.addAll( ((MaybeHasParams) eClause).getParams() );
        return params;
    }
}
