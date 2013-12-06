
package org.zerorm.core;

import junit.framework.TestCase;
import org.zerorm.core.model.Table0001;

/**
 *
 * @author bvan
 */
public class TableTest extends TestCase {

    
    public void testNativeColumns(){
        Table0001 t1 = new Table0001();
        for(Column c: t1.getColumns()){
            System.out.println(c.formatted());
        }

    }
}
