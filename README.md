0RM (or ZeroRM)
======
You should use this if you don't want to use an ORM, but you do want some semblance of DRY.  
It's also useful for mapping Abstract Syntax Trees to SQL Expressions.  
You want something that's *type safer* for dynamically generating SQL.

I'm sure there's plenty of things I'm doing stupidly in this. Feel free to submit pull requests.

# Why?

1. Budgets are limited, you can't buy a new database when queries on 100m rows go slow
  - Sometimes you need to hand-tweak that SQL
  - Remember that time the RDBMS optimizer had a brain fart, and nobody told the ORM?
2. You maybe to easily parse external Abstract Syntax Trees to SQL expressions easily
  - and maybe create complex, type-safer SQL expressions
3. You like 
  - SQL and Java
    - but hate seeing super long SQL strings in code
    - and think it would be nice to be able to parameterize parts of it
    - Sometimes like defining things on the fly
      - `new Select( $("name") ).from( $("animal") ).where( $("name").eq( param ) );`
  - Readable code
    - there's still a lot of boilerplate, would love to remove lines
  - Extendable code (hopefully this works out well...)
4. I don't like:
  - ORMs all that much
  - `stmt.setLong(1, 1234L)`, `stmt.setXXX(2, something)`, etc...
    - You've got your own custom mappers anyhow.
  - huge jars
  - lots of dependencies
  - complicated code, classes over 600 lines, tons of classes

### Type Safer

It's mostly type-safe, but I don't guarantee it, and there's ways of overriding. The most dangerous class to use is `Sql`, because it's largely intended for throwing raw strings into your statements. `$( colName )` is also unsafe in the sense that whatever `colName` is will be thrown out directly to SQL. However, `$$( colName )` is a bit safer because it removes double quotes and wraps the identifier (ANSI_QUOTES mode in MySQL), at the expense of requiring the case be completely correct in most cases.

### Inspiration
This code has been inspired by:  
SQL-92/99 BNF Grammars : http://savage.net.au/SQL  
Korma: http://sqlkorma.com  
jOOq: http://jooq.org  
ORMLite : http://ormlite.com  

### Improvements
There's quite a bit of improvement we could make to this.  
`DISTINCT` isn't implemented. You can sort of implement it by modifying a column name and adding a space.  
Lots of other things are missing, but hopefully this structure works out good.  

### Contact
This is my first open-sourced library, but I work on lots of other things I'm happy to share. 
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
}

// Animals who have died in captivity...
public void deadPets(){
    ArrayList<String> aliveStates = new ArrayList<>();
    aliveStates.add( "SLEEPING" );
    aliveStates.add( "AWAKE" );
    aliveStates.add( "IN_UTERO" );
    
    Select deadPets = animalPet()
        .where( pet.status.not_in( aliveStates ) );       /* 4 */
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

