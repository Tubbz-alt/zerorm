package org.zerorm.core;

import java.util.ArrayList;
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
public class AnimalTest
        extends TestCase {

    public static class Animal extends Table {              // Uses class name for table
        @Schema(name = "id")         public Column pk;      // Unchecked columns
        @Schema(name = "species")    public Column version;
        @Schema(name = "subspecies") public Column revision;

        public Animal(){ super(); }
    };

    @Schema(name = "pet")
    public static class AnimalInstance extends Table {
        @Schema(name = "id")         public Column<Long> pk;     // Checked columns
        @Schema(name = "type")       public Column<Long> animal;
        @Schema                      public Column<String> name; // uses field name
        @Schema(name="mother")       public Column<Long> parent;
        @Schema(name = "status")     public Column<String> status;

        public AnimalInstance(){ super(); }
    };

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AnimalTest(String testName){
        super( testName );

    }

    /**
     * Rigourous Expr :-)
     */
    public void testApp(){
        AbstractSQLFormatter.setDefault( "Oracle" ).setDebugParams( true );
        AnimalInstance p = new AnimalInstance();
        Param<Long> pidPar = p.parent.checkedParam( "mother");
        pidPar.setValue( 1234L );

        String s001_expected = "SELECT * FROM pet";
        System.out.println( "Test s001, expected: " + s001_expected );
        Select s001 = (new Select( "*" ).from( new AnimalInstance() )).as( "pi" );
        assertTrue( "Got: " + s001.formatted(), s001.formatted().equals( s001_expected ) );

        String s002_expected = "SELECT * FROM ( SELECT * FROM pet ) pi";
        System.out.println( "Test s002, expected: " + s002_expected );
        Select s002 = new Select( "*" ).from( s001 );
        assertTrue( "Got: " + s002.formatted(), s002.formatted().equals( s002_expected ) );

        AbstractSQLFormatter.getDefault().setDebugParams( true );
        Param<Long> bpParam = new Param<Long>();
        bpParam.setName( "firstparam" );
        bpParam.setValue( new Long( 1235L ) );
    }
    
    Animal anml_t = new Animal();
    AnimalInstance pet_t = new AnimalInstance();

    public void testExampleJoins(){
        String expected0001 = "SELECT Animal.id, Animal.species, Animal.subspecies FROM Animal JOIN pet ON ( Animal.id = pet.type )";
        Select simpleJoin = animalPet_t();
        String actual = simpleJoin.formatted();
        assertEquals( expected0001, actual );
        
        String expected0002 = "SELECT Animal.id, Animal.species, Animal.subspecies FROM Animal JOIN pet ON ( Animal.id = pet.type ) WHERE pet.type = 1234";
        Select pid_1234 = animalPet_t()
                .where( pet_t.animal.eq( 1234L ) );
        actual = pid_1234.formatted();
        assertEquals( expected0002, actual );

        String expected0003 ="SELECT Animal.id, Animal.species, Animal.subspecies FROM Animal JOIN pet ON ( Animal.id = pet.type ) WHERE pet.name = ?";
        Param<String> nameParam = pet_t.name.checkedParam();
        Select pid_x = animalPet_t()
                .where( pet_t.name.eq( nameParam) );
        nameParam.setValue( "Lucy" ); 
        nameParam.setValue( "Spike" );
        actual = pid_x.formatted();  // Only Spike is bound
        assertEquals( expected0003, actual );
    }

    public void testAliveAnimals(){
        ArrayList<String> aliveStates = new ArrayList<>();
        aliveStates.add( "SLEEPING" );
        aliveStates.add( "AWAKE" );
        aliveStates.add( "IN_UTERO" );
        
        String expected =  "SELECT Animal.id, Animal.species, Animal.subspecies FROM Animal JOIN pet ON ( Animal.id = pet.type ) WHERE pet.status IN (?,?,?)";
        Select running = animalPet_t()
                .where( pet_t.status.in( aliveStates ) );
        assertEquals(expected, running.formatted());
    }

    Select animalPet_t(){
        // Only want the animal columns, not the pet columns
        return new Select( anml_t.getColumns() )
                .from( anml_t )
                .join( pet_t, anml_t.pk.eq( pet_t.animal ) );
    }

}
