import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Buy_Stock {
    public static void buy_stock (String user_name, String company_name, int quantity, JTextArea stock_data_area) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {

            if (!is_stock_open(conn)) {
                stock_data_area.append("시장이 개장되지 않아 주식 거래가 불가능합니다.\n");
                return;
            }

            double cur_price = get_bPrice(conn, company_name);
            if (cur_price == 0.0) {
                stock_data_area.append("구매 불가: 유효하지 않은 회사명입니다.\n");
                return;
            }

            double total_cost = cur_price * quantity;
            if (!check_user_balance(conn, user_name, total_cost)) {
                stock_data_area.append("현재 보유 금액보다 많은 금액을 구매할 수 없습니다.\n");
                return;
            }

            update_user_balance(conn, user_name, total_cost);
            record_history(conn, user_name, company_name, quantity, cur_price, "buy"); // 매수 => buy 거래 기록 추가
            update_own_stock(conn, user_name, company_name, quantity);
            stock_data_area.append(user_name + " 사용자가 " + company_name + " 주식을 " + quantity + "만큼 구매하였습니다.\n");
        } catch (SQLException e) {
            if (e.getMessage().contains("Table is read only")) {
                stock_data_area.append("주식 장이 종료되어 거래가 불가능합니다.\n");
            } else {
                throw e;
            }
        }
    }
    private static double get_bPrice(Connection conn, String company_name) throws SQLException { // 현재 주가 가져오기
        String query = "select b_price from stock where company_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, company_name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("b_price");
            }
        }
        return 0.0;
    }
    private static boolean check_user_balance(Connection conn, String user_name, double totalCost) throws SQLException { // 사용자 잔액 확인 함수
        String query = "select balance from User_info where user_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user_name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                return balance >= totalCost; // 사용자 잔액이 충분한 경우 true
            }
        }
        return false;
    }
    private static void update_user_balance(Connection conn, String user_name, double total_cost) throws SQLException { // 사용자 잔액 차감
        String query = "update User_info set balance = balance - ? where user_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, total_cost);
            stmt.setString(2, user_name);
            stmt.executeUpdate();
        }
    }

    private static void record_history(Connection conn, String user_name, String company_name, int quantity, double price, String dealType) throws SQLException { // 거래 내역 기록
        String query = "insert into Data_history (user_name, company_name, quantity, price, deal_type, timestamp) values (?, ?, ?, ?, ?, current_timestamp)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user_name);
            stmt.setString(2, company_name);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, price);
            stmt.setString(5, dealType);
            stmt.executeUpdate();
        }
    }
    private static void update_own_stock(Connection conn, String user_name, String company_name, int quantity) throws SQLException { // 사용자 보유 주식 업데이트
        String query = "insert into Own_stock (user_name, company_name, quantity) values (?, ?, ?) on duplicate key update quantity = quantity + ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user_name);
            stmt.setString(2, company_name);
            stmt.setInt(3, quantity);
            stmt.setInt(4, quantity);
            stmt.executeUpdate();
        }
    }
    private static boolean is_stock_open(Connection conn) throws SQLException {
        String query = "select c_open from company LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBoolean("c_open");
            }
        }
        return false;
    }

}
