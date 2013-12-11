
package org.zerorm.core;

import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.SimplePrimary;

/**
 * A basic value. Useful for user-defined columns/rows
 * @author bvan
 */
public class Val<T> extends SimplePrimary<Val> {
    private T value;
    
    public Val(){}
    
    public Val(T value) { 
        this.value = value;
    }
    
    public Val(T value, String alias) { 
        this.value = value;
        this.alias = alias;
    }
    
    public T getValue(){
        return this.value;
    }
    
    @Override
    public String getName(){
        return value != null ? value.toString() : "";
    }
    
    @Override
    public String formatted(AbstractSQLFormatter fmtr){
        return fmtr.format( this );
    }
}
