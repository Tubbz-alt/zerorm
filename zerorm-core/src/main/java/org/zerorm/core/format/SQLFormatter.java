package org.zerorm.core.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.zerorm.core.Column;
import org.zerorm.core.Delete;
import org.zerorm.core.Expr;
import org.zerorm.core.Insert;
import org.zerorm.core.Op;
import org.zerorm.core.Param;
import org.zerorm.core.Select;
import org.zerorm.core.Select.JoinExpr;
import org.zerorm.core.Sql;
import org.zerorm.core.Table;
import org.zerorm.core.Update;
import org.zerorm.core.Val;
import org.zerorm.core.format.db.DB;
import org.zerorm.core.interfaces.Formattable;
import org.zerorm.core.interfaces.MaybeHasAlias;
import org.zerorm.core.interfaces.SimpleTable;

/**
 *
 * @author bvan
 */
public class SQLFormatter extends AbstractSQLFormatter {
    DB dbImpl;
    public SQLFormatter(DB dbImpl){ this.dbImpl = dbImpl;}
    
    @Override
    public String format(Insert stmt){
        Object source = stmt.getSource();
        List<Column> columns = stmt.getColumns();
        if( source == null){
            throw new RuntimeException( "Source is null" );
        }
        StringBuilder sql = new StringBuilder();
        sql.append( "INSERT INTO " );
        sql.append( stmt.getInto().getTable() );
        
        if (!columns.isEmpty()) {
            StringBuilder clist = new StringBuilder( );
            for( Iterator<Column> i = columns.iterator(); i.hasNext(); ) {
                clist.append(i.next().getName() );
                clist.append( i.hasNext() ? ", " : "" );
            }
            sql.append( " ( " ).append( clist.toString() ).append( " )" );
        }
        
        if(source instanceof List){
            List values = (List) source;
            sql.append( " VALUES " );
            StringBuilder vl = new StringBuilder();
            for(Iterator i = values.listIterator(); i.hasNext();){
                vl.append( SQLFormatter.getDefault().formatAsSafeString( i.next() ) );
                vl.append( i.hasNext() ? ", " : "" );
            }
            sql.append( "( " ).append( vl.toString() ).append( " )" );

        } else if(source instanceof SimpleTable){
            if(source instanceof Select){
                Select values = (Select) source;
                sql.append( " " ).append( format(values) );
            } else {
                Table t = (Table) source;
                sql.append( " TABLE " ).append( t.getTable() );
            }
        } else {
            throw new RuntimeException("Source is invalid");
        }
        
        return sql.toString();
    }
    
    @Override
    public String format(Select stmt){
        StringBuilder sql = new StringBuilder();
        sql.append( formatColumns( stmt.getSelections() ,"SELECT", true) );
        sql.append( "FROM " );
        sql.append( aliased( stmt.getFrom().formatted(this), stmt.getFrom()) );
        sql.append( format( stmt.getJoins() ) );
        if(!stmt.getWhere().isEmpty()){
            sql.append( " WHERE " ).append( format(stmt.getWhere() ) );
        }
        sql.append( formatColumns( stmt.getGroupBys()," GROUP BY", false) );
        if(!stmt.getHaving().isEmpty()){
            sql.append( " HAVING " ).append( format(stmt.getHaving() ) );
        }
        sql.append( formatColumns( stmt.getOrderBys()," ORDER BY", false) );
        return stmt.getWrapped() ? "( " + sql.toString() + " )" : sql.toString();
    }
    
    @Override
    public String format(Update stmt){
        StringBuilder sql = new StringBuilder();
        sql.append( "UPDATE " );
        sql.append( stmt.getTarget().getTable() );
        
        if (stmt.getClauses().isEmpty()) {
            throw new RuntimeException("No SET clauses defined");
        }
        sql.append( " SET " );
        for(Iterator<Expr> iter =  stmt.getClauses().iterator(); iter.hasNext();){
            Expr e = iter.next();
            e.setWrapped( false );
            sql.append( format(e) ).append( iter.hasNext() ? ", ":"");
        }
        
        if (!stmt.getWhere().isEmpty()) {
            sql.append( " WHERE " ).append( format( stmt.getWhere() ) );
        }

        return sql.toString();
    }
    
    @Override
    public String format(Delete stmt){
        StringBuilder sql = new StringBuilder();
        sql.append( "DELETE FROM " );
        sql.append( stmt.getFrom().getTable() );
        if (!stmt.getWhere().isEmpty()) {
            sql.append( " WHERE "  ).append( format(stmt.getWhere() ) );
        } else if(stmt.getProtected()){
            throw new RuntimeException("Cannot DELETE all from an unprotected table");
        }
        return sql.toString();
    }
    
    private String formatColumns(Collection<? extends MaybeHasAlias> cols, String clause, 
            boolean aliased){
        if(cols.isEmpty()){ return ""; }
        ArrayList<String> canonicals = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append( clause ).append( " " );
        for(Iterator<? extends MaybeHasAlias> i1 = cols.iterator(); i1.hasNext();){
            MaybeHasAlias selection = i1.next();
            // Make sure we're not adding redundant columns (in the case of some blind joins)
            if(canonicals.contains( selection.canonical() )){
                if(!i1.hasNext()){
                    int commaIndex = sql.length() - 2;
                    // Handle the case that the redundant canonical is at the end of the list
                    if(",".equals( sql.substring( commaIndex, commaIndex + 1 ) )){
                        sql.delete( commaIndex, commaIndex + 1 );
                    }
                }
                continue;
            }

            canonicals.add( aliased ? selection.canonical() : ((Column) selection).getName() );
            String fmt = selection instanceof Column ? format(((Column) selection) ) 
                    : selection.formatted(this);
            sql.append( aliased ? aliased(fmt, selection) : selection.formatted(this) );
            sql.append( i1.hasNext() ? ", " : "" );
        }
        return sql.append( " ").toString();
    }
    
    @Override
    public String format(Column col){
        StringBuilder s = new StringBuilder();
        SimpleTable parent = col.getParent();
        // If the parent isn't null, add the name
        if(parent != null){
            s.append( parent.canonical() ).append( "." );
        }
        s.append( col.getName() );
        return s.toString();
    }
    
    @Override
    public String format(Param param){
        if(!getDebugParams()){ return "?"; }
        String type = "";
        if( param.getValueClass() != null ){
            type = "(" + param.getValueClass().getCanonicalName() + ")";
        }
        String val = param.getValue() != null ? param.getValue().toString()  : "null";
        System.err.println( ":" + param.getName() + "->" + type + val);
        return "?";
    }

    // joins
    public String format(LinkedHashMap<JoinExpr, SimpleTable> joinList){
        StringBuilder sql = new StringBuilder();
        Iterator<Entry<JoinExpr, SimpleTable>> iter = joinList.entrySet().iterator();

        for(Entry<JoinExpr, SimpleTable> jc; iter.hasNext();){
            jc = iter.next();
            sql.append( " " ).append( jc.getKey().op() ).append( " " );
            sql.append( aliased(jc.getValue().formatted(this), jc.getValue()) ).append( " ON " );
            sql.append( format( jc.getKey().test() ) );
        }
        return sql.toString();
    }
    
    @Override
    public String format(Val val){
        Object value = val.getValue();
        if(value instanceof String){
            return "'" + value.toString() + "'";
        } else if(value instanceof java.util.Date){
            return getDateAsSQLString((java.util.Date) value);
        }
        return value.toString();
    }
    

    public String format(Expr expr){
        StringBuilder s = new StringBuilder();
        if(expr.isEmpty()){ return s.toString(); }
        s.append( formatExprForSqlString( expr.getLeft() ));
        s.append( formatExprForSqlString( expr.getOp()));
        s.append( formatExprForSqlString( expr.getRight() ));
        
        return expr.isWrapped() ? "( " + s.toString() + " )" : s.toString();
    }
    
    private String formatExprForSqlString(Object token){
        if(token == null){
            return "";
        }
        if(token instanceof Op){ return " " + token.toString() + " "; }
        return formatAsSafeString(token);
    }
    
    @Override
    public String formatAsSafeString(Object value){
        if(value instanceof Expr){
            return format((Expr) value) ;
        } else if(value instanceof Column){
            return format((Column) value ) ;
        } else if(value instanceof Formattable){
            return ((Formattable) value).formatted(this);
        } else if(value instanceof String){
            return "'" + value + "'" ;
        } else if(value instanceof Sql){
            return value.toString();
        } else if(value instanceof java.util.Date){
            return getDateAsSQLString((java.util.Date) value);
        }
        return value.toString();
    };
    
    public String getDateAsSQLString(java.util.Date date){
        return dbImpl.toTimestamp( date );
    }

}