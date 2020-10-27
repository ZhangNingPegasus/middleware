package org.wyyt.test.seata;


import io.seata.common.loader.LoadLevel;
import io.seata.rm.datasource.undo.AbstractUndoExecutor;
import io.seata.rm.datasource.undo.SQLUndoLog;
import io.seata.rm.datasource.undo.mysql.MySQLUndoExecutorHolder;
import io.seata.sqlparser.util.JdbcConstants;

@LoadLevel(name = JdbcConstants.MYSQL, order = 2)
public class CustomMySQLUndoExecutorHolder extends MySQLUndoExecutorHolder {
    @Override
    public AbstractUndoExecutor getInsertExecutor(SQLUndoLog sqlUndoLog) {
        return new CustomMySQLUndoInsertExecutor(sqlUndoLog);
    }
}