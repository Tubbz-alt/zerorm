0RM (aka zerorm)
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
    class Application extends Table {
        @Schema(name="app")     public Column pk;
        @Schema(name="version") public Column version;
        @Schema(name="appID")   public Column id;

        public Application() { super(); }
    }
    
    @Schema(name="process")
    class Process extends Table {
        @Schema(name="process")  public Column<Long> pk;
        @Schema                  public Column<Long> pid;
        @Schema(name="app")      public Column<Long> parent;
        @Schema(name="pStatus")  public Column<String> status;

        public Process() { super(); }
    }
    
    void exampleJoins(){
        Application app_t = new Application();
        Process process_t = new Process();
        Select simpleJoin = appProcess_t(app_t, process_t);
        System.out.println(app_process_t.formatted());
        
        Select pid_1234 = appProcess_t(app_t, process_t)
          .where(process_t.pid.eq(1234L));
        System.out.println(pid_1234.formatted());
        
        Param<Long> pidParam = p.pid.checkedParam( "pid");
        Select pid_x = appProcess_t(app_t, process_t)
          .where(process_t.pid.eq(pidParam));
        pidParam.setValue( 1234L );
        pidParam.setValue( 4321L );
        System.out.println(pid_x.formatted());
    }
    
    void runningApplications(){
        ArrayList<String> rStates = new ArrayList<>();
        rStates.add("RUNNING");
        rStates.add("BUSY");
        
        Select running = appProcess_t(app_t, process_t)
          .where(process_t.status.in(rStates));
        System.out.println(running.formatted());
    }
    
    Select app_process_join(Application app_t, Process process_t){
        // Only want the application part, not the Process part
        Application app_t = new Application();
        Process process_t = new Process();
        return new Select(app_t.getColumns())
              .from(app_t)
              .join(process_t, app_t.pk.eq(process_t.parent));
    }


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

