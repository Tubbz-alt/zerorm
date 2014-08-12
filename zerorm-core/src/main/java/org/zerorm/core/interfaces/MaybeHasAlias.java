/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zerorm.core.interfaces;

/**
 *
 * @author bvan
 */
public interface MaybeHasAlias<T> extends Formattable {
    /**
     * Must not return a null value
     * @return 
     */
    public String alias();
    public String canonical();
    public T as(String alias);
    public T asExact(String alias);
}
