
package org.zerorm.core;

import java.util.List;
import org.zerorm.core.Expr.SafeList;
import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.MaybeHasAlias;
import org.zerorm.core.interfaces.MaybeHasParams;
import org.zerorm.core.interfaces.SimplePrimary;
import org.zerorm.core.interfaces.SimpleTable;

/**
 * Column class. 
 * A column can belong to a table
 * @author bvan
 */
public class Column<T> extends SimplePrimary<Column> {
    private SimpleTable parent;
    private String name;
    private Class<?> javaType;
    
    private Column(){this.name = "";}
    
    /**
     * Construct a column tied to a table.
     */
    public Column(String name, SimpleTable parent){
        this(name, Object.class, parent);
    }

    /**
     * Construct a column with a parent table and alias
     */
    public Column(String name, SimpleTable parent, String alias){
        init(name, Object.class, parent, alias);
    }
    
    public Column(MaybeHasAlias orig, SimpleTable parent){
        Class tClass = orig instanceof Column ? ((Column) orig).getJavaType() : Object.class;
        String cname = orig instanceof SimplePrimary ? ((SimplePrimary) orig).getName() : orig.canonical();
        init(cname, tClass, parent, orig.alias());
    }
    
    /**
     * Construct a column tied to a table. Supply a default column type (Long, String, etc..)
     */
    public Column(String name, Class<?> type, SimpleTable parent){
        init(name,type,parent,null);
    }

    public Column(String name, Class<?> type, SimpleTable parent, String alias){
        init(name,type,parent,alias);
    }
    
    private void init(String name, Class<?> type, SimpleTable parent, String alias){
        this.name = name;
        this.parent = parent;
        this.alias = alias;
        this.javaType = type;
    }
    
    public String getName(){
        return name != null ? name : "";
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public SimpleTable getParent(){
        return this.parent;
    }

    /**
     * Sets the alias and returns this column
     * @param alias
     * @return 
     */
    @Override
    public Column as(String alias){
        if(this.name.equals("*")){
            return this;
        }
        return super.as( alias );
    }
    
    @Override
    public Column asExact(String alias){
        // Remove any illegal characters for SQL
        alias = alias.replace( "\"", "");
        if(this.name.equals("*")){
            return this;
        }
        return super.asExact( alias );
    }
    
    public Param<T> checkedParam(){
        return (Param<T>) Param.checkedParam( javaType );
    }
    
    public Param<T> checkedParam(String name){
        return (Param<T>) Param.checkedParam( name, javaType );
    }
    
    public Param<T> checkedParam(String name, T value){
        if(value instanceof List){
            return (Param<T>) new SafeList(name, (List) value, javaType);
        }
        return (Param<T>) Param.checkedParam( name, javaType, value );
    }
    
    public MaybeHasParams checkedParamList(String name, List value){
        return new SafeList(name, value, javaType);
    }
    
    @Override
    public String formatted(AbstractSQLFormatter fmtr) {
        return fmtr.format( this );
    }
        
    public Class<?> getJavaType(){
        return this.javaType;
    }

    // Convenience methods
    /** between val1 and val2 */
    public Expr between(T val1, T val2){ return Op.between(this, val1, val2); }
    public Expr between(MaybeHasAlias val1, MaybeHasAlias val2){ return Op.between(this, val1, val2); }
    public Expr between(Param<T> val1, Param<T> val2){ return Op.between(this, val1, val2); }
    
    /** not between val1 and val2 */
    public Expr not_between(T val1, T val2){ return Op.not_between(this, val1, val2); }
    public Expr not_between(MaybeHasAlias val1, MaybeHasAlias val2){ return Op.not_between(this, val1, val2); }
    public Expr not_between(Param<T> val1, Param<T> val2){ return Op.not_between(this, val1, val2); }
    
    /** equal to */
    public Expr eq(T val1) { return Op.eq( this, val1 ); }
    public Expr eq(MaybeHasAlias val1) { return Op.eq( this, val1 ); }
    public Expr eq(Param<T> val1) { return Op.eq( this, val1 ); }
    
    /** not equal to */
    public Expr neq(T val1) { return Op.neq( this, val1 ); }
    public Expr neq(MaybeHasAlias val1) { return Op.neq( this, val1 ); }
    public Expr neq(Param<T> val1) { return Op.neq( this, val1 ); }
    
    /** less than */
    public Expr lt(T val1) { return Op.lt( this, val1 ); }
    public Expr lt(MaybeHasAlias val1) { return Op.lt( this, val1 ); }
    public Expr lt(Param<T> val1) { return Op.lt( this, val1 ); }
    
    /** less than or equal to */
    public Expr lteq(T val1){ return Op.lteq( this, val1 ); }
    public Expr lteq(MaybeHasAlias val1){ return Op.lteq( this, val1 ); }
    public Expr lteq(Param<T> val1){ return Op.lteq( this, val1 ); }
    
    /** greater than */
    public Expr gt(T val1){ return Op.gt( this, val1 ); }
    public Expr gt(MaybeHasAlias val1){ return Op.gt( this, val1 ); }
    public Expr gt(Param<T> val1){ return Op.gt( this, val1 ); }
    
    /** greater than or equal to */
    public Expr gteq(T val1){ return Op.gteq( this, val1 ); }
    public Expr gteq(MaybeHasAlias val1){ return Op.gteq( this, val1 ); }
    public Expr gteq(Param<T> val1){ return Op.gteq( this, val1 ); }
    
    /** in */
    public Expr in(T val1){ return Op.in( this, val1 ); }
    public Expr in(List<T> val1){ return Op.in( this, val1 ); }
    public Expr in(MaybeHasAlias val1){ return Op.in( this, val1 ); }
    public Expr in(Param<T> val1){ return Op.in( this, val1 ); }
    
    /** not in */
    public Expr not_in(T val1) { return Op.not_in( this, val1 ); }
    public Expr not_in(List<T> val1) { return Op.not_in( this, val1 ); }
    public Expr not_in(MaybeHasAlias val1) { return Op.not_in( this, val1 ); }
    public Expr not_in(Param<T> val1) { return Op.not_in( this, val1 ); }
    
    /** is null */
    public Expr is_null() { return Op.is_null(this); }
    
    /** is not null */
    public Expr not_null() { return Op.not_null(this); }
    
}
