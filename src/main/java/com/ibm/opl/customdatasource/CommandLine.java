package com.ibm.opl.customdatasource;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for command line parsing.
 *
 */
public class CommandLine {
    String _modelFileName;
    String _propertiesFileName;
    ArrayList<String> _dataFileNames = new ArrayList<String>();

    String _externalDataName;

    CommandLine(String[] args) {
        if (args.length == 0) {
            usage();
        }
        int i = 0;
        for (i = 0; i < args.length; i++) {
            if ("-h".equals(args[i])) {
                usage();
            } else if ("-export".equals(args[i])) {
                i++;
                if (i < args.length && args[i].charAt(0) != '-' && args[i].charAt(0) != '\0') {
                    _externalDataName = args[i];
                } else {
                    usage();
                }
            } else if ("-properties".equals(args[i])) {
                i++;
                if (i < args.length && args[i].charAt(0) != '-' && args[i].charAt(0) != '\0') {
                    _propertiesFileName = args[i];
                } else {
                    usage();
                }
            } else if (args[i].charAt(0) == '-') {
                System.err.println("Unknown option: " + args[i]);
                usage();
            } else {
                break;
            }
        }
        if (i < args.length) {
            _modelFileName = args[i];
            for (i++; i < args.length; i++) {
                _dataFileNames.add(args[i]);
            }
        }
    }

    void usage() {
        System.err.println();
        System.err.println("Usage:");
        System.err.println("CustomDataSource [options] model-file [data-file ...]");
        System.err.println("  options ");
        System.err.println("    -h                        this help message");
        System.err.println("    -export [dat-file]        write external data");
        System.err.println("    -properties [prop-file]   use given properties");
        System.err.println();
        System.exit(0);
    }

    String getModelFileName() {
        return _modelFileName;
    }

    List<String> getDataFileNames() {
        return _dataFileNames;
    }

    String getPropertiesFileName() {
        return _propertiesFileName;
    }

    String getExternalDataName() {
        return _externalDataName;
    }
}