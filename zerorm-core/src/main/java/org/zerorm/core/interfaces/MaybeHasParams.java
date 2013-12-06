
package org.zerorm.core.interfaces;

import java.util.List;
import org.zerorm.core.Param;

/**
 *
 * @author bvan
 */
public interface MaybeHasParams {
    public boolean hasParams();
    public List<Param> getParams();
}
