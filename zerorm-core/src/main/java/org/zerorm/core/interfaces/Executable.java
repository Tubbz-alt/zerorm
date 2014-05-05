
package org.zerorm.core.interfaces;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.zerorm.core.Param;
import org.zerorm.core.Table.CTE;
import org.zerorm.core.format.AbstractSQLFormatter;

/**
 *
 * @author bvan
 */
public abstract class Executable<T> implements Formattable, MaybeHasParams {
    private List<CTE> ctes = new ArrayList<>();

    public PreparedStatement prepare(Connection conn) throws SQLException {
        String sql = formatted();
        PreparedStatement stmt = conn.prepareStatement(sql);
        return stmt;
    }

    public void bindAll(PreparedStatement stmt) throws SQLException {
        Iterator<Param> params = getParams().iterator();
        for (int i = 1; params.hasNext(); i++) {
            // TODO: Handle Safe List
            stmt.setObject(i, params.next().getValue());
        }
    }

    public PreparedStatement prepareAndBind(Connection conn) throws SQLException {
        PreparedStatement stmt = prepare(conn);
        try {
            bindAll(stmt);
        } catch (SQLException ex){
            stmt.close();
            throw ex;
        }
        return stmt;
    }
    
    public T with(CTE commonTableExpression){
        this.ctes.add( commonTableExpression );
        return (T) this;
    }
    
    public List<CTE> getWiths(){
        return ctes;
    }
    
    public void dump() {
        System.out.println(formatted());
    }
    
    @Override
    public boolean hasParams() {
        return !getParams().isEmpty();
    }
    
    @Override
    public String formatted() {
        return formatted(AbstractSQLFormatter.getDefault());
    }
}
