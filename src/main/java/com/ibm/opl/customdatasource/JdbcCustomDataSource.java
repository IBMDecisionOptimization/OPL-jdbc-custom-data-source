package com.ibm.opl.customdatasource;

import ilog.opl.IloCustomOplDataSource;
import ilog.opl.IloOplDataHandler;
import ilog.opl.IloOplElement;
import ilog.opl.IloOplElementDefinition;
import ilog.opl.IloOplElementDefinitionType.Type;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplTupleSchemaDefinition;
import ilog.opl_core.cppimpl.IloTupleSchema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;

/**
 * An custom data source reading data using JDBC.
 *
 */
class JdbcCustomDataSource extends IloCustomOplDataSource {
    private JdbcConfiguration _configuration;
    private IloOplModelDefinition _def;

    /**
     * Creates a new JDBC custom data source, based on the specified configuration.
     * 
     * @param configuration The JDBC data source configuration.
     * @param oplF The OPL factory.
     * @param def The OPL Model definition.
     */
    public JdbcCustomDataSource(JdbcConfiguration configuration, IloOplFactory oplF, IloOplModelDefinition def) {
        super(oplF);
        _configuration = configuration;
        _def = def;
    }

    void fillNamesAndTypes(IloTupleSchema schema, String[] names, Type[] types) {
        IloOplElementDefinition elementDefinition = _def.getElementDefinition(schema.getName());
        IloOplTupleSchemaDefinition tupleSchema = elementDefinition.asTupleSchema();
        for (int i = 0; i < schema.getSize(); i++) {
            String columnName = schema.getColumnName(i);
            types[i] = tupleSchema.getComponent(i).getElementDefinitionType();
            names[i] = columnName;
        }
    }

    /**
     * Overrides the IloCustomOplDataSource method to read data when the model
     * is generated.
     */
    public void customRead() {
        System.out.println("Reading elements from database");
        Properties prop = _configuration.getReadQueries();
        Enumeration<?> propertyNames = prop.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String name = (String) propertyNames.nextElement();
            String query = prop.getProperty(name);
            System.out.println("Reading " + name + " using \"" + query + "\"");
            customRead(name, query);
        }
        System.out.println("Done");
    }

    public void customRead(String name, String query) {
        IloOplElementDefinition def = _def.getElementDefinition(name);
        Type type = def.getElementDefinitionType();
        Type leaf = def.getLeaf().getElementDefinitionType();

        if (type == Type.SET) {
            if (leaf == Type.TUPLE) {
                readTupleSet(name, query);
            } else {
                readSet(leaf, name, query);
            }
        }
    }

    public void readSet(Type leaf, String name, String query) {
        IloOplDataHandler handler = getDataHandler();
        try {
            Connection conn = DriverManager.getConnection(_configuration.getUrl(),
                    _configuration.getUser(),
                    _configuration.getPassword());
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            handler.startElement(name);
            handler.startSet();

            while (rs.next()) {
                if (leaf == Type.INTEGER)
                    handler.addIntItem(rs.getInt(1));
                else if (leaf == Type.FLOAT)
                    handler.addNumItem(rs.getFloat(1));
                else if (leaf == Type.STRING)
                    handler.addStringItem(rs.getString(1));
            }
            handler.endSet();
            handler.endElement();
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void readTupleSet(String name, String query) {
        IloOplDataHandler handler = getDataHandler();
        try {
            IloOplElement elt = handler.getElement(name);
            ilog.opl_core.cppimpl.IloTupleSet tupleSet = (ilog.opl_core.cppimpl.IloTupleSet) elt.asTupleSet();
            IloTupleSchema schema = tupleSet.getSchema_cpp();
            int size = schema.getTotalColumnNumber();

            String[] oplFieldsName = new String[size];
            Type[] oplFieldsType = new Type[size];

            fillNamesAndTypes(schema, oplFieldsName, oplFieldsType);

            Connection conn = DriverManager.getConnection(_configuration.getUrl(),
                    _configuration.getUser(),
                    _configuration.getPassword());
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            handler.startElement(name);
            handler.startSet();
            while (rs.next()) {
                handler.startTuple();
                for (int column = 0; column < oplFieldsName.length; column++) {
                    String columnName = oplFieldsName[column];
                    if (oplFieldsType[column] == Type.INTEGER) {
                        handler.addIntItem(rs.getInt(columnName));
                    } else if (oplFieldsType[column] == Type.FLOAT) {
                        handler.addNumItem(rs.getFloat(columnName));
                    } else if (oplFieldsType[column] == Type.STRING) {
                        handler.addStringItem(rs.getString(columnName));
                    }
                }
                handler.endTuple();
            }
            handler.endSet();
            handler.endElement();
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
};
