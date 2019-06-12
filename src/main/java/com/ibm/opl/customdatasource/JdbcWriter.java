package com.ibm.opl.customdatasource;

import ilog.concert.IloTuple;
import ilog.opl.IloOplElement;
import ilog.opl.IloOplElementDefinition;
import ilog.opl.IloOplElementDefinitionType.Type;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplTupleSchemaDefinition;
import ilog.opl_core.cppimpl.IloTupleSchema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import com.ibm.opl.customdatasource.JdbcConfiguration.OutputParameters;

/**
 * The class to write data using JDBC.
 *
 */
public class JdbcWriter {
    private static long DEFAULT_BATCH_SIZE = 10000;
    private JdbcConfiguration _configuration;
    private IloOplModelDefinition _def;
    private IloOplModel _model;
    private long _batch_size;
    
    /**
     * Convenience method to write the output of a model to a database.
     * 
     * @param config The database connection configuration.
     * @param model The OPL model.
     */
    public static void writeOutput(JdbcConfiguration config, IloOplModel model) {
      IloOplModelDefinition definition = model.getModelDefinition();
      JdbcWriter writer = new JdbcWriter(config, definition, model);
      writer.customWrite();
  }

    public JdbcWriter(JdbcConfiguration configuration, IloOplModelDefinition def, IloOplModel model) {
        _configuration = configuration;
        _def = def;
        _model = model;
        _batch_size = DEFAULT_BATCH_SIZE;
    }

    public void customWrite() {
        long startTime = System.currentTimeMillis();
        System.out.println("Writing elements to database");
        Map<String, JdbcConfiguration.OutputParameters> outputMapping = _configuration.getOutputMapping();

        for (String name : outputMapping.keySet()) {
          JdbcConfiguration.OutputParameters op = outputMapping.get(name);
          System.out.println("Writing " + name);
          customWrite(name, op);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Done (" + (endTime - startTime)/1000.0 + " s)");
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
      int index = i + 1; // index in PreparedStatement
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

    /**
     * Writes a model element to database.
     * 
     * @param name The model element name.
     * @param table The database table.
     */
    void customWrite(String name, OutputParameters op) {
      String table = op.outputTable;
        IloOplElement elt = _model.getElement(name);
        ilog.opl_core.cppimpl.IloTupleSet tupleSet = (ilog.opl_core.cppimpl.IloTupleSet) elt.asTupleSet();
        IloTupleSchema schema = tupleSet.getSchema_cpp();
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(_configuration.getUrl(), _configuration.getUser(),
                    _configuration.getPassword());
            try (Statement stmt = conn.createStatement()) {
              String sql;
              // drop existing table if exists
              if (op.autodrop) {
                DatabaseMetaData dbm = conn.getMetaData();
                try (ResultSet rs = dbm.getTables(null, null, table, null)) {
                  boolean exists = rs.next();
                  if (exists) {
                    sql = DROP_QUERY.replaceFirst("%", table);
                    stmt.executeUpdate(sql);
                  }
                }
              }
              
              // create table using tuple fields
              // first create query
              sql = null;
              if (op.outputTable != null && op.createStatement == null) {
                sql = createTableQuery(schema, table);
              } else if (op.createStatement != null) {
                sql = op.createStatement;
              }
              if (sql != null) {
                stmt.execute(sql);
              }
            } 
            PreparedStatement insert = null;
            try {
              IloOplElementDefinition tupleDef = _def.getElementDefinition(schema.getName());
              IloOplTupleSchemaDefinition tupleSchemaDef = tupleDef.asTupleSchema();
              final Type[] columnType = new Type[schema.getSize()];
              for (int i = 0; i < columnType.length; ++i)
                  columnType[i] = tupleSchemaDef.getComponent(i).getElementDefinitionType();
              
              String psql = null;
              if (op.outputTable != null && op.insertStatement == null) {
                psql = getInsertQuery(schema, table);
              } else {
                psql = op.insertStatement;
              }
              insert = conn.prepareStatement(psql);
              
              conn.setAutoCommit(false); // begin transaction
              // iterate the set and create the final insert statement
              long icount = 1;
              for (java.util.Iterator it1 = tupleSet.iterator(); it1.hasNext();) {
                  IloTuple tuple = (IloTuple) it1.next();
                  updateValues(tuple, schema, tupleSchemaDef, insert);
                  if (_batch_size == 0) {
                    // no batch
                    insert.executeUpdate();
                  }
                  else {
                    insert.addBatch();
                    if ((icount % _batch_size) == 0) {
                      insert.executeBatch();
                    }
                  }
                  icount ++;
              }
              if (_batch_size != 0) {
                insert.executeBatch();
              }
              conn.commit();
            } catch (SQLException e) {
              conn.rollback();
              throw e;
            } finally {
              if (insert != null)
                insert.close();
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
