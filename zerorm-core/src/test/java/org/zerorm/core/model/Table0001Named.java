
package org.zerorm.core.model;

import org.zerorm.core.Column;
import org.zerorm.core.Table;
import org.zerorm.core.interfaces.Schema;

/**
 *
 * @author bvan
 */
@Schema(name="Table0001")
public class Table0001Named extends Table {

    @Schema(name="pk") public Column<Long> pkNamed;
    @Schema(name="named") public Column<String> nameNamed;

    public Table0001Named(){
        super();
    }
}
