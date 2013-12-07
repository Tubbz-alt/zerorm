
package org.zerorm.core.interfaces;

import org.zerorm.core.format.AbstractSQLFormatter;

/**
 *
 * @author bvan
 * @param <T> The subchild, to help with the "as*(String alias... methods
 */
public abstract class Primary<T> implements MaybeHasAlias<T>{
    protected String alias = "";

    @Override
    public String alias() {
        return alias != null ? alias : "";
    }

    @Override
    public T as(String alias) {
        this.alias = alias;
        return (T) this;
    }
    
    @Override
    public T asExact(String alias){
        this.alias = '"' + alias + '"';
        return (T) this;
    }
    
    /**
     * Recast as a different class (useful for classes that extend Primary)
     * @param <U>
     * @param alias
     * @param clazz
     * @return 
     */
    public <U> U as(String alias, Class<U> clazz){
        this.alias = alias;
        return (U) this;
    }
    
    public <U> U asExact(String alias, Class<U> clazz){
        this.alias = '"' + alias + '"';
        return (U) this;
    }
    
    public abstract String getName();
    
    @Override
    public String canonical(){
        return !alias.isEmpty() ? alias : getName();
    }
    
    @Override
    public String formatted(){
        return formatted(AbstractSQLFormatter.getDefault());
    }
}
