# Run sample with DB2

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

To run the sample with DB2. you need to install DB2. DB2 Express-C is a free
community edition of DB2. DB2 Express-C is available on Microsoft Windows,
Linux and Mac OS.

You can download and install DB2 Express-C from [here](https://www.ibm.com/developerworks/downloads/im/db2express/).


In a <em>DB2 Command Window</em>:

Create database using `db2 create database CUSTOMDB`

Run the following SQL script to create and populate the example database:
```
db2 -tvmf data/oil_db2.sql
```

Before you run the sample, you need to edit `build.properties` to make `db2.jdbc.connector.path` point
to your DB2 jdbc driver.

You can download the DB2 jdbc driver [here](http://www-01.ibm.com/support/docview.wss?uid=swg21363866).
Note that if you installed DB2 Express-C, your JDBC driver is `db2jcc4.jar`
in `<DB2 installdir>/SQLLIB/java`.

Edit `data\db_db2.xml` for your JDBC connection string and credentials.
Your connection string looks like `db2://localhost:<port>/<database_name>`
where `port` is the DB2 port (default is 50000), `<database_name>` is the name
of your database (default is `CUSTOMDB`).

## Run the sample
Compile and run the sample for IBM DB2:

```
$ ant run_db2
```

* Uses data/oil.mod as a model file
* Uses data/oil.dat as a data file
* Uses data/db_db2.xml to customize the JDBC custom data source.


