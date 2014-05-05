/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zerorm.core;

import junit.framework.TestCase;
import org.zerorm.core.format.PrettyFormatter;
import org.zerorm.core.model.Table0001;
import org.zerorm.core.model.Table0001x;
import org.zerorm.core.model.Table0002;

/**
 *
 * @author bvan
 */
public class SelectTest extends TestCase {
    public SelectTest(String testName){
        super( testName );
    }
    
    @Override
    protected void setUp() throws Exception{
        super.setUp();
    }
    
    public void testUnionAll(){
        String msg = "Union mismatch. Expected: '%s', actual: '%s'";
        
        Table0001 t0001 = new Table0001();
        Table0001x t0001x = new Table0001x();
        String expected0001 = "SELECT Table0001.pk, Table0001.name FROM Table0001 UNION ALL SELECT Table0001x.pk, Table0001x.name FROM Table0001x";
        String actual = Select
                .unionAll( t0001.selectAllColumns(), t0001x.selectAllColumns())
                .formatted();
        check(msg, expected0001, actual);
        
        String expected0002 = "SELECT Table0001.pk, Table0001.name FROM Table0001 WHERE Table0001.pk = 1234 UNION ALL SELECT Table0001x.pk, Table0001x.name FROM Table0001x WHERE Table0001.pk = 1234";
        actual = Select
                .unionAll( t0001.selectAllColumns(), t0001x.selectAllColumns())
                .where( t0001.pk.eq( 1234L))
                .formatted();
        check(msg, expected0002, actual);
        
        String expected0003 = "SELECT t1_t1x.pk, t1_t1x.name FROM (SELECT Table0001.pk, Table0001.name FROM Table0001 UNION ALL SELECT Table0001x.pk, Table0001x.name FROM Table0001x ) t1_t1x WHERE t1_t1x.pk = 1234";
        Select uni = Select.unionAll( t0001.selectAllColumns(), t0001x.selectAllColumns());
        actual = new Select( uni.as( "t1_t1x").getColumns() )
                .from( uni )
                .where( uni.$("pk").eq( 1234L) )
                .formatted();
        check(msg, expected0003, actual);
        
        PrettyFormatter formatter = new PrettyFormatter();
        //System.out.println( formatter.format( actual ));
    }
    
    void check(String message, String expected, String actual){
        assertTrue(String.format( message, expected, actual), expected.equals( actual));
    }
    
    public void testSelectParams(){
        String msg = "Error when having a sub-select with parameters as a column";
        String expected0001 = "SELECT Table0002.table0001_pk, ( SELECT Table0001.pk FROM Table0001 WHERE Table0001.name = ? ) FROM Table0002";
        Table0001 t1 = new Table0001();
        Select sel0001 = t1
                .select(t1.pk)
                .where( t1.name.eq(t1.name.checkedParam( "name","foo")) );
        Table0002 t2 = new Table0002();
        String actual = t2.select(t2.table0001_pk, sel0001).formatted();
        check(msg, expected0001, actual);
    }
}
