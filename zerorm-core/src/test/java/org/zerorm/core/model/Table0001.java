package org.zerorm.core.model;

import org.zerorm.core.Column;
import org.zerorm.core.Table;
import org.zerorm.core.interfaces.Schema;

/**
 *
 * @author bvan
 */
public class Table0001 extends Table {

    @Schema public Column<Long> pk;
    @Schema public Column<String> name;

    public Table0001(){
        super();
    }

}
