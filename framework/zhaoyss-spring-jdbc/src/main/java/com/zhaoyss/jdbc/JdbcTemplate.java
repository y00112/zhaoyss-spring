package com.zhaoyss.jdbc;

import com.zhaoyss.exception.DataAccessException;
import com.zhaoyss.jdbc.tx.TransactionalUtils;
import jakarta.annotation.Nullable;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(ConnectionCallback<T> action) {
        // 获取当前事务连接
        Connection current = TransactionalUtils.getCurrentConnection();
        if (current != null) {
            try {
                return action.doInConnection(current);
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }

        // 无事务，从 DataSource 获取新连接
        try (Connection newConn = dataSource.getConnection()) {
            T result = action.doInConnection(newConn);
            return result;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) {
        return execute((Connection con) -> {
            try (PreparedStatement ps = psc.createPreparedStatement(con)) {
                return action.doInPreparedStatement(ps);
            }
        });
    }

    public int update(String sql, Object... args) {
        return execute(
                preparedStatementCreator(sql, args),
                (PreparedStatement ps) -> {
                    return ps.executeUpdate();
                }
        );
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(preparedStatementCreator(sql, args)
                , (PreparedStatement ps) -> {
                    List<T> list = new ArrayList<>();
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(rowMapper.mapRow(rs, rs.getRow()));
                        }
                    }
                    return list;
                });
    }

    public <T> List<T> queryForList(String sql, Class<T> clazz, Object... args) {
        return queryForList(sql, new BeanRowMapper<>(clazz), args);
    }

    @SuppressWarnings("unchecked")
    public <T> T queryForObject(String sql,Class<T> clazz,Object... args){
        if (clazz == String.class) {
            return (T) queryForObject(sql, StringRowMapper.instance, args);
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) queryForObject(sql, BooleanRowMapper.instance, args);
        }
        if (Number.class.isAssignableFrom(clazz) || clazz.isPrimitive()) {
            return (T) queryForObject(sql, NumberRowMapper.instance, args);
        }
        return queryForObject(sql, new BeanRowMapper<>(clazz), args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... arsg) {
        return execute(
                preparedStatementCreator(sql, arsg),
                (PreparedStatement ps) -> {
                    T t = null;
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            if (t == null) {
                                t = rowMapper.mapRow(rs, rs.getRow());
                            } else {
                                throw new DataAccessException("Multiple rows found.");
                            }
                        }
                    }
                    if (t == null) {
                        throw new DataAccessException("Empty result set.");
                    }
                    return t;

                }
        );
    }

    public Number queryForNumber(String sql, Object... args) {
        return queryForObject(sql, NumberRowMapper.instance, args);
    }

    private PreparedStatementCreator preparedStatementCreator(String sql, Object... args) {
        return (Connection con) -> {
            var ps = con.prepareStatement(sql);
            bindArgs(ps, args);
            return ps;
        };
    }

    private void bindArgs(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    public Number updateAndReturnGeneratedKey(String sql, Object... args) {
        return execute(
                // PreparedStatementCreator
                (Connection con) -> {
                    var ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    bindArgs(ps, args);
                    return ps;
                },
                // PreparedStatementCallback
                (PreparedStatement ps) -> {
                    int n = ps.executeUpdate();
                    if (n == 0) {
                        throw new DataAccessException("0 rows inserted.");
                    }
                    if (n > 1) {
                        throw new DataAccessException("Multiple rows inserted.");
                    }
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        while (keys.next()) {
                            return (Number) keys.getObject(1);
                        }
                    }
                    throw new DataAccessException("Should not reach here.");
                }
        );
    }
}

class NumberRowMapper implements RowMapper<Number> {
    static NumberRowMapper instance = new NumberRowMapper();


    @Nullable
    @Override
    public Number mapRow(ResultSet rs, int rowNum) throws SQLException {
        return (Number) rs.getObject(1);
    }
}

class StringRowMapper implements RowMapper<String>{

    static StringRowMapper instance = new StringRowMapper();

    @Nullable
    @Override
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString(1);
    }
}

class BooleanRowMapper implements RowMapper<Boolean> {

    static BooleanRowMapper instance = new BooleanRowMapper();

    @Override
    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getBoolean(1);
    }
}