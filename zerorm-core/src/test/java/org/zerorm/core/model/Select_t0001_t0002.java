
package org.zerorm.core.model;

import org.zerorm.core.Select;

/**
 *
 * @author bvan
 */
public class Select_t0001_t0002 extends Select {
    Table0001 t1 = new Table0001();
    Table0002 t2 = new Table0002();
    
    public Select_t0001_t0002(){
        super();
        from( t1 )
                .selection( t1.getColumns() )
                .selection( t1.getColumns() )
                .join( t2, t1.pk.eq( t2.table0001_pk ) );
    }

}
