import javax.swing.*;
import java.sql.*;
import java.util.Random;
import java.util.TimerTask;

public class Stock_update {
    public static void update_stock_data(JTextArea stock_data_area) throws SQLException {
        Random rd = new Random();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "select company_name, b_price, s_price from Stock";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

                stock_data_area.setText("");
                stock_data_area.append("회사명\t매수가\t매도가\n");
                stock_data_area.append("--------------------------------------------\n");

                while (rs.next()) {
                    String company_name = rs.getString("company_name");
                    double b_price = rs.getDouble("b_price");
                    double s_price = rs.getDouble("s_price");

                    // 랜덤 변화율 적용
                    double b_per = (rd.nextDouble() * 0.2 - 0.1);
                    double s_per = (rd.nextDouble() * 0.2 - 0.1);
                    double new_b_price = Math.round(b_price * (1 + b_per) * 100.0) / 100.0;
                    double new_s_price = Math.round(s_price * (1 + s_per) * 100.0) / 100.0;

                    // 데이터베이스 업데이트
                    String update_query = "UPDATE Stock SET b_price = ?, s_price = ? WHERE company_name = ?";
                    try (PreparedStatement update_stmt = conn.prepareStatement(update_query)) {
                        update_stmt.setDouble(1, new_b_price);
                        update_stmt.setDouble(2, new_s_price);
                        update_stmt.setString(3, company_name);
                        update_stmt.executeUpdate();
                    }

                    // 변경된 데이터 출력
                    stock_data_area.append(String.format("%-15s\t%.2f\t%.2f\n", company_name, new_b_price, new_s_price));
                }
            }
        }
    }
    static class Stock_update_task extends TimerTask {
        @Override
        public void run() {
            Random rd = new Random();
            double per;

            try (Connection conn = DBConnection.getConnection()){
                String query = "select company_name from stock";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                    while (rs.next()) {
                        String company_name = rs.getString("company_name");
                        double b_per = (rd.nextDouble() * 0.2 - 0.1);
                        double s_per = (rd.nextDouble() * 0.2 - 0.1);
                        double new_b = get_bPrice(conn, company_name) * (1 + b_per);
                        double new_s = get_sPrice(conn, company_name) * (1 + s_per);
                        update_bPrice(conn, company_name, new_b);
                        update_sPrice(conn, company_name, new_s);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        private double get_bPrice(Connection conn, String company_name) throws SQLException {
            String query = "select b_price from Stock where company_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, company_name);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("b_price");
                }
            }
            return 0.0;
        }
        private double get_sPrice(Connection conn, String company_name) throws SQLException {
            String query = "select s_price from Stock where company_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, company_name);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("s_price");
                }
            }
            return 0.0;
        }
        private void update_bPrice(Connection conn, String company_name, double newb_price) throws SQLException {
            String query = "update stock set b_price = ? where company_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                double update_b_price = Math.round(newb_price * 100.0) / 100.0;
                stmt.setDouble(1, update_b_price);
                stmt.setString(2, company_name);
                stmt.executeUpdate();
            }
        }
        private void update_sPrice(Connection conn, String company_name, double news_price) throws SQLException{
            String query = "update stock set s_price = ? where company_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                double update_s_price = Math.round(news_price * 100.0) / 100.0;
                stmt.setDouble(1, update_s_price);
                stmt.setString(2, company_name);
                stmt.executeUpdate();
            }
        }
    }

    public static void print_stock_data(JTextArea stock_data_area) throws SQLException {
        String query = "Select * from Stock_info";
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            stock_data_area.setText("");
            stock_data_area.append("회사명\t매수가\t매도가\n");
            stock_data_area.append("--------------------------------------------\n");

            while (rs.next()) {
                String company_name = rs.getString("회사명");
                double b_price = rs.getDouble("매수가");
                double s_price = rs.getDouble("매도가");

                stock_data_area.append(String.format("%-15s\t%.2f\t%.2f\n", company_name, b_price, s_price));
            }
        }
    }
    public static void open_stock() throws SQLException {
        String query = "{call open_stock()}";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        }
    }
    public static void close_stock() throws SQLException {
        String query = "{call close_stock()}";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        }
    }
}
