package org.wyyt.test.seata;

import io.seata.rm.datasource.sql.struct.TableRecords;
import io.seata.rm.datasource.undo.SQLUndoLog;
import io.seata.rm.datasource.undo.mysql.MySQLUndoInsertExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class CustomMySQLUndoInsertExecutor extends MySQLUndoInsertExecutor {
    public CustomMySQLUndoInsertExecutor(SQLUndoLog sqlUndoLog) {
        super(sqlUndoLog);
    }

    @Override
    protected TableRecords queryCurrentRecords(Connection conn) throws SQLException {
        return super.queryCurrentRecords(conn);
    }
}