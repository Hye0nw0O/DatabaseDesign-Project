import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class Application {
    public static void launch() {
        JFrame frame = new JFrame("Stock Trading System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);
        frame.setLayout(new BorderLayout());

        JTextArea stock_data_area = new JTextArea();
        stock_data_area.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(stock_data_area);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 주식 정보 확인
        JButton refresh_button = new JButton("주식 정보 확인하기");
        refresh_button.addActionListener(e -> {
            stock_data_area.setText("");
            try {
                Stock_update.print_stock_data(stock_data_area);
                stock_data_area.append("\n");
            } catch (Exception ex) {
                stock_data_area.append("Error refreshing stock data.\n");
            }
        });
        frame.add(refresh_button, BorderLayout.NORTH);

        Timer auto_update_timer = new Timer(5000, e-> {
           try {
               Stock_update.update_stock_data(stock_data_area);
           } catch (Exception ex) {
               stock_data_area.append("주식 업데이트 중 오류 발생.\n");
               ex.printStackTrace();
           }
        });

        // 종료 타이머
        Timer limit_timer = new Timer(20000, e -> {
            try {
                Stock_update.close_stock();
                auto_update_timer.stop();
                stock_data_area.append("금일의 주식 장이 종료되었습니다.\n");
            } catch (SQLException ex) {
                stock_data_area.append("주식 장 종료 중 오류 발생\n");
                ex.printStackTrace();
            }
        });

        JPanel button_panel = new JPanel(new FlowLayout());
        JButton start_update = new JButton("주식 장 개시\n");

        start_update.addActionListener(e -> {
            try {
                Stock_update.open_stock();
                auto_update_timer.start();
                limit_timer.start();
                stock_data_area.append("주식 장이 개시되었습니다.\n거래를 시작하세요.\n");
            } catch (SQLException ex) {
                stock_data_area.append("주식 장 종료 중 오류 발생\n");
                ex.printStackTrace();
            }
        });
        button_panel.add(start_update);
        frame.add(button_panel, BorderLayout.WEST);
        frame.setVisible(true);


        JPanel trans_action_Panel = new JPanel(new GridLayout(2, 1));

        JPanel buy_Panel = new JPanel(new FlowLayout());
        JTextField buy_user_field = new JTextField(10);
        JPasswordField but_pw_field = new JPasswordField(10);
        JTextField buy_company_field =  new JTextField(10);
        JTextField buy_quantity_field = new JTextField(5);
        JButton buy_button = new JButton("매수");

        buy_button.addActionListener(e -> {
            String user_name = buy_user_field.getText();
            String password = new String(but_pw_field.getPassword());
            String company_name = buy_company_field.getText();
            int quantity = Integer.parseInt(buy_quantity_field.getText());
            try {
                if (Account.verify_account(user_name, password)) {
                    Buy_Stock.buy_stock(user_name, company_name, quantity, stock_data_area);
                } else {
                    stock_data_area.append("사용자 명 또는 비밀번호가 잘못되었습니다.\n");
                }
            } catch (Exception ex) {
                stock_data_area.append("매수 중 오류 발생\n");
                ex.printStackTrace();
            }
        });

        buy_Panel.add(new JLabel("닉네임: "));
        buy_Panel.add(buy_user_field);
        buy_Panel.add(new JLabel("패스워드: "));
        buy_Panel.add(but_pw_field);
        buy_Panel.add(new JLabel("회사명: "));
        buy_Panel.add(buy_company_field);
        buy_Panel.add(new JLabel("수량: "));
        buy_Panel.add(buy_quantity_field);
        buy_Panel.add(buy_button);
        trans_action_Panel.add(buy_Panel);

        JPanel sell_panel = new JPanel(new FlowLayout());
        JTextField sell_user_field = new JTextField(10);
        JPasswordField sell_pw_field = new JPasswordField(10);
        JTextField sell_company_field = new JTextField(10);
        JTextField sell_quantity_field = new JTextField(5);
        JButton sell_button = new JButton("매도");

        sell_button.addActionListener(e -> {
            String user_name = sell_user_field.getText();
            String password = new String(sell_pw_field.getPassword());
            String company_name = sell_company_field.getText();
            int quantity = Integer. parseInt(sell_quantity_field.getText());
            try {
                if (Account.verify_account(user_name, password)) {
                    Sell_Stock.sell_stock(user_name, company_name, quantity, stock_data_area);
                } else {
                    stock_data_area.append("사용자 명 또는 비밀번호가 잘못되었습니다.\n");
                }
            } catch (Exception ex) {
                stock_data_area.append("매도 중 오류 발생\n");
                ex.printStackTrace();
            }
        });

        sell_panel.add(new JLabel("닉네임: "));
        sell_panel.add(sell_user_field);
        sell_panel.add(new JLabel("패스워드"));
        sell_panel.add(sell_pw_field);
        sell_panel.add(new JLabel("회사명: "));
        sell_panel.add(sell_company_field);
        sell_panel.add(new JLabel("수량: "));
        sell_panel.add(sell_quantity_field);
        sell_panel.add(sell_button);
        trans_action_Panel.add(sell_panel);
        frame.add(trans_action_Panel, BorderLayout.SOUTH);

        frame.setVisible(true);

        JButton check_status = new JButton("전체 사용자 정보 확인");
        check_status.addActionListener(e -> {
            stock_data_area.setText("");
            User_status.display_user_status(stock_data_area);
        });
        button_panel.add(check_status);
    }
}
