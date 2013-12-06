0RM (or ZeroRM)
======
This is the first library I've made publicly available.  
I'm sure there's plenty of things I'm doing stupidly. Feel free to submit pull requests.

You should use this if you don't want to use an ORM, but you do want some semblance of DRY.  
It's also useful for mapping Abstract Syntax Trees to SQL Expressions.

# Why?

1. I work in High Energy Physics, and research budgets are limited 
  - we can't buy a new database when queries on 100m rows go slow
  - so I want to be able to fine tune SQL
2. I want to easily parse external Abstract Syntax Trees to SQL expressions easily
  - and dynamically create complex, type-safe SQL expressions
3. I like 
  - SQL
    - but sometimes it's real nice to be able to parameterize it
  - Readable code
    - but there's still a lot of boilerplate, sorry about that.
  - Easily extendable code (hopefully this works out well...)
4. I don't like:
  - ORMs all that much
  - `stmt.setLong(1, 1234L)`, `stmt.setXXX(2, something)`, etc...
  - huge jars
  - lots of dependencies
  - complicated code, classes over 600 lines, tons of classes

It's mostly type-safe. The most dangerous class to use is `Sql`, because it's largely
intended for throwing raw strings into your statements.

This code has been inspired by (in desecending order of inspiration)

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

## Code samples
```java
public static class Animal extends Table {              // Uses class name for table
    @Schema(name = "id")         public Column pk;      // Unchecked columns
    @Schema(name = "species")    public Column version;
    @Schema(name = "subspecies") public Column revision;
    public Animal(){ super(); }                         // Calling super does some magic
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
Animal anml_t = new Animal();
AnimalInstance pet_t = new AnimalInstance();

public void testExampleJoins(){

    Select simpleJoin = animalPet_t();                      /* 1 */

    Select pet_1234 = animalPet_t()
        .where( pet_t.id.eq( 1234L ) );                     /* 2 */

    Param<String> nameParam = pet_t.name.checkedParam();
    Select pid_x = animalPet_t()
        .where( pet_t.name.eq( nameParam) );                /* 3 */
    nameParam.setValue( "Lucy" ); 
    nameParam.setValue( "Spike" );
}

// Apparently animals who have died in captivity...
public void deadPets(){
    ArrayList<String> aliveStates = new ArrayList<>();
    aliveStates.add( "SLEEPING" );
    aliveStates.add( "AWAKE" );
    aliveStates.add( "IN_UTERO" );
    
    Select deadPets = animalPet_t()
        .where( pet_t.status.not_in( aliveStates ) );       /* 4 */
}

Select animalPet_t(){
    // Only want the animal columns, not the pet columns
    return new Select( anml_t.getColumns() )
        .from( anml_t )
        .join( pet_t, anml_t.pk.eq( pet_t.animal ) );
}
```
Produces (in order):  
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
  WHERE pet.name = ?;                                 -- 'SPIKE'

-- 4
SELECT Animal.id, Animal.species, Animal.subspecies 
  FROM Animal 
  JOIN pet ON ( Animal.id = pet.type ) 
  WHERE pet.status NOT IN (?,?,?);                    -- ('SLEEPING','AWAKE','IN_UTERO')
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
- Type safe parameter binding
- A few other cases (check the code)

### Testing

There are some unit tests. There should be more. I have other tests which exercise the library 
more that I typically run, but still, more unit tests are needed.

I have used the `Select` class in code quite a bit. Not so much the `Insert`, `Update`, `Delete`.
Proceed with extra caution for these.

