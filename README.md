0RM (or ZeroRM) [![Build Status](https://travis-ci.org/slaclab/zerorm.svg?branch=master)](https://travis-ci.org/slaclab/zerorm)
======
I'm sure there's plenty of things I'm doing stupidly in this. Feel free to submit pull requests.  

#### You should use this if:  
You don't want to use an ORM, but you do want some semblance of DRY  
You want something that's *type safer* for dynamically generating SQL  
You need something that will let you easily translate DSL's Abstract Syntax Tree into SQL  

#### You should NOT use this if:
You need something for production (seriously, the maven version is 0.1-SNAPSHOT)

## More Reasons?  
1. Budgets are limited, you can't buy a new database when queries on 100m rows go slow
  - Sometimes you need to hand-tweak that SQL
  - Remember that time the RDBMS optimizer had a brain fart, and nobody told the ORM?
2. You want the ability to:
  - parse Abstract Syntax Trees to SQL expressions easily
    - and maybe create complex, type-safer SQL expressions
  - pre-process parts of your SQL at compile/bind-time
    - you can override the `formatted()` method on individual `Formattable` objects
    - or, more generally, create a new `AbstractSQLFormatter`
3. You like: 
  - SQL and Java
    - but hate seeing super long SQL strings in code
    - and think it would be nice to be able to parameterize parts of it
    - Sometimes like defining things on the fly
      - `new Select( $("name") ).from( "animal" ).where( $("name").eq( param ) );`
  - Readable code
  - Extendable code
4. You don't like: 
  - ORMs all that much
  - Or this: `stmt.setLong(1, 1234L)`, `stmt.setXXX(2, something)`
    - But you've got your own custom mappers anyhow.
  - huge jars
  - lots of dependencies
  - complicated code, too man LoC, etc...

### Type Safer

It's mostly type-safe, thanks to generating SQL that you is fed into `connection.prepareStatement()`.  

The most dangerous class to use is `Sql`, because it's largely intended for throwing raw strings into your statements. `$( colName )` is also unsafe in the sense that whatever `colName` is will be thrown out directly to SQL. However, `$$( colName )` is a bit safer because it removes double quotes and wraps the identifier (ANSI_QUOTES mode in MySQL), at the expense of requiring the case be completely correct in most cases.

#### Improvements
There's quite a bit of improvement we could make to this.  
Things are missing from SQL-9[29], but hopefully most of it is easily implemented.  

#### Inspirations
SQL-92/99 BNF Grammars : http://savage.net.au/SQL  
Korma: http://sqlkorma.com  
jOOq: http://jooq.org  
ORMLite : http://ormlite.com  

### Contact
Feel free to contact me:  
brianv .at. stanford.edu

## Not unrealistic code examples  
```java
public static class Animal extends Table {                   // Uses class name for table
    @Schema(name = "id")         public Column pk;           // Unchecked columns
    @Schema(name = "species")    public Column version;
    @Schema(name = "subspecies") public Column revision;
    
    public Animal(){ super(); }                              // super does some magic
};
```  
```java
@Schema(name = "pet")
public static class AnimalInstance extends Table {
    @Schema(name = "id")         public Column<Long> pk;     // Checked columns
    @Schema(name = "type")       public Column<Long> animal;
    @Schema                      public Column<String> name; // uses field name
    @Schema(name="mother")       public Column<Long> parent;
    @Schema(name = "status")     public Column<String> status;
    
    public AnimalInstance(){ super(); }
};
```  
```java
Animal anml = new Animal();
AnimalInstance pet = new AnimalInstance();

public void testExampleJoins(){

    Select simpleJoin = animalPet();                        /* 1 */

    Select pet_1234 = animalPet()
        .where( pet.id.eq( 1234L ) );                       /* 2 */

    Param<String> nameParam = pet.name.checkedParam();
    Select animalsNamed = animalPet()
        .where( pet.name.eq(nameParam) );                   /* 3 */
    nameParam.setValue( "Lucy" ); 
    nameParam.setValue( "Spike" );
    
    // How this might play out...
    try(PreparedStatement stmt = animalsNamed.prepareAndBind( getConnection() ){
      ResultSet rs = stmt.executeQuery();
      while(rs.next(){
        // do Something fancy
      }
    } catch (SQLException ex){ 
      /* nah, our pet database is purrfect */ 
    }
    
    // Redefine selections at runtime
    Select distNames = animalPet()
        .clearSelections()
        .selection( DISTINCT.of( pet.name ) )
        .selection( anml.getColumns() );
    
    // Dynamically wrap selections into new ones
    Select dn = new Select( distNames.getColumns() )        /* 4 */
        .from( distNames.as("dn") );
    
    // Add hooks that will be executed before the SQL is compiled
    Select stageFirst = new Select(){
            @Override
            public String formatted(){
               fillTempTableFirst();
               setupComplexTransaction();
               return super.formatted(); 
            }
        }.selection( $("externalKey") )
        .from( "tempTable" );
}

// Animals who have died in captivity...
public void deadPets(){
    ArrayList<String> aliveStates = new ArrayList<>();
    aliveStates.add( "SLEEPING" );
    aliveStates.add( "AWAKE" );
    aliveStates.add( "IN_UTERO" );
    
    Select deadPets = animalPet()
        .where( pet.status.not_in( aliveStates ) );       /* 5 */
}

Select animalPet(){
    // Only want the animal columns, not the pet columns
    return new Select( anml.getColumns() )
        .from( anml )
        .join( pet, anml.pk.eq( pet.animal ) );
}
```

When formatted, the above `Select` statements would produce:
```sql
-- 1
SELECT Animal.id, Animal.species, Animal.subspecies 
  FROM Animal 
  JOIN pet ON ( Animal.id = pet.type );

-- 2
SELECT Animal.id, Animal.species, Animal.subspecies 
  FROM Animal 
  JOIN pet ON ( Animal.id = pet.type ) 
  WHERE pet.id = 1234;

-- 3
SELECT Animal.id, Animal.species, Animal.subspecies 
  FROM Animal 
  JOIN pet ON ( Animal.id = pet.type ) 
  WHERE pet.name = ?;                             -- 'SPIKE'

-- 4
SELECT dn.name, dn.id, dn.species, dn.subspecies 
  FROM  ( SELECT DISTINCT pet.name, Animal.id, 
    Animal.species, Animal.subspecies 
    FROM Animal 
    JOIN pet ON ( Animal.id = pet.type ) ) dn

-- 5
SELECT Animal.id, Animal.species, Animal.subspecies 
  FROM Animal 
  JOIN pet ON ( Animal.id = pet.type ) 
  WHERE pet.status NOT IN (?,?,?);                -- ('SLEEPING','AWAKE','IN_UTERO')
```


## Current version

This code may not be thread safe, that hasn't been thorougly tested.

Sorry it's not that well documented. Hopefully the readability and simplicity make up for that

There's only runtime exceptions, and not too many of them. Typically they occur only when it is
known that the compiled SQL would throw an exception if it was executed. One exception being 
that you aren't allowed to `DELETE FROM TableName` without unprotected the `Delete` statement.
The other cases are bind-time exceptions:
- When Selecting from another `Select` statement, the other statement must have a non-empty alias
- When joining a table, the select statement of the join walks through the join expression.
  -  If it finds that a `Column` with a non-null parent isn't defined in FROM or JOIN parts,
     it throws an exception.
- Type safer parameter binding
- A few other cases (check the code)

### Testing

There are some unit tests. There should be more. I have other tests which exercise the library 
more that I typically run, but still, more unit tests are needed.

I have used the `Select` class in code quite a bit. Not so much the `Insert`, `Update`, `Delete`.
Proceed with extra caution for these.

