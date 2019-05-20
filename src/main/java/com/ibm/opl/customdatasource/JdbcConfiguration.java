package com.ibm.opl.customdatasource;

import java.util.Enumeration;
import java.util.Properties;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;

/**
 * The class to store JDBC custom data source connection parameters.
 */
public class JdbcConfiguration {
    Properties _readProperties = new Properties();
    Properties _writeProperties = new Properties();

    private final static String URL = "url";
    private final static String USER = "user";
    private final static String PASSWORD = "password";

    private final static String READ = "read";
    private final static String WRITE = "write";

    private final static String QUERY = "query";
    private final static String NAME = "name";
    private final static String TABLE = "table";
    private final static String TARGET = "target";
    
    private String _url = null;
    private String _user = null;
    private String _password = null;
    
    /**
     * Creates a new JDBC configuration.
     */
    public JdbcConfiguration() {
    }
    
    public String getUrl() {
        return _url;
    }
    
    public void setUrl(String url) {
      _url = url;
    }
    
    public String getUser() {
        return _user;
    }

    public String getPassword() {
        return _password;
    }

    public Properties getReadQueries() {
        return _readProperties;
    }
    
    /**
     * Adds a read query to the datasource.
     * 
     * The specified query is used to populate the OPL data which name is specified.
     * @param name The OPL data
     * @param query The read query
     */
    public void addReadQuery(String name, String query) {
      _readProperties.setProperty(name, query);
    }

    public Properties getWriteMapping() {
        return _writeProperties;
    }
    
    /**
     * Adds a write mapping to the datasource.
     * @param name The OPL output name.
     * @param target The database table to map the output to.
     */
    public void addWriteMapping(String name, String target) {
      _writeProperties.setProperty(name, target);
    }
    
    /**
     * Reads the configuration from the specified file.
     * 
     * Supported files are .properties and .XML files.
     * See also {@link #readXML(InputStream)} and {@link #readProperties(InputStream)}
     * @param filename The configuration file name.
     * @throws IOException
     */
    public void read(String filename) throws IOException {
        InputStream input = new FileInputStream(filename);
        try {
            if (filename.toLowerCase().endsWith(".properties"))
                this.readProperties(input);
            else if (filename.toLowerCase().endsWith(".xml"))
                this.readXML(input);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    /**
     * Read and parse JDBC configuration from the specified property file.
     * 
     * Example:
     * <pre>
     * # The connection string
     * # The default url connects to mysql on default port, using database
     * # 'custom_data_source'
     * url=jdbc:mysql://localhost:3306/custom_data_source?useSSL=false
     * 
     * # Your connection credentials
     * user=sql_user
     * password=mysql
     * 
     * # Read queries. Those are the SQL queries to create OPL data sets.
     * # Each read query has the form: read.<table name>
     * read.Gasolines=SELECT name FROM GasData
     * read.Oils=SELECT name FROM OilData
     * read.GasData=SELECT * FROM GasData
     * read.OilData=SELECT * FROM OilData
     * 
     * # Result table name. This define the name of the table to write results in.
     * # table will be dropped.
     * # table will be created with fields names and types.
     * # table will be updated.
     * # Elements must be a tuplesets.
     * write.Result=result
     * </pre>
     * @param input The InputStream to read the configuration from
     * @throws IOException If an exception occurs while reading the configuration
     */
    public void readProperties(InputStream input) throws IOException {
        Properties properties = new Properties();
        properties.load(input);

        this._url = properties.getProperty(URL);
        this._user = properties.getProperty(USER);
        this._password = properties.getProperty(PASSWORD);
        
        // iterate properties to find read and write
        Enumeration<?> propertyNames = properties.propertyNames();
        String read = READ + ".";
        String write = WRITE + ".";
        while (propertyNames.hasMoreElements()) {
            String name = (String) propertyNames.nextElement();
            if (name.startsWith(read)) {
                int pos = read.length();
                String element = name.substring(pos);
                _readProperties.setProperty(element, (String) properties.getProperty(name));
            } else if (name.startsWith(write)) {
                int pos = write.length();
                String element = name.substring(pos);
                _writeProperties.setProperty(element, (String) properties.getProperty(name));
            }
        }
    }

    /**
     * Read and parse JDBC configuration from the specified XML file.
     *
     * Example:
     * <pre>
     * {@code
     * <datasource>
     *     <!-- The connection string
     *          The default url connects to mysql on default port, using database
     *          'custom_data_source'
     *     -->
     *     <url>jdbc:mysql://localhost:3306/custom_data_source?useSSL=false</url>
     * 
     *     <!-- Your connection credentials -->
     *     <user>root</user>
     *     <password>mysql</password>
     * 
     *     <!-- The read queries
     *          The name attribute is used to populate the corresponding Data Element.
     *     -->
     *     <read>
     *         <query name="Gasolines">SELECT name FROM GasData</query>
     *         <query name="Oils"> SELECT name FROM OilData</query>
     *         <query name="GasData">SELECT * FROM GasData</query>
     *        <query name="OilData">SELECT * FROM OilData</query>
     *     </read>
     *  
     *     <!-- The output table mapping.
     *          This mapping define how output data sets are exported to the database.
     *     -->
     *     <write>
     *         <!-- This maps the output dataset "Result" to the "result" table -->
     *         <table name="Result" target="result"/>
     *     </write>
     * </datasource>
     * }
     * </pre>
     * @param input The InputStream to read the configuration from
     * @throws IOException If an exception occurs while reading the configuration
     * @throws RuntimeException if an XML parse exception occurs.
     */
    public void readXML(InputStream input) throws IOException {
        DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(input);
            Element root = doc.getDocumentElement();
            // connection parameters
            _url = doc.getElementsByTagName(URL).item(0).getTextContent();
            _user = doc.getElementsByTagName(USER).item(0).getTextContent();
            _password = doc.getElementsByTagName(PASSWORD).item(0).getTextContent();
            
            // input parameters
            Node readNode = doc.getElementsByTagName(READ).item(0);
            if (readNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList queries = ((Element)readNode).getElementsByTagName(QUERY);
                for (int iquery = 0; iquery < queries.getLength(); iquery++) {
                    Node qNode = queries.item(iquery);
                    if (qNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element qElement = (Element)qNode;
                        String name = qElement.getAttribute(NAME);
                        String query = qElement.getTextContent();
                        _readProperties.setProperty(name, query);
                    }
                }
            }
            
            // write parameters
            Node writeNode = doc.getElementsByTagName(WRITE).item(0);
            if (writeNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList tables = ((Element)writeNode).getElementsByTagName(TABLE);
                for (int itable = 0; itable < tables.getLength(); itable++) {
                    Node tNode = tables.item(itable);
                    if (tNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element tElement = (Element)tNode;
                        String name = tElement.getAttribute(NAME);
                        String target = tElement.getAttribute(TARGET);
                        _writeProperties.setProperty(name, target);
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException("Could not read XML configuration");
        }
    }
}