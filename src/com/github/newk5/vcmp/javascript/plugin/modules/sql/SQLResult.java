package com.github.newk5.vcmp.javascript.plugin.modules.sql;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.github.newk5.vcmp.javascript.plugin.core.Context;
import static com.github.newk5.vcmp.javascript.plugin.core.Context.console;
import com.github.newk5.vcmp.javascript.plugin.core.common.AsyncResult;
import com.github.newk5.vcmp.javascript.plugin.core.common.ResultBuilder;
import io.alicorn.v8.V8JavaAdapter;
import io.alicorn.v8.V8JavaObjectUtils;
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
        ResultSet r = (ResultSet) super.getParams()[0];
        try {
            if (!r.isBeforeFirst() && r.getRow() == 0) {
                return null;
            }
            if (!multiple) {
                
                V8Object o = SQLWrapper.toObject(r);
                V8Array args = Context.toJavascriptArgs(new Object[]{o});
                r.close();
                st.close();
                return args;
                
            } else {
                
                V8Array o = SQLWrapper.toArray(r);
                V8Array args = Context.toJavascriptArgs(new Object[]{o});
                r.close();
                st.close();
                return args;
                
            }
        } catch (SQLException ex) {
            org.pmw.tinylog.Logger.error(ex);
            console.error(ex.getCause().toString());
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
