
package org.zerorm.core.format.dialect;

import java.text.SimpleDateFormat;

/**
 *
 * @author bvan
 */
public class Postgres implements DB {
    
    @Override
    public String toTimestamp(java.util.Date date){
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMddHHmmss.SSS" );
        return "to_timestamp('" + sdf.format( date ) + "', 'YYYYMMDDHH24MISS.MS')";
    }

}
