
package org.zerorm.core;

import junit.framework.TestCase;
import static org.zerorm.core.Op.*;

/**
 *
 * @author bvan
 */
public class AnonymousTest extends TestCase {
    
    public void testAnonymous(){
        String expected0001 = "SELECT name FROM animal WHERE name = 'SPIKE'";
        String actual = new Select($("name"))
                .from("animal").where( Op.eq( $("name"), "SPIKE" ) ).formatted();
        assertEquals( expected0001, actual );

        // Verify no replacement
        String expected0002 = "SELECT name FROM animal WHERE 'name' = 'SPIKE'";
        actual = new Select( $("name") )
                .from("animal").where( Op.eq( "name", "SPIKE" ) ).formatted();
        assertEquals( expected0002, actual );
        
    }
}
