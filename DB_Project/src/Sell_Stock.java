import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Sell_Stock {

    public static void sell_stock (String user_name, String company_name, int quantity, JTextArea stock_data_area) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            if (!is_stock_open(conn)) {
                stock_data_area.append("시장이 개장되지 않아 주식 거래가 불가능합니다.\n");
                return;
            }

            int stock_quantity = get_user_quantity(conn, user_name, company_name);
            if (stock_quantity == 0) {
                stock_data_area.append("매도 불가: " + user_name + " 사용자가 " + company_name + " 주식을 보유하지 않았습니다.\n");
                return;
            }

            if (stock_quantity < quantity) { // 보유량보다 많은 주식을 매도하려는 경우
                stock_data_area.append("매도 불가: 현재 보유 중인 " + stock_quantity + "주보다 많이 매도할 수 없습니다.\n");
                return;
            }

            double s_price = get_sPrice(conn, company_name); // 매도 가격 확인
            if (s_price == 0.0) {
                stock_data_area.append("매도 불가: 유효하지 않은 회사명입니다.\n");
                return;
            }

            double gain = s_price * quantity;
            update_user_balance(conn, user_name, gain); // 사용자 잔액 업데이트
            update_user_Stock(conn, user_name, company_name, quantity); // 보유 주식 감소
            record_history(conn, user_name, company_name, quantity, s_price, "sell"); // 거래 기록 추가
            stock_data_area.append(user_name + " 사용자가 " + company_name + " 주식을 " + quantity + "주 매도하여 " + gain + " 잔액이 증가했습니다.\n");
        } catch (SQLException e) {
            if (e.getMessage().contains("Table is read only")) {
                stock_data_area.append("매도 불가: 주식 장이 종료되어 거래가 불가능합니다.\n");
            } else {
                throw e;
            }
        }
    }

    private static double get_sPrice(Connection conn, String company_name) throws SQLException { // 현재 주가 가져오기
        String query = "select s_price from stock where company_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, company_name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("s_price");
            }
        }
        return 0.0;
    }
    private static int get_user_quantity(Connection conn, String user_name, String company_name) throws SQLException { // 사용자가 소유중인 주식 확인
        String query = "select quantity from Own_stock where user_name = ? and company_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user_name);
            stmt.setString(2, company_name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantity");
            }
        }
        return 0;
    }
    private static void update_user_balance(Connection conn, String user_name, double gain) throws SQLException { // 매도 후 사용자 잔액 업데이트
        String query = "update User_info set balance = balance + ? where user_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, gain);
            stmt.setString(2, user_name);
            stmt.executeUpdate();
            System.out.println(user_name + " 사용자의 잔액 업데이트 완료");
        }
    }
    private static void update_user_Stock(Connection conn, String user_name, String company_name, int quantity) throws SQLException { // 사용자가 소유한 주식 양 조정 => use Own_stock 테이블
        String query = "update Own_stock set quantity = quantity - ? where user_name = ? and company_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setString(2, user_name);
            stmt.setString(3, company_name);
            stmt.executeUpdate();
            String del_query = "delete from Own_stock where user_name = ? and company_name= ? and quantity = 0";
            try (PreparedStatement del_stmt = conn.prepareStatement(del_query)) {
                del_stmt.setString(1, user_name);
                del_stmt.setString(2, company_name);
                del_stmt.executeUpdate();
            }
            System.out.println("Own stock 테이블 기반으로" + user_name + " 사용자의 주식 보유 량 업데이트 완료");
        }
    }
    private static void record_history(Connection conn, String user_name, String company_name, int quantity, double price, String dealType) throws SQLException { // 매도 거래 기록
        String query = "insert into Data_history (user_name, company_name, quantity, price, deal_type, timestamp) values (?, ?, ?, ?, ?, current_timestamp)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user_name);
            stmt.setString(2, company_name);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, price);
            stmt.setString(5, dealType);
            stmt.executeUpdate();
            System.out.println(user_name + " 사용자의 매도 기록 완료."); // 가독성 이유로 얘는 없애도 될 것 같음
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
