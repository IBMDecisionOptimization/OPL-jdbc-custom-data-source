# Run sample with MySQL

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
To run the sample with MySQL, you need to install MySQL. MySQL Community Server is a free edition of MySQL.

On Microsoft Windows, you can download and install it from [here](https://dev.mysql.com/downloads/mysql/).

On other plateforms, MySQL Community Server is available with most package
managers. Please refer to the [installation instructions](https://dev.mysql.com/doc/refman/5.7/en/installing.html).

You can check your MySQL installation by running <code>mysqladmin</code>.
This binary would be available in /usr/bin on linux and <msysql install dir>/bin
on Windows.
	  
```
[root@host]# mysqladmin --version
```

Before you run the sample, you need to run the script to create and populate
sample tables.

Edit `data\oil_mysql.sql` for your database name. The default for the script is
to create a new database. If you are not an administrator or if you don't
have the permissions to create database, edit the first lines to use your
database.

Run the script with:

```
$ mysql < data\oil_mysql.sql
```

You also need to download the [JDBC driver for MySQL: MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/)

Once the driver is download and extracted, edit property `jdbc.connector.path` in `build.properties`
to include the MySQL Connector/J `.jar` (should look like `mysql-connector-java-5.1.40-bin.jar`
in your MySQL Connector/J extracted diretory)

Edit `data\db_mysql.xml` for your JDBC connection string and credentials.
Your connection string looks like `jdbc:mysql://localhost:3306/<database_name>?useSSL=false`
where `<database_name>` is the name of your database (default is `custom_data_source`).

## Run the sample

Compile and run the sample for MySQL:

```
$ ant run_mysql
```

* Uses data/oil.mod as a model file
* Uses data/oil.dat as a data file
* Uses data/db_mysql.xml to customize the JDBC custom data source.