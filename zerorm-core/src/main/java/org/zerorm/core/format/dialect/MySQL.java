
package org.zerorm.core.format.dialect;

import java.text.SimpleDateFormat;

/**
 *
 * @author bvan
 */
public class MySQL implements DB {

    @Override
    public String toTimestamp(java.util.Date date){
        SimpleDateFormat sdf = new SimpleDateFormat( "''yyyy-MM-dd HH:mm:ss.SSS''" );
        return sdf.format( date );
    }
    
}
