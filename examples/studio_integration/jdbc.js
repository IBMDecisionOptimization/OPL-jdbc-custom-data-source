// ----------------------------------------------------------------------------
// Sample material distributed under Apache 2.0 license.
//
// Copyright IBM Corporation 2019. All Rights Reserved.
// ----------------------------------------------------------------------------

//
// Before running this sample, please review and setup the following
//

// OPL_JDBC_DRIVER points to the jar for the jdbc driver you want to use.
var jdbc_driver = IloOplGetEnv("OPL_JDBC_DRIVER");
if (! jdbc_driver ) {
	jdbc_driver = "../../external_libs/mssql-jdbc-7.2.2.jre8.jar";  // default for this project
}

// OPL_JDBC_LIBS points to the directory containing the library needed for this sample.
// You want to put jdbc-custom-data-source.jar there.
var libs = IloOplGetEnv("OPL_JDBC_LIBS");
if (! libs ) {
	libs = "../../lib";  // default value use the lib at the root of this project
}


//
// From this point, nothing is to be edited.
//

// Update this to point to your jdbc driver.
IloOplImportJava(jdbc_driver)



// The jar containing the jdbc custom data source
IloOplImportJava(libs +  "/jdbc-custom-data-source.jar");

function JDBCConnector(url) {
	// Now create JdbcConfiguration
	this.db = IloOplCallJava("com.ibm.opl.customdatasource.JdbcConfiguration", "<init>", "");
	this.db.setUrl(url);
	// add custom data source
	IloOplCallJava("com.ibm.opl.customdatasource.JdbcCustomDataSource",
         "addDataSource", "", this.db, thisOplModel);
	this.read = __JDBCConnector_read;
	this.execute = __JDBCConnector_execute;
	this.update = __JDBCConnector_update;
	return this;
}

function __JDBCConnector_read(name, query) {
	this.db.addReadQuery(name, query);
}
function __JDBCConnector_execute(statement) {
	this.db.execute(statement);
}
function __JDBCConnector_update(name, statement) {
	this.db.addInsertStatement(name, statement);
}