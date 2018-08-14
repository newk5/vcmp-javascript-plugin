package com.github.newk5.vcmp.javascript.plugin.modules.sql;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.github.newk5.vcmp.javascript.plugin.core.Context;
import static com.github.newk5.vcmp.javascript.plugin.core.Context.console;
import com.github.newk5.vcmp.javascript.plugin.core.common.AsyncResult;
import com.github.newk5.vcmp.javascript.plugin.core.common.ResultBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLResult extends AsyncResult implements ResultBuilder {

    private boolean multiple;
    private Statement st;

    public SQLResult(boolean multiple) {
        this.multiple = multiple;
    }

    public SQLResult(boolean single, V8Function callback, Object[] params) {
        super(callback, params);
        this.multiple = single;
    }

    @Override
    public V8Array build() {
        if (super.getParams()[0] != null && super.getParams()[0] instanceof Integer) {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    org.pmw.tinylog.Logger.error(ex);
                    if (ex.getCause() != null) {
                        console.error(ex.getCause().toString());
                    } else {
                        console.error(ex.getMessage());
                    }
                }
            }
            V8Array args = Context.toJavascriptArgs((Integer) super.getParams()[0]);
            return args;
        } else {
            ResultSet r = (ResultSet) super.getParams()[0];
            try {

                if (r != null && !r.isBeforeFirst() && r.getRow() == 0) {
                    return null;
                }
                if (!multiple) {

                    V8Object o = SQLWrapper.toObject(r);
                    V8Array args = Context.toJavascriptArgs(o);
                    if (r != null) {
                        r.close();
                    }
                    if (st != null) {
                        st.close();
                    }

                    return args;

                } else {

                    V8Array o = SQLWrapper.toArray(r);
                    V8Array args = Context.toJavascriptArgs(o);
                    if (r != null) {
                        r.close();
                    }
                    if (st != null) {
                        st.close();
                    }
                    return args;

                }

            } catch (SQLException ex) {
                org.pmw.tinylog.Logger.error(ex);
                if (ex.getCause() != null) {
                    console.error(ex.getCause().toString());
                } else {
                    console.error(ex.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * @return the multiple
     */
    public boolean isMultiple() {
        return multiple;
    }

    /**
     * @param multiple the multiple to set
     */
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    /**
     * @return the st
     */
    public Statement getSt() {
        return st;
    }

    /**
     * @param st the st to set
     */
    public void setSt(Statement st) {
        this.st = st;
    }

}
