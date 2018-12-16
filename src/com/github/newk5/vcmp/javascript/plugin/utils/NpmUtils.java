
package com.github.newk5.vcmp.javascript.plugin.utils;

import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;


public class NpmUtils {

    public String lineSeparator = java.lang.System.getProperty("line.separator");

   
    public String userDir() {
        return System.getProperty("user.dir")+"/src";
    }

    public Map getSystemEnv() {
        return System.getenv();
    }

    public String getProp(String prop) {
        return System.getProperty(prop);
    }

    public java.sql.Date toSQLDate(LocalDateTime d) throws SQLException {
        if (d == null) {
            return null;
        }
        long ms = d.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return new java.sql.Date(ms);
    }

    public BigInteger toEpochMs(LocalDateTime d) {
        if (d == null) {
            return null;
        }
        long ms = d.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return new BigInteger(ms + "");
    }

    public BigInteger toBigInt(Long d) {
        if (d == null) {
            return null;
        }
        return new BigInteger(d + "");
    }

    public BigInteger toBigInt(Double d) {
        if (d == null) {
            return null;
        }
        return new BigInteger(d.intValue() + "");
    }

    public BigInteger toBigInt(String d) {
        if (d == null) {
            return null;
        }
        return new BigInteger(d);
    }
}
