/*
 * Licensed Materials - Property of IBM
 * 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
 * Copyright IBM Corporation 1998, 2013. All Rights Reserved.
 *
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp.
 */

//-------------------------------------------------------------- -*- Java -*-
//Java version of customdatasource.cpp of OPL distrib
//--------------------------------------------------------------------------
package com.ibm.opl.customdatasource;

import ilog.concert.IloException;
import ilog.opl.IloOplDataSource;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplException;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplRunConfiguration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Arrays;

/**
 * This program demonstrates the use of OPL's data source API to customize a
 * data source using JDBC. It also shows how one can write results using JDBC.
 *
 */
public class CustomDataSource {
    static public void main(String[] args) throws Exception {
        int status = 127;
        try {
            CommandLine cl = new CommandLine(args);
            IloOplFactory.setDebugMode(true);
            IloOplFactory oplF = new IloOplFactory();
            IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);

            IloOplRunConfiguration rc = null;
            if (cl.getDataFileNames().size() == 0) {
                rc = oplF.createOplRunConfiguration(cl.getModelFileName());
            } else {
                String[] dataFiles = cl.getDataFileNames().toArray(new String[cl.getDataFileNames().size()]);
                rc = oplF.createOplRunConfiguration(cl.getModelFileName(), dataFiles);
            }
            rc.setErrorHandler(errHandler);
            IloOplModel opl = rc.getOplModel();

            IloOplModelDefinition def = opl.getModelDefinition();

            //
            // Reads the JDBC configuration, initialize a JDBC custom data source
            // and sets the source in OPL.
            //
            JdbcConfiguration jdbcProperties = null;
            String jdbcConfigurationFile = cl.getPropertiesFileName();
            if (jdbcConfigurationFile != null) {
                jdbcProperties = new JdbcConfiguration();
                jdbcProperties.read(jdbcConfigurationFile);
                // Create the custom JDBC data source
                IloOplDataSource jdbcDataSource = new JdbcCustomDataSource(jdbcProperties, oplF, def);
                // Pass it to the model.
                opl.addDataSource(jdbcDataSource);
            }
            
            opl.generate();

            if (cl.getExternalDataName() != null) {
                FileOutputStream ofs = new FileOutputStream(cl.getExternalDataName());
                opl.printExternalData(ofs);
                ofs.close();
            }
            boolean success = false;
            if (opl.hasCplex()) {
                if (opl.getCplex().solve()) {
                    success = true;
                }
            } else {
                if (opl.getCP().solve()) {
                    success = true;
                }
            }
            if (success == true) {
                opl.postProcess();
                // write results
                if (jdbcProperties != null) {
                    JdbcWriter writer = new JdbcWriter(jdbcProperties, def, opl);
                    writer.customWrite();
                }
            }
            oplF.end();
            status = 0;
        } catch (IloOplException ex) {
            System.err.println("### OPL exception: " + ex.getMessage());
            ex.printStackTrace();
            status = 2;
        } catch (IloException ex) {
            System.err.println("### CONCERT exception: " + ex.getMessage());
            ex.printStackTrace();
            status = 3;
        } catch (Exception ex) {
            System.err.println("### UNEXPECTED UNKNOWN ERROR ...");
            ex.printStackTrace();
            status = 4;
        }
        System.exit(status);
    }
}
