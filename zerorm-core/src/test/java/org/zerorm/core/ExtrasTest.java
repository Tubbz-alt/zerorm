
package org.zerorm.core;

import junit.framework.TestCase;
import org.zerorm.core.primaries.Case;
import org.zerorm.core.model.Table0001;
import static org.zerorm.core.primaries.Fn.*;
import org.zerorm.core.primaries.ValExpr;

/**
 *
 * @author bvan
 */
public class ExtrasTest extends TestCase {

    
    public void testCaseStatement(){
        String message = "Case statement mismatch: expected '%s', actual '%s'";
        
        Table0001 t1 = new Table0001();
        
        String expected0001 = "SELECT CASE WHEN Table0001.pk = 1234 THEN '1234' ELSE '4321' END FROM Table0001";
        Val<String> _then = new Val<>("1234");
        Val<String> _else = new Val<>("4321");
        Case _case = new Case(t1.pk.eq( 1234L), _then, _else);
        String actual = new Select( _case )
                .from( t1 ).formatted();
        check(message, expected0001, actual);
        
        String expected0002 = "SELECT CASE WHEN Table0001.pk = 1234 THEN '1234' ELSE '4321' END fakeName FROM Table0001";
        _case = new Case(t1.pk.eq( 1234L), _then, _else).as( "fakeName", Case.class);
        actual = new Select( _case ).from( t1 ).formatted();
        check(message, expected0002, actual);
        
        t1.as( "t1");
        String expected0003 = "SELECT CASE WHEN t1.pk = 1234 THEN '1234' ELSE '4321' END fakeName FROM Table0001 t1";
        _case = new Case(t1.pk.eq( 1234L), _then, _else).as( "fakeName", Case.class);
        actual = new Select( _case ).from( t1 ).formatted();
        check(message, expected0003, actual);
        
        String expected0004 = "SELECT CASE WHEN t1.pk = 1234 THEN '1234' ELSE t1.name END fakeName FROM Table0001 t1";
        _case = new Case(t1.pk.eq( 1234L), _then, t1.name).as( "fakeName", Case.class);
        actual = new Select( _case ).from( t1 ).formatted();
        check(message, expected0004, actual);

    }
    
    public void testFunctions(){
        Table0001 t1 = new Table0001();
        
        String expected0001 = "SELECT AVG( Table0001.pk ) FROM Table0001";
        String actual = t1.select( AVG.of( t1.pk ) ).formatted();
        assertEquals(expected0001, actual);
        
        String expected0002 = "SELECT AVG( Table0001.pk ) FROM Table0001";
        actual = t1.select( AVG.of( t1.pk.as( "FAIL") ) ).formatted();
        assertEquals(expected0002, actual);

        String expected0003 = "SELECT AVG( Table0001.pk ) avgpk FROM Table0001";
        actual = t1.select( AVG.of( t1.pk ).as( "avgpk" ) ).formatted();
        assertEquals(expected0003, actual);
        
        String expected0004 = "SELECT DISTINCT Table0001.pk FROM Table0001";
        actual = t1.select( DISTINCT.of( t1.pk ) ).formatted();
        assertEquals(expected0004, actual);
        
        String expected0005 = "SELECT DISTINCT Table0001.pk avgpk FROM Table0001";
        actual = t1.select( DISTINCT.of( t1.pk ).as( "avgpk" ) ).formatted();
        assertEquals(expected0005, actual);
        
        String expected0006 = "SELECT DISTINCT Table0001.pk, Table0001.name FROM Table0001";
        actual = t1.select( DISTINCT.of( t1.pk ), t1.name ).formatted();
        assertEquals(expected0006, actual);
        
    }
    
    public void testValueExpr(){
        String message = "Value Expression mismatch: expected '%s', actual '%s'";
        
        Table0001 t1 = new Table0001();
        
        String expected0001 = "SELECT ( 'Name: ' || Table0001.name ) FROM Table0001";
        Val<String> _name = new Val<>("Name: ");
        ValExpr expr = new ValExpr( Op.concat( _name, t1.name) );
        String actual = new Select( expr )
                .from( t1 ).formatted();
        check(message, expected0001, actual);
        
        _name = new Val<>("Name: ").as( "name");
        expr = new ValExpr( Op.concat( _name, t1.name.as( "error")) );
        actual = new Select( expr )
                .from( t1 ).formatted();
        check(message, expected0001, actual);
        
        String expected0002 = "SELECT ( 'Name: ' || Table0001.name ) vname FROM Table0001";
        _name = new Val<>("Name: ").as( "name");
        expr = new ValExpr( Op.concat( _name, t1.name) ).as( "vname");
        actual = new Select( expr )
                .from( t1 ).formatted();
        check(message, expected0002, actual);
        
        String expected0003 = "SELECT ( 'Name: ' || t1.name ) FROM Table0001 t1";
        expr = new ValExpr( Op.concat( _name, t1.name) );
        actual = new Select( expr )
                .from( t1.as( "t1" ) ).formatted();
        check(message, expected0003, actual);
    }
    
    void check(String message, String expected, String actual){
        assertTrue(String.format( message, expected, actual), expected.equals( actual));
    }
}
