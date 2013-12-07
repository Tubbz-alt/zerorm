
package org.zerorm.core.interfaces;

import java.util.List;

/**
 * Parent for Table and Select.
 * @author bvan
 */
public interface SimpleTable<T> extends MaybeHasAlias<T>, Formattable {
    public List<? extends MaybeHasAlias> getColumns();
}
