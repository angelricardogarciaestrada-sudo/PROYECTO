package umg.simuladorsos1;

import javax.swing.SwingUtilities;
import umg.simuladorsos1.ui.MainFrame;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
