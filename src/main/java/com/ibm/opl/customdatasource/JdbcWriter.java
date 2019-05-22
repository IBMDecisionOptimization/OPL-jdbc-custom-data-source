package com.ibm.opl.customdatasource;

import ilog.concert.IloTuple;
import ilog.opl.IloOplElement;
import ilog.opl.IloOplElementDefinition;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplElementDefinitionType.Type;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplTupleSchemaDefinition;
import ilog.opl_core.cppimpl.IloTupleSchema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;

/**
 * The class to write data using JDBC.
 *
 */
public class JdbcWriter {
    private JdbcConfiguration _configuration;
    private IloOplModelDefinition _def;
    private IloOplModel _model;
    
    public static void writeOutput(JdbcConfiguration config, IloOplModel model) {
      IloOplFactory factory = IloOplFactory.getOplFactoryFrom(model);
      IloOplModelDefinition definition = model.getModelDefinition();
      JdbcWriter writer = new JdbcWriter(config, definition, model);
      writer.customWrite();
  }

    public JdbcWriter(JdbcConfiguration configuration, IloOplModelDefinition def, IloOplModel model) {
        _configuration = configuration;
        _def = def;
        _model = model;
    }

    public void customWrite() {
        System.out.println("Writing elements to database");
        Properties prop = _configuration.getWriteMapping();
        Enumeration<?> propertyNames = prop.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String name = (String) propertyNames.nextElement();
            String table = prop.getProperty(name);
            System.out.println("Writing " + name + " using table " + table + "");
            customWrite(name, table);
        }
        System.out.println("Done");
    }

    static final String CREATE_QUERY = "CREATE TABLE %(";

    String createTableQuery(IloTupleSchema schema, String table) {
        String query = CREATE_QUERY.replace("%", table);
        IloOplElementDefinition elementDefinition = _def.getElementDefinition(schema.getName());
        IloOplTupleSchemaDefinition tupleSchema = elementDefinition.asTupleSchema();
        for (int i = 0; i < schema.getSize(); i++) {
            String columnName = schema.getColumnName(i);
            Type type = tupleSchema.getComponent(i).getElementDefinitionType();
            query += columnName;
            query += " ";
            if (type == Type.INTEGER)
                query += "INT";
            else if (type == Type.FLOAT)
                query += "FLOAT";
            else if (type == Type.STRING)
                query += "VARCHAR(30)";
            if (i < (schema.getSize() - 1))
                query += ", ";
        }
        query += ")";
        System.out.println("Create query = " + query);
        return query;
    }

    static final String INSERT_QUERY = "INSERT INTO %(";

    String getPlaceholderString(int size) {
      StringBuffer b = new StringBuffer();
      for (int i=0; i < size-1; i++)
        b.append("?,");
      b.append("?");
      return b.toString();
    }
    
    String getInsertQuery(IloTupleSchema schema, String table) {
      String query = INSERT_QUERY.replace("%", table);
      for (int i = 0; i < schema.getSize(); i++) {
          String columnName = schema.getColumnName(i);
          query += columnName;
          if (i < (schema.getSize() - 1))
              query += ", ";
      }
      query += ") VALUES(" + getPlaceholderString(schema.getSize()) + ")";
      return query;
    }
    
    void updateValues(IloTuple tuple, IloTupleSchema schema, 
                      IloOplTupleSchemaDefinition tupleSchemaDef, PreparedStatement stmt) throws SQLException {
      for (int i = 0; i < schema.getSize(); i++) {
          int index = i+1;  // index in PreparedStatement
          Type columnType = tupleSchemaDef.getComponent(i).getElementDefinitionType();
          if (columnType == Type.INTEGER)
              stmt.setInt(index, tuple.getIntValue(i));
          else if (columnType == Type.FLOAT)
              stmt.setDouble(index, tuple.getNumValue(i));
          else if (columnType == Type.STRING)
              stmt.setString(index, tuple.getStringValue(i));
      }
    }

    static final String DROP_QUERY = "DROP TABLE %";

    void customWrite(String name, String table) {
        IloOplElementDefinition def = _def.getElementDefinition(name);
        Type type = def.getElementDefinitionType();
        Type leaf = def.getLeaf().getElementDefinitionType();

        IloOplElement elt = _model.getElement(name);
        ilog.opl_core.cppimpl.IloTupleSet tupleSet = (ilog.opl_core.cppimpl.IloTupleSet) elt.asTupleSet();
        IloTupleSchema schema = tupleSet.getSchema_cpp();
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(_configuration.getUrl(), _configuration.getUser(),
                    _configuration.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            // drop existing table
            try {
                sql = DROP_QUERY.replaceFirst("%", table);
                // System.out.println(sql);
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                // table does not exists,
            }
            // create table using tuple fields
            // first create query
            sql = createTableQuery(schema, table);
            // System.out.println(sql);
            stmt.executeUpdate(sql);
            
            try {
              IloOplElementDefinition tupleDef = _def.getElementDefinition(schema.getName());
              IloOplTupleSchemaDefinition tupleSchemaDef = tupleDef.asTupleSchema();
              
              conn.setAutoCommit(false); // begin transaction
              String psql = getInsertQuery(schema, table);
              PreparedStatement pstmt = conn.prepareStatement(psql);
              // iterate the set and create the final insert statement
              for (java.util.Iterator it1 = tupleSet.iterator(); it1.hasNext();) {
                  IloTuple tuple = (IloTuple) it1.next();
                  updateValues(tuple, schema, tupleSchemaDef, pstmt);
                  pstmt.executeUpdate();
              }
              conn.commit();
            } catch (SQLException e) {
              conn.rollback();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
          if (conn != null)
            try {
              conn.close();
            } catch (SQLException e) {
              e.printStackTrace();
            }
        }
    }
}