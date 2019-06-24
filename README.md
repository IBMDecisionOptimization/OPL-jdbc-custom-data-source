# IBM Decision Optimization Modeling with OPL Custom data source sample

This sample demonstrates how to use the IBM Decision Optimization Modeling with
OPL custom data source API to import data from a JDBC data source into an OPL model.


This sample illustrates the [Subclassing IloCustomOplDataSoource](https://www.ibm.com/support/knowledgecenter/en/SSSA5P_12.8.0/ilog.odms.ide.help/OPL_Studio/opllanguser/topics/opl_languser_extfunc_datasubcl.html) section from the OPL User's manual.
This sample shows how to read and write tuplesets to/from a database with Java. It also enables you to read a database and generate .dat files to be used in the IDE to prototype your optimization model.

One [variation](examples/ilo_opl_call_java) of this sample shows how to read and write tuplesets to/from a database involving only some scripting in your .dat

This sample comes with example connection configurations to DB2, MySQL and MS SQL Server. It can
be easily adapted to any database that has JDBC drivers.
This example will work with any 12.x OPL version, even if it is configured to run with 12.8.0 version.


## Table of Contents
   - [Prerequisites](#prerequisites)
   - [Build and run the sample](#build-and-run-the-sample)
      - [Run the 'oil' sample](#run-the-oil-sample)
      - [Run the sample from OPL](#run-the-sample-from-opl)
   - [Export plain dat files](#export-plain-dat-files)
   - [Run with another OPL version](#run-with-another-opl-version)
   - [License](#license)   
   
### Prerequisites

1. This sample assumes that IBM ILOG CPLEX Optimization Studio 12.8.0 is
   installed and configured in your environment.

2. Install [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).  
   Once installed, you can check that it is accessible using this command:

	```
	java -version
	```
	
3. Install [Apache ant](http://ant.apache.org/manual/install.html).

4. The sample assumes you have a database with JDBC drivers installed. This
   sample specifically provides instructions for IBM DB2 Express-C and
   MySQL Comunity Server, but is compatible with minimal changes with other JDBC
   compatible databases.

## Build and run the sample

Before you run, you need to populate the database. See details in subsections:

- [Run sample with DB2](README.DB2.md)
- [Run sample with MySQL](README.MySQL.md)
- [Run sample with MS SQL Server](README.SQLServer.md)

### Run the 'oil' sample

The [examples/oil](examples/oil) sample demonstrates how to create a Java application with
a custom data source and invoke OPL from Java.

### Run the sample from OPL

The [examples/studio_integration](examples/studio_integration) sample shows how to
use the jdbc custom data source as a library, without having the need to
invoke OPL runtime from java. You can use this method to access database
using a jdbc-custom-data-source from `oplrun` or OPL Studio.



## Export plain dat files

* When running the `ant` command with the DB2/mysql target, simply add `-Dexport=result.dat` on the command line, and it will export all the tuplesets that have been extracted from the database to `result.dat` file.

## Run with another OPL version

* Edit the build.xml at the root of the directory, and adapt the `example.home` variable to point to your 12.x version.
* Recompile the project

## Limitations

* The custom data source reader supports scalar values, sets and tuplessets. Arrays are not supported.
* Inner tuples are not supported.

## License

This sample is delivered under the Apache License Version 2.0, January 2004 (see LICENSE.txt).
