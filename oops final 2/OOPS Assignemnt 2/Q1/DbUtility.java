import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC utility used by the application. Change USER and PASSWORD to match
 * your MySQL installation before running the program.
 */
public final class DbUtility {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/course_registration_db";
    private static final String USER = "root";
    // Enter your local MySQL password here. Keep it empty only when your MySQL root user has no password.
    private static final String PASSWORD = "";

    private DbUtility() { }

    /** Loads the Type-4 MySQL driver and opens a physical database connection. */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL JDBC driver was not found. Add mysql-connector-j JAR to the classpath.", exception);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}