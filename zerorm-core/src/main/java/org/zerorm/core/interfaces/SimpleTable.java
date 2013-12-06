
package org.zerorm.core.interfaces;

import java.util.List;

/**
 *
 * @author bvan
 */
public interface SimpleTable<T> extends MaybeHasAlias<T>, Formattable {
    public List<? extends MaybeHasAlias> getColumns();
}
