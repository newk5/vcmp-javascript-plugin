package com.github.newk5.vcmp.javascript.plugin.utils;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    public java.sql.Timestamp toSQLDate(LocalDateTime t) {
        if (t == null) {
            return null;
        }

        Instant instant = t.atZone(ZoneId.systemDefault()).toInstant();
        Date oldDate = Date.from(instant);

        return new java.sql.Timestamp(oldDate.getTime());
    }

    public java.sql.Timestamp toSQLTimestamp(LocalDateTime t) {
        Instant instant = t.atZone(ZoneId.systemDefault()).toInstant();
        Date oldDate = Date.from(instant);

        return new java.sql.Timestamp(oldDate.getTime());
    }

    public LocalDateTime parseSQLDate(Object d) {
        if (d instanceof BigInteger) {
            return parseSQLiteDate((BigInteger) d);
        } else {
            return parseMySQLDate(d);
        }
    }
    
    public LocalDateTime parseMySQLDate(Object date) {
        if (date instanceof LocalDateTime) {
            return (LocalDateTime) date;
        }
        
        
        if (date instanceof java.util.Date) {
            java.util.Date d = (java.util.Date) date;
            LocalDateTime da = LocalDateTime.ofInstant(Instant.ofEpochMilli(d.getTime()), TimeZone.getDefault().toZoneId());
            return da;
        } else if (date instanceof java.sql.Timestamp) {
            java.sql.Timestamp t = (java.sql.Timestamp) date;

            LocalDateTime da = LocalDateTime.ofInstant(Instant.ofEpochMilli(t.getTime()), TimeZone.getDefault().toZoneId());
            return da;
        } else if (date instanceof java.sql.Date) {
            java.sql.Date d = (java.sql.Date) date;
            LocalDateTime da = LocalDateTime.ofInstant(Instant.ofEpochMilli(d.getTime()), TimeZone.getDefault().toZoneId());
            return da;
        }
        return null;
    }

    public LocalDateTime parseSQLiteDate(BigInteger ms) {
        LocalDateTime d = LocalDateTime.ofInstant(Instant.ofEpochMilli(ms.longValue()), TimeZone.getDefault().toZoneId());
        return d;
    }

    public String formatDate(String pattern, LocalDateTime d) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return dtf.format(d);
    }
}
