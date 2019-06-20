# Run sample with MS SQL Server

## Build the sample

Sample [studio_integration](examples/studio_integration) do not need to be compiled, so you can skip this
section.

Before you build the [oil](examples/oil) sample, you must edit `build.properties` for the appropriate path locations:

* `sqlserver.jdbc.connector.path` should point to your JDBC driver location.
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

To run the sample with MS SQL Server, you need to install Microsoft SQL Server.
After it is installed, you are ready to setup the sample database.

To create a sample database, open a <em>Commnad Prompt</em> window, and Provided your
SQL Server instance name is SQLEXPRESS, create database using:

```
C:\>sqlcmd -S .\SQLEXPRESS -i data\oil_mssql.sql
```

Before you run the sample, you need to download the [Microsoft JDBC Driver for SQL Server](https://docs.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server?view=sql-server-2017).

Once your download finished, decompress the archive. The archive contains jar files that
look like `mssql-jdbc-7.2.2.jre8.jar`. 


Depending on the sample you run, you need to edit `build.properties` to make
`sqlserver.jdbc.connector.path` point to your MSSQL server jdbc driver (i.e. mssql-jdbc-7.2.2.jre8.jar).

In sample [studio_integration](examples/studio_integration), you will need to edit `jdbc.js` to point
to your jdbc driver, *or* add an `OPL_JDBC_DRIVER` environment variable pointing to it:

```
	var jdbc_driver = IloOplGetEnv("OPL_JDBC_DRIVER");
	if (! jdbc_driver ) {
		jdbc_driver = "../../external_libs/mssql-jdbc-7.2.2.jre8.jar";  // default for this project
	}
```

For sample [oil](examples/oil), edit `data\db_mssql.xml` for your JDBC connection string and credentials.
Your connection string looks like `	jdbc:sqlserver://localhost;instanceName=<instance>;databaseName=<database_name>;integratedSecurity=true`

where `instance` is the mssql instance name (default is SQLEXPRESS), `<database_name>` is the name
of your database (default is `custom_data_source`).

## Run the oil sample

Compile and run the sample for MS SQL Server:

```
$ ant run_mssql
```

* Uses data/oil.mod as a model file
* Uses data/oil.dat as a data file
* Uses data/db_mssql.xml to customize the JDBC custom data source.

## Other samples

The other samples do not need to be compiled, and will use `oplrun` executable. Each sample directory
contains a convenience `run.bat` command line to run the sample. Otherwise you run them using the `run` target:

```
$ ant run
```


