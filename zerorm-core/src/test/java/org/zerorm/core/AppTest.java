package org.zerorm.core;

import org.zerorm.core.Expr;
import org.zerorm.core.Val;
import java.util.Arrays;
import java.util.HashMap;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.zerorm.core.Column;
import org.zerorm.core.Delete;
import org.zerorm.core.Insert;
import org.zerorm.core.Op;
import org.zerorm.core.Param;
import org.zerorm.core.Sql;
import org.zerorm.core.Select;
import org.zerorm.core.Table;
import static org.zerorm.core.Op.*;
import org.zerorm.core.Update;
import org.zerorm.core.format.AbstractSQLFormatter;
import org.zerorm.core.interfaces.Schema;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {
    
    public static class Application extends Table {
        @Schema(name="application") public Column pk;
        @Schema(name="version") public Column version;
        @Schema(name="applicationID") public Column id;

        public Application() {
            super();
        }
    };
    
    @Schema(name="process")
    public static class Process extends Table {
        @Schema(name="process") 
        public Column<Long> pk;
        
        @Schema 
        public Column<Long> pid;
        
        @Schema(name="parent")
        public Column<Long> parent;
        
        @Schema(name="processStatus") 
        public Column<String> status;

        public Process() {
            super();
        }
    };

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);

    }

    /**
     * Rigourous Expr :-)
     */
    public void testApp() {
        AbstractSQLFormatter.setDefault("Oracle").setDebugParams( true );
        
        String s001_expected = "SELECT * FROM process";
        System.out.println("Test s001, expected: " + s001_expected);
        Select s001 = (new Select("*").from(new Process())).as("pi");
        assertTrue("Got: " + s001.formatted(), s001.formatted().equals( s001_expected));
        
        String s002_expected = "SELECT * FROM ( SELECT * FROM process ) pi";
        System.out.println("Test s002, expected: " + s002_expected);
        Select s002 = new Select("*").from(s001);
        assertTrue("Got: " + s002.formatted(), s002.formatted().equals( s002_expected));
        
        AbstractSQLFormatter.getDefault().setDebugParams( true );
        Param<Long> bpParam = new Param<Long>();
        bpParam.setName("firstparam");
        bpParam.setValue(new Long(1235L));
    }

}
