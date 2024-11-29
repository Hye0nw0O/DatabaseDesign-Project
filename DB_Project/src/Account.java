import java.sql.*;

public class Account {
    public static boolean verify_account(String user_name, String pw) throws SQLException {
        String query = "{call verify_account(?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(query)) {
            stmt.setString(1, user_name);
            stmt.setString(2, pw);
            stmt.registerOutParameter(3, java.sql.Types.BOOLEAN);
            stmt.execute();
            return stmt.getBoolean(3);
        }
    }
}
