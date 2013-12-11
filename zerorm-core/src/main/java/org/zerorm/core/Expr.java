
package org.zerorm.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.Formattable;
import org.zerorm.core.interfaces.MaybeHasParams;

/**
 *
 * @author bvan
 */
public class Expr implements MaybeHasParams, Formattable {
    
    static class SafeList extends Param<List> implements MaybeHasParams, Formattable {
        // TODO: Clean this up
        List checkedList;
        String name;
        public SafeList(List list){
            Class<?> type = list.get( 0 ) instanceof Number 
                    ? Number.class : list.get( 0 ).getClass();
            init(list, type);
        }
        
        public SafeList(List list, Class<?> type){
            init(list, type);
        }
        
        public SafeList(String name, List list, Class<?> type){
            this.name = null;
            init(list, type);
        }
        
        private void init(List list, Class<?> type){
            checkedList = Collections.checkedList( new ArrayList(), type );
            String name = "a0";
            for(int i = 0; i < list.size(); i++, name = "a" + i){
                checkedList.add( list.get( i ) );
            }
        }
        
        @Override
        public boolean hasParams(){
            return !getParams().isEmpty();
        }

        @Override
        public List<Param> getParams(){
            List<Param> paramList = new ArrayList<>();
            String pre = name == null ? "a" : name;
            String pname = null;
            for(int i = 0; i < checkedList.size(); i++, pname = pre + i){
                paramList.add(new Param(pname, checkedList.get( i ) ));
            }
            return paramList;
        }

        @Override
        public String formatted(AbstractSQLFormatter fmtr){
            StringBuilder format = new StringBuilder("(");
            for(Iterator i = checkedList.iterator(); i.hasNext(); ){
                format.append( new Param(name, i.next() ).formatted( fmtr ) );
                format.append( i.hasNext() ? "," : "");
            }
            return format.append( ")").toString();
        }
        
    }
    
    // Ordered list of tokens
    private boolean wrapped = false;
    private Object tLeft;
    private Object tRight;
    private Op oper;
    
    protected Expr(){ }
    
    private Expr(Object identifier){
        this.tLeft = identifier;
    }
    
    private Expr(Object object, Op op){
        this(object);
        this.oper = op;
    }
    
    /**
     * Create a new expression. This expression will be be rendered wrapped 
     * with parens by default.
     * @param object
     * @param op
     * @param right
     */
    protected Expr(Object object, Op op, Object right){
        this(object, op);
        if(right instanceof List){
            tRight = new SafeList((List)right);
        } else {
            this.tRight = right;
        }
        this.wrapped = true;
    }
    
    /**
     * Create a new expression. If wrap is true, the expression will 
     * be rendered wrapped with parens.
     * @param object
     * @param op
     * @param right
     * @param wrap 
     */
    public Expr(Object object, Op op, Object right, boolean wrap){
        this(object,op,right);
        this.wrapped = wrap;
    }
    
    public Expr(Object object, Op op, Object val1, Object val2){
        this(object, op);
        this.tRight = new Expr(val1, Op.AND, val2);
    }
        
    public static Expr or(Expr... exprs) {
        return reduce(Op.OR, true, new Expr(), exprs);
    }

    public static Expr and(Expr... exprs) {
        return reduce(Op.AND, true, new Expr(), exprs);
    }

    public static Expr reduce(Op op, boolean wrap, Expr initial, Expr... exprs){
        Expr red = initial == null ? new Expr() : initial;
        for(int i = 0; i < exprs.length; i++){
            Expr neue = exprs[i];
            if(neue != null && !neue.isEmpty()){
                red = red.isEmpty() ? neue : new Expr( red, op, neue, false );
            }
        }
        red.wrapped = wrap;
        return red;
    }
    
    public List getValues(){
        ArrayList vals = new ArrayList();
        checkAndAddValues(tLeft, vals);
        checkAndAddValues(tRight, vals);
        return vals;
    }
    
    private void checkAndAddValues(Object object, List vals){
        if(object != null){
            if(object instanceof Expr){
                vals.addAll( ((Expr) object).getValues());
            } else {
                vals.add(object);
            }
        }
    }
    
    public Object getLeft(){
        return this.tLeft;
    }
    
    public Op getOp(){
        return this.oper;
    }
    
    public Object getRight(){
        return this.tRight;
    }
        
    public boolean isEmpty(){
        return oper == null && tLeft == null && tRight == null;
    }

    @Override
    public boolean hasParams() {
        return !getParams().isEmpty();
    }
    
    @Override
    public List<Param> getParams() {
        List<Param> params = new ArrayList<>();
        getParams(tLeft, params);
        getParams(tRight, params);
        return params;
    }
    
    private void getParams(Object token, List<Param> params){
        if ( token != null ) {
            if (token instanceof MaybeHasParams) {
                params.addAll( ((MaybeHasParams) token).getParams() );
            } else if (token instanceof Param) {
                params.add( (Param) token );
            }
        }
    }
    
    @Override
    public String toString(){
        return formatted();
    }
    
    @Override
    public String formatted(){
        return formatted(AbstractSQLFormatter.getDefault());
    }
    
    @Override
    public String formatted(AbstractSQLFormatter fmtr) {
        return fmtr.format( this );
    }
    
    public boolean isWrapped(){
        return wrapped;
    }
    
    public void setWrapped(boolean encapsulated){
        this.wrapped = encapsulated;
    }
}
