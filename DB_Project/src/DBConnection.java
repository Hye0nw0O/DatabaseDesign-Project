import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection { // DB 연결 확인용
    private static final String URL = "jdbc:mysql://localhost:3306/irb";
    private static final String USER = "root";
    private static final String PASSWORD = "acj41510";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
