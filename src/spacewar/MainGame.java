package spacewar;

import javax.swing.SwingUtilities;

public class MainGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SpaceWarGame game = new SpaceWarGame();
            game.setVisible(true);
        });
    }
}