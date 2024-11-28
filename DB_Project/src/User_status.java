import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User_status {
    public static void display_user_status(JTextArea output_area) {
        try (Connection conn = DBConnection.getConnection()) {
            String queryUsers = "select user_name, balance from User_info";
            try (PreparedStatement stmtUsers = conn.prepareStatement(queryUsers);
                 ResultSet rsUsers = stmtUsers.executeQuery()) {
                if (!rsUsers.isBeforeFirst()) {
                    output_area.append("사용자가 없습니다.\n");
                    return;
                }

                while (rsUsers.next()) {
                    String user_name = rsUsers.getString("user_name");
                    double balance = rsUsers.getDouble("balance");

                    output_area.append("사용자: " + user_name + "\n");
                    output_area.append("잔여 금액: " + balance + "원\n");
                    output_area.append("보유 주식:\n");

                    displayOwnedStocks(conn, user_name, output_area);

                    output_area.append("\n");
                }
            }
        } catch (Exception e) {
            output_area.append("전체 사용자 정보 조회 중 오류 발생\n");
            e.printStackTrace();
        }
    }
    private static void displayOwnedStocks(Connection conn, String user_name, JTextArea outputArea) throws SQLException {
        String query = "select company_name, quantity from Own_stock where user_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user_name);
            ResultSet rs = stmt.executeQuery();
            if (!rs.isBeforeFirst()) {
                outputArea.append("  - 보유 주식 없음\n");
                return;
            }
            while (rs.next()) {
                String company = rs.getString("company_name");
                int quantity = rs.getInt("quantity");
                outputArea.append("  - " + company + ": " + quantity + "주\n");
            }
        }
    }

}
