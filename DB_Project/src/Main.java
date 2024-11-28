import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Account_create.account_create(new Account_create.Account_create_callback() {
                @Override
                public void on_Account_create(String user_name) {
                    SwingUtilities.invokeLater(Application :: launch);
                }

                @Override
                public void on_cancel() {
                    SwingUtilities.invokeLater(Application :: launch);
                }
            });
        });


    }
}