package com.zhaoyss.jdbc.tx;

import com.zhaoyss.annotation.Transactional;
import com.zhaoyss.exception.TransactionException;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceTransactionManager implements PlatformTransactionManager, InvocationHandler {

    static final ThreadLocal<TransactionStatus> transactionStatus = new ThreadLocal<>();

    final DataSource dataSource;

    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        TransactionStatus ts = transactionStatus.get();
        if (ts == null){
            // 当前无事务，开启新事物
            try(Connection connection = dataSource.getConnection()){
                final boolean autoCommit = connection.getAutoCommit();
                if (autoCommit){
                    connection.setAutoCommit(false);
                }
                try{
                    // 设置ThreadLocal状态
                    transactionStatus.set(new TransactionStatus(connection));
                    // 调用业务方法
                    Object result = method.invoke(proxy, args);
                    // 提交事务
                    connection.commit();
                    // 方法返回
                    return result;
                }catch (InvocationTargetException e){
                    // 回滚事务
                    TransactionException te = new TransactionException(e.getCause());
                    try{
                        connection.rollback();
                    }catch (SQLException sqle){
                        te.addSuppressed(sqle);
                    }
                    throw te;
                }finally {
                    // 删除 ThreadLocal状态
                    transactionStatus.remove();
                    if (autoCommit){
                        connection.setAutoCommit(true);
                    }
                }
            }
        }else {
            // 当前已有事务，加入当前事务执行
            return method.invoke(proxy,args);
        }
    }
}
