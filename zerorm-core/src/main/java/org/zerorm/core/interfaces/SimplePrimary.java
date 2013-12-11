package org.zerorm.core.interfaces;

import org.zerorm.core.format.AbstractSQLFormatter;

/**
 *
 * @author bvan
 * @param <U> The subchild, to help with the "as*(String alias... methods
 */
public abstract class SimplePrimary<U extends SimplePrimary> implements MaybeHasAlias<U> {

    protected String alias = "";

    @Override
    public String alias(){
        return alias != null ? alias : "";
    }

    @Override
    public U as(String alias){
        this.alias = alias;
        return (U) this;
    }

    @Override
    public U asExact(String alias){
        this.alias = '"' + alias + '"';
        return (U) this;
    }

    /**
     * Recast as a different class (useful for classes that extend Primary)
     *
     * @param <U>
     * @param alias
     * @param clazz
     * @return
     */
    public <V extends SimplePrimary> V as(String alias, Class<V> clazz){
        this.alias = alias;
        return (V) this;
    }

    public <V extends SimplePrimary> V asExact(String alias, Class<V> clazz){
        this.alias = '"' + alias + '"';
        return (V) this;
    }

    public abstract String getName();

    @Override
    public String toString(){
        return getName();
    }

    @Override
    public String canonical(){
        return !alias.isEmpty() ? alias : getName();
    }

    @Override
    public String formatted(){
        return formatted( AbstractSQLFormatter.getDefault() );
    }
}

