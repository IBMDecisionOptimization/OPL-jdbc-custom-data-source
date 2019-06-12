# Run sample with MS SQL Server

## Build the sample

Before you build the sample, you must edit `build.properties` for the appropriate path locations:

* If you want to run the sample with MySQL, `mysql.jdbc.connector.path` should point to your JDBC driver location.
* If you want to run the sample with IBM DB2, `db2.jdbc.connector.path` should point to your JDBC driver location.
* `opl.home` should point to your OPL home, unless you have a `CPLEX_STUDIO_DIR128` set. (this variable should exists if you installed on a Windows machine).

The build file, `build.xml`, imports the build file from the OPL samples,
in `<opl home>/examples/opl_interfaces/java/build_common.xml`.
This build file defines all variables that are needed to configure the execution.

The example is compiled using the `compile` Ant target:
```
ant compile
```
The example is automatically compiled with the run Ant targets is invoked.

## Setup the sample database

To run the sample with MS SQL Server, you need to install MS SQL Server. 

In a <em>Commnad Prompt</em> window:

Provided your sql server instance name is SQLEXPRESS, create database using:

```
C:\>sqlcmd -S .\SQLEXPRESS -i data\oil_mssql.sql
```

Before you run the sample, you need to edit `build.properties` to make `sqlserver.jdbc.connector.path` point
to your MSSQL server jdbc driver (i.e. mssql-jdbc-7.2.2.jre8.jar)

Edit `data\db_mssql.xml` for your JDBC connection string and credentials.
Your connection string looks like `	jdbc:sqlserver://localhost;instanceName=<instance>;databaseName=<database_name>;integratedSecurity=true`

where `instance` is the mssql instance name (default is SQLEXPRESS), `<database_name>` is the name
of your database (default is `custom_data_source`).

## Run the sample
Compile and run the sample for MS SQL Server:

```
$ ant run_mssql
```

* Uses data/oil.mod as a model file
* Uses data/oil.dat as a data file
* Uses data/db_db2.xml to customize the JDBC custom data source.