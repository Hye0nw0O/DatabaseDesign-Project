import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Account {
    public static boolean verify_account(String user_name, String pw) throws SQLException {
        String query = "select password from Account where user_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user_name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String check_pw = rs.getString("password");
                return check_pw.equals(pw);
            }
        }
        return false;
    }
}
