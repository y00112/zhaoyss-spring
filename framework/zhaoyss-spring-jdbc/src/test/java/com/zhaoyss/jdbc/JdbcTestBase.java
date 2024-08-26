package com.zhaoyss.jdbc;

import com.zhaoyss.io.PropertyResolver;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class JdbcTestBase {
    public static final String CREATE_USER = "CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(255) NOT NULL, age INTEGER)";
    public static final String CREATE_ADDRESS = "CREATE TABLE addresses (id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER NOT NULL, address VARCHAR(255) NOT NULL, zip INTEGER)";

    public static final String INSERT_USER = "INSERT INTO users (name, age) VALUES (?, ?)";
    public static final String INSERT_ADDRESS = "INSERT INTO addresses (userId, address, zip) VALUES (?, ?, ?)";

    public static final String UPDATE_USER = "UPDATE users SET name = ?, age = ? WHERE id = ?";
    public static final String UPDATE_ADDRESS = "UPDATE addresses SET address = ?, zip = ? WHERE id = ?";

    public static final String DELETE_USER = "DELETE FROM users WHERE id = ?";
    public static final String DELETE_ADDRESS_BY_USERID = "DELETE FROM addresses WHERE userId = ?";

    public static final String SELECT_USER = "SELECT * FROM users WHERE id = ?";
    public static final String SELECT_USER_NAME = "SELECT name FROM users WHERE id = ?";
    public static final String SELECT_USER_AGE = "SELECT age FROM users WHERE id = ?";
    public static final String SELECT_ADDRESS_BY_USERID = "SELECT * FROM addresses WHERE userId = ?";

    public static PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        ps.put("zhaoyss.datasource.url", "jdbc:sqlite:test.db");
        ps.put("zhaoyss.datasource.username", "sa");
        ps.put("zhaoyss.datasource.password", "");
        ps.put("zhaoyss.datasource.driver-class-name", "org.sqlite.JDBC");
        var pr = new PropertyResolver(ps);
        return pr;
    }

    @BeforeEach
    public static void beforeEach() {
        cleanDb();
    }

    private static void cleanDb() {
        Path db = Path.of("test.db").normalize().toAbsolutePath();
        try {
            Files.deleteIfExists(db);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


}
