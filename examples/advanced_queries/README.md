# Advanced queries

This sample shows how advanced SQL queries and statements can be used to
create database. This version offers the most flexibility for outputs to database.

# Setup

Before you run the sample, you will need to edit [oil.dat](oil.dat), and configure
paths. In particular, you want to add the JDBC connector jar, for instance:

```
  // Update this to point to your jdbc driver.
  // For instance mssql-jdbc-7.2.2.jre8.jar for mssql
  IloOplImportJava("../../external_libs/mssql-jdbc-7.2.2.jre8.jar")
```

And you want to specify the configuration file for your database connection:

```
  // This is the configuration file for accessing your database.
  var jdbcConfigXml = "../../data/db_mssql.xml";
```

Lastly, you want to point to the jdbc custom data source jar.
It is distributed as binaries in the [lib](../../lib) directory:

```
  // The jar containing the jdbc custom data source
  IloOplImportJava("../../lib/jdbc-custom-data-source.jar");
```

# Customize table creation

Instead of using default table creation, this sample relies on SQL statement executed when the model is run.
The output table is dropped first, then we create a table using a SQL statement:

```
  config.execute("DROP TABLE result");
  config.execute("CREATE TABLE result(oil VARCHAR(30), gas VARCHAR(30), blend FLOAT, a FLOAT)");
```

Then we configure the JdbcWriter to use an insert statement:

```
  config.addInsertStatement("Result", "INSERT INTO result(oil, gas, blend, a) VALUES (?,?,?,?)");
```

# Running the sample

To run the sample, invoke the `run.bat` script:


```
[P36] D:\>
run.bat
Buildfile: D:\opl\OPL-jdbc-custom-data-source\examples\ilo_opl_call_java\build.xml

platform:

run:
     [exec]
     [exec] <<< setup
     [exec]
     [exec] Reading elements from database
     [exec] Reading Gasolines using "SELECT NAME FROM GASDATA"
     [exec] Reading OilData using "SELECT * FROM OILDATA"
     [exec] Reading Oils using "SELECT NAME FROM OILDATA"
     [exec] Reading GasData using "SELECT * FROM GASDATA"
     [exec] Done (0.129 s)
     [exec]
     [exec] <<< generate
     [exec]
     [exec] Tried aggregator 1 time.
     [exec] LP Presolve eliminated 1 rows and 0 columns.
     [exec] Reduced LP has 12 rows, 12 columns, and 43 nonzeros.
     [exec] Presolve time = 0.00 sec. (0.01 ticks)
     [exec]
     [exec] Iteration log . . .
     [exec] Iteration:     1   Scaled dual infeas =             0.000000
     [exec] Iteration:     2   Dual objective     =        434000.000000
     [exec]
     [exec] <<< solve
     [exec]
     [exec]
     [exec] OBJECTIVE: 287750
     [exec] Result =  {<"Crude1" "Diesel" 800 0> <"Crude1" "Regular" 2111.1 750>

     [exec]      <"Crude1" "Super" 2088.9 0> <"Crude2" "Diesel" 0 0>
     [exec]      <"Crude2" "Regular" 4222.2 750> <"Crude2" "Super" 777.78 0>
     [exec]      <"Crude3" "Diesel" 200 0> <"Crude3" "Regular" 3166.7 750>
     [exec]      <"Crude3" "Super" 133.33 0>}
     [exec] Writing elements to database
     [exec] Writing Result
     [exec] Done (0.047 s)
     [exec]
     [exec] <<< post process
     [exec]
     [exec]
     [exec] <<< done
     [exec]

BUILD SUCCESSFUL
```
