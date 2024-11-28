import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Account_create {
    public interface  Account_create_callback {
        void on_Account_create(String user_name);

        void on_cancel();
    }
    public static void account_create(Account_create_callback callback) {
        JFrame account_frame = new JFrame("계정 생성");
        account_frame.setSize(400, 300);
        account_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        account_frame.setLayout(new GridLayout(5, 2));

        JLabel id_l = new JLabel("ID");
        JTextField id_f = new JTextField();
        JLabel pw_l = new JLabel("Password");
        JPasswordField pw_f = new JPasswordField();
        JLabel user_name_l = new JLabel("사용자 명: ");
        JTextField user_name_f = new JTextField();

        JButton create = new JButton("계정 생성");
        JButton skip = new JButton("계정을 생성하지 않고 진행(계정 존재 시 선택)");
        JButton cancel = new JButton("취소");

        create.addActionListener(e -> {
            String id = id_f.getText().trim();
            String password = new String(pw_f.getPassword());
            String user_name = user_name_f.getText().trim();

            if (id.isEmpty() || password.isEmpty() || user_name.isEmpty()) {
                JOptionPane.showMessageDialog(account_frame, "세 요소를 빠짐없이 입력해주세요", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (insert_account(id, password, user_name)) {
                JOptionPane.showMessageDialog(account_frame, "계정 생성 완료", "Account create success", JOptionPane.INFORMATION_MESSAGE);
                callback.on_Account_create(user_name);
                account_frame.dispose();
            } else {
                JOptionPane.showMessageDialog(account_frame, "ID, password, 사용자 명이 이미 존재합니다.", "Account create success", JOptionPane.ERROR_MESSAGE);
            }
        });
        skip.addActionListener(e -> { // skip 버튼 -> 기존 계정 존재 시, 계정 생성 없이 진행
            callback.on_cancel();
            account_frame.dispose();
        });
        cancel.addActionListener(e -> { // cancel 버튼 -> 프로그램 종료
            System.exit(0); 
        });

        account_frame.add(id_l);
        account_frame.add(id_f);
        account_frame.add(pw_l);
        account_frame.add(pw_f);
        account_frame.add(user_name_l);
        account_frame.add(user_name_f);
        account_frame.add(create);
        account_frame.add(skip);
        account_frame.add(cancel);

        account_frame.setVisible(true);

    }
    private static boolean insert_account(String id, String password, String user_name) {
        String check_query = "select count(*) from account where id = ? or user_name = ?";
        String query = "insert into Account (id, password, user_name) values (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement check_stmt = conn.prepareStatement(check_query); PreparedStatement stmt = conn.prepareStatement(query)) {
            check_stmt.setString(1, id);
            check_stmt.setString(2, user_name);
            ResultSet rs = check_stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // 같은 계정이 존재함
            }
            stmt.setString(1, id);
            stmt.setString(2, password);
            stmt.setString(3, user_name);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}