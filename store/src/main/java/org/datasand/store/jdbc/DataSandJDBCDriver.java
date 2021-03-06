/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import org.datasand.codec.Encoder;
import org.datasand.microservice.MicroServicesManager;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DataSandJDBCDriver implements Driver {
    
    public static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DataSandJDBCDriver.class);
    public static DataSandJDBCDriver drv = new DataSandJDBCDriver();
    static {
        Encoder.registerSerializer(JDBCMessage.class, new JDBCMessage());
        Encoder.registerSerializer(JDBCDataContainer.class, new JDBCDataContainer());
    }
    public DataSandJDBCDriver() {
        try {
            DriverManager.registerDriver(this);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean acceptsURL(String arg0) throws SQLException {
        return true;
    }

    @Override
    public java.sql.Connection connect(String url, Properties arg1) throws SQLException {
        LOG.info("Data Sand JDBC Connection");
        try {
            MicroServicesManager manager = new MicroServicesManager(true);
            return new Connection(manager, url);
        } catch (Exception err) {
            err.printStackTrace();
        }
        LOG.info("Error Data Sand JDBC Connection");
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1)
        throws SQLException {
        DriverPropertyInfo i = new DriverPropertyInfo("DataSand", "DataSand");
        return new DriverPropertyInfo[] {i};
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        // TODO Auto-generated method stub
        return null;
    }

}
