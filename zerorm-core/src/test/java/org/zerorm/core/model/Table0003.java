
package org.zerorm.core.model;

import org.zerorm.core.Column;
import org.zerorm.core.Table;
import org.zerorm.core.interfaces.Schema;

/**
 *
 * @author bvan
 */
public class Table0003 extends Table {

    @Schema public Column<Long> pk;
    @Schema public Column<Long> parent;
    @Schema public Column<String> name;

    public Table0003(){
        super();
    }

}
