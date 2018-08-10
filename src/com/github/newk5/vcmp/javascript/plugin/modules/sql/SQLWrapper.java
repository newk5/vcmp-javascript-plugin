package com.github.newk5.vcmp.javascript.plugin.modules.sql;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8Value;
import com.github.newk5.vcmp.javascript.plugin.core.Context;
import static com.github.newk5.vcmp.javascript.plugin.core.Context.console;
import static com.github.newk5.vcmp.javascript.plugin.core.Context.v8;
import com.github.newk5.vcmp.javascript.plugin.core.EventLoop;
import io.alicorn.v8.V8JavaAdapter;
import io.alicorn.v8.V8JavaObjectUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.pmw.tinylog.Logger;

public class SQLWrapper {

    private ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    private EventLoop eventLoop;

    public static void injectClasses() {
        V8JavaAdapter.injectClass(Statement.class, v8);
        V8JavaAdapter.injectClass(PreparedStatement.class, v8);
        V8JavaAdapter.injectClass(ResultSet.class, v8);
        V8JavaAdapter.injectClass("SQLDate", java.sql.Date.class, v8);

    }

    public SQLWrapper() {
    }

    public void exec(PreparedStatement p, Boolean async, V8Function callback) {

        try {
            p.executeUpdate();
        } catch (SQLException ex) {
            Logger.error(ex);
            console.error(ex.getCause().toString());
        }
    }

    public V8Object query(Object query, Connection c, Boolean async, V8Function callback, boolean multiple) throws SQLException {

        if (async != null && async) {
            pool.submit(() -> {
                try {
                    ResultSet r = null;

                    Statement st = null;
                    if (query instanceof String) {
                        st = c.createStatement();
                        r = st.executeQuery(query.toString());

                    } else if (query instanceof Statement) {
                        PreparedStatement p = (PreparedStatement) query;
                        r = p.executeQuery();
                        st = (Statement) p;
                    }

                    if (callback != null) {
                        SQLResult res = new SQLResult(multiple, callback, new Object[]{r});
                        res.setSt(st);
                        eventLoop.queue.add(res);
                    }
                } catch (Exception ex) {
                    Logger.error(ex);
                    console.error(ex.getCause().toString());
                }
            });
        } else {
            Statement s = null;
            ResultSet r = null;
            if (query instanceof String) {
                s = c.createStatement();
                r = s.executeQuery(query.toString());
            } else if (query instanceof Statement) {
                PreparedStatement p = (PreparedStatement) query;
                r = p.executeQuery();
                s = (Statement) p;
            }
            if (!multiple) {
                V8Object obj = SQLWrapper.toObject(r);
                r.close();
                s.close();
                return obj;
            } else {
                V8Array arr = SQLWrapper.toArray(r);
                r.close();
                s.close();
                return arr;
            }

        }
        return null;
    }

    public SQLWrapper(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    public Connection newConnection(String url, String userName, String password) {

        try {
            if ((userName == null || "".equals(userName)) || (password == null || "".equals(password))) {
                return DriverManager.getConnection(url);
            }
            return DriverManager.getConnection(url, userName, password);
        } catch (SQLException ex) {
            Logger.error(ex);
            console.error(ex.getCause().toString());
        }
        return null;
    }

    public static V8Object toObject(ResultSet r) throws SQLException {
        V8Object v8obj = new V8Object(Context.v8);
        while (r.next()) {
            for (int i = 1; i <= r.getMetaData().getColumnCount(); i++) {
                Object obj = r.getObject(i);
                if (obj instanceof Boolean) {
                    v8obj.add(r.getMetaData().getColumnName(i), (Boolean) obj);
                } else if (obj instanceof String) {
                    v8obj.add(r.getMetaData().getColumnName(i), obj.toString());
                } else if (obj instanceof Double) {
                    v8obj.add(r.getMetaData().getColumnName(i), (Double) obj);
                } else if (obj instanceof Integer) {
                    v8obj.add(r.getMetaData().getColumnName(i), (Integer) obj);
                } else {
                    V8Value val = (V8Value) Context.toJavascript(obj);
                    v8obj.add(r.getMetaData().getColumnName(i), val);
                }
            }
            break;
        }
        return v8obj;
    }

    public static V8Array toArray(ResultSet r) throws SQLException {
        V8Array v8Arr = new V8Array(Context.v8);
        while (r.next()) {
            V8Object v8obj = new V8Object(Context.v8);

            for (int i = 1; i <= r.getMetaData().getColumnCount(); i++) {
                Object obj = r.getObject(i);
                if (obj instanceof Boolean) {
                    v8obj.add(r.getMetaData().getColumnName(i), (Boolean) obj);
                } else if (obj instanceof String) {
                    v8obj.add(r.getMetaData().getColumnName(i), obj.toString());
                } else if (obj instanceof Double) {
                    v8obj.add(r.getMetaData().getColumnName(i), (Double) obj);
                } else if (obj instanceof Integer) {
                    v8obj.add(r.getMetaData().getColumnName(i), (Integer) obj);
                } else {
                    V8Value val = (V8Value) Context.toJavascript(obj);
                    v8obj.add(r.getMetaData().getColumnName(i), val);
                }
            }
            v8Arr.push(v8obj);
        }
        return v8Arr;
    }

}
