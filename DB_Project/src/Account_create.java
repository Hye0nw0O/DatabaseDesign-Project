import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
                account_frame.dispose();
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
        String query = "insert into Account (id, password, user_name) values (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            stmt.setString(2, password);
            stmt.setString(3, user_name);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                String error_msg = e.getMessage();
                String error_field = extractDuplicateField(error_msg);
                JOptionPane.showMessageDialog(null,  "이미 사용중인 " + error_field + "입니다.", "계정 생성 실패", JOptionPane.ERROR_MESSAGE);
            } else {
                e.printStackTrace();
            }
        }
        return false;
    }
    private static String extractDuplicateField(String errorMessage) {
        if (errorMessage.contains("account.PRIMARY")) {
            return "ID";
        } else if (errorMessage.contains("account.password")) {
            return "패스워드";
        } else if (errorMessage.contains("account.user_name")) {
            return "사용자 명";
        } else {
            return "알 수 없는 필드";
        }
    }

}
