
package org.zerorm.core.interfaces;

/**
 *
 * @author bvan
 * @param <T> The subchild, to help with the "as*(String alias... methods
 */
public abstract class Primary<T extends Primary> extends SimplePrimary<T> implements MaybeHasParams {

    @Override
    public boolean hasParams(){
        return !getParams().isEmpty();
    }
}
