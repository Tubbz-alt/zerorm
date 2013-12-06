
package org.zerorm.core.model;

import java.sql.Timestamp;
import org.zerorm.core.Column;
import org.zerorm.core.Table;
import org.zerorm.core.interfaces.Schema;

/**
 *
 * @author bvan
 */
public class Table0002 extends Table {
    @Schema public Column<Long> table0001_pk;
    @Schema public Column<Timestamp> createdate;

    public Table0002(){
        super();
    }
}
