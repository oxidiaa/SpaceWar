package spacewar;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.ResultSet;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class SpaceWarGame extends JFrame {
    private JPanel mainMenuPanel;
    private JPanel highScorePanel;
    private JPanel difficultyPanel;
    private CardLayout cardLayout;
    private DatabaseConnection dbConnection;
    private GamePanel gamePanelInstance;
    private JTextArea scoreArea;
    private Timer starTimer;
    private float starAngle = 0;
    private GamePanel.Difficulty selectedDifficulty = GamePanel.Difficulty.NORMAL;
    private BufferedImage backgroundImage;
    private Clip menuSound;

    public SpaceWarGame() {
        setTitle("Space War");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        dbConnection = new DatabaseConnection();

        cardLayout = new CardLayout();
        setLayout(cardLayout);

        // Initialize background
        initializeBackground();

        createMainMenu();
        createHighScorePanel();
        createDifficultyPanel();

        add(mainMenuPanel, "MENU");
        add(highScorePanel, "HIGHSCORE");
        add(difficultyPanel, "DIFFICULTY");

        cardLayout.show(getContentPane(), "MENU");

        loadMenuSound();
        playMenuSound();
    }

    private void initializeBackground() {
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/resources/background.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createMainMenu() {
        mainMenuPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        
        // Create content panel with semi-transparent background
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setOpaque(false);
        mainMenuPanel.add(contentPanel, BorderLayout.CENTER);

        // Title with space effect
        JLabel titleLabel = new JLabel("SPACE WAR") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create metallic gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 215, 0),
                    0, getHeight(), new Color(255, 165, 0)
                );
                g2d.setPaint(gradient);
                
                // Draw text with glow effect
                Font font = new Font("Arial", Font.BOLD, 48);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                
                // Draw glow
                for (int i = 0; i < 5; i++) {
                    g2d.setColor(new Color(255, 215, 0, 50 - i * 10));
                    g2d.drawString(text, x - i, y - i);
                    g2d.drawString(text, x + i, y + i);
                }
                
                // Draw main text
                g2d.setColor(new Color(255, 215, 0));
                g2d.drawString(text, x, y);
            }
        };
        titleLabel.setPreferredSize(new Dimension(400, 100));
        titleLabel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 50, 0);
        contentPanel.add(titleLabel, gbc);

        // Create futuristic buttons
        JButton playButton = createFuturisticButton("Play Game");
        JButton highScoreButton = createFuturisticButton("High Score");
        JButton exitButton = createFuturisticButton("Exit");

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 10, 0);
        contentPanel.add(playButton, gbc);

        gbc.gridy = 2;
        contentPanel.add(highScoreButton, gbc);

        gbc.gridy = 3;
        contentPanel.add(exitButton, gbc);

        playButton.addActionListener(e -> cardLayout.show(getContentPane(), "DIFFICULTY"));
        highScoreButton.addActionListener(e -> showHighScorePanel());
        exitButton.addActionListener(e -> System.exit(0));

        // Start animation timer
        starTimer = new Timer(50, e -> {
            starAngle += 0.02f;
            mainMenuPanel.repaint();
        });
        starTimer.start();
    }

    private JButton createFuturisticButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create metallic gradient
                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, new Color(50, 50, 100),
                                               0, getHeight(), new Color(30, 30, 80));
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, new Color(70, 70, 120),
                                               0, getHeight(), new Color(50, 50, 100));
                } else {
                    gradient = new GradientPaint(0, 0, new Color(40, 40, 80),
                                               0, getHeight(), new Color(20, 20, 60));
                }
                g2d.setPaint(gradient);
                
                // Draw button shape
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Draw glowing border
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(100, 100, 200, 150));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
                
                // Draw highlight
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.drawLine(5, 5, getWidth() - 5, 5);
                
                // Draw text with glow
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                
                // Draw text glow
                g2d.setColor(new Color(100, 100, 200, 100));
                g2d.drawString(getText(), x - 1, y - 1);
                g2d.drawString(getText(), x + 1, y + 1);
                
                // Draw main text
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 45));
        button.setOpaque(false);
        
        return button;
    }

    private void createHighScorePanel() {
        highScorePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        
        // Create content panel with semi-transparent background
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setOpaque(false);
        highScorePanel.add(contentPanel, BorderLayout.CENTER);

        // Title with space effect
        JLabel titleLabel = new JLabel("HIGH SCORES", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create metallic gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 215, 0),
                    0, getHeight(), new Color(255, 165, 0)
                );
                g2d.setPaint(gradient);
                
                // Draw text with glow effect
                Font font = new Font("Arial", Font.BOLD, 36);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                
                // Draw glow
                for (int i = 0; i < 5; i++) {
                    g2d.setColor(new Color(255, 215, 0, 50 - i * 10));
                    g2d.drawString(text, x - i, y - i);
                    g2d.drawString(text, x + i, y + i);
                }
                
                // Draw main text
                g2d.setColor(new Color(255, 215, 0));
                g2d.drawString(text, x, y);
            }
        };
        titleLabel.setPreferredSize(new Dimension(400, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Create a custom panel for scores
        JPanel scoresPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw semi-transparent background
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Draw border glow
                g2d.setColor(new Color(100, 100, 200, 100));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
            }
        };
        scoresPanel.setLayout(new BoxLayout(scoresPanel, BoxLayout.Y_AXIS));
        scoresPanel.setOpaque(false);
        scoresPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Score area with enhanced styling
        scoreArea = new JTextArea() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw text with custom formatting
                Font font = new Font("Consolas", Font.BOLD, 18);
                g2d.setFont(font);
                
                String[] lines = getText().split("\n");
                int y = 30;
                
                // Draw header
                g2d.setColor(new Color(255, 215, 0));
                g2d.drawString("RANK  PLAYER NAME           SCORE", 10, y);
                y += 30;
                
                // Draw separator line
                g2d.setColor(new Color(100, 100, 200, 150));
                g2d.drawLine(10, y, getWidth() - 10, y);
                y += 20;
                
                // Draw scores
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        // Draw text with glow
                        g2d.setColor(new Color(255, 255, 255, 100));
                        g2d.drawString(line, 12, y - 2);
                        g2d.drawString(line, 12, y + 2);
                        
                        // Draw main text
                        g2d.setColor(Color.WHITE);
                        g2d.drawString(line, 12, y);
                        
                        // Draw separator line
                        g2d.setColor(new Color(100, 100, 200, 50));
                        g2d.drawLine(10, y + 10, getWidth() - 10, y + 10);
                        
                        y += 35;
                    }
                }
            }
        };
        scoreArea.setEditable(false);
        scoreArea.setFont(new Font("Consolas", Font.BOLD, 18));
        scoreArea.setBackground(new Color(0, 0, 0, 0));
        scoreArea.setForeground(Color.WHITE);
        scoreArea.setCaretColor(Color.WHITE);
        scoreArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(scoreArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = createFuturisticButton("Back to Menu");
        backButton.addActionListener(e -> cardLayout.show(getContentPane(), "MENU"));
        contentPanel.add(backButton, BorderLayout.SOUTH);
    }

    private void createDifficultyPanel() {
        difficultyPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        
        // Create content panel with semi-transparent background
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setOpaque(false);
        difficultyPanel.add(contentPanel, BorderLayout.CENTER);

        // Title
        JLabel titleLabel = new JLabel("SELECT DIFFICULTY") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create metallic gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 215, 0),
                    0, getHeight(), new Color(255, 165, 0)
                );
                g2d.setPaint(gradient);
                
                // Draw text with glow effect
                Font font = new Font("Arial", Font.BOLD, 36);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                
                // Draw glow
                for (int i = 0; i < 5; i++) {
                    g2d.setColor(new Color(255, 215, 0, 50 - i * 10));
                    g2d.drawString(text, x - i, y - i);
                    g2d.drawString(text, x + i, y + i);
                }
                
                // Draw main text
                g2d.setColor(new Color(255, 215, 0));
                g2d.drawString(text, x, y);
            }
        };
        titleLabel.setPreferredSize(new Dimension(400, 80));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 50, 0);
        contentPanel.add(titleLabel, gbc);

        // Difficulty buttons
        JButton easyButton = createDifficultyButton("EASY", GamePanel.Difficulty.EASY);
        JButton normalButton = createDifficultyButton("NORMAL", GamePanel.Difficulty.NORMAL);
        JButton hardButton = createDifficultyButton("HARD", GamePanel.Difficulty.HARD);
        JButton backButton = createFuturisticButton("Back to Menu");

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 10, 0);
        contentPanel.add(easyButton, gbc);

        gbc.gridy = 2;
        contentPanel.add(normalButton, gbc);

        gbc.gridy = 3;
        contentPanel.add(hardButton, gbc);

        gbc.gridy = 4;
        contentPanel.add(backButton, gbc);

        backButton.addActionListener(e -> cardLayout.show(getContentPane(), "MENU"));
    }

    private JButton createDifficultyButton(String text, GamePanel.Difficulty difficulty) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create metallic gradient
                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, new Color(50, 50, 100),
                                               0, getHeight(), new Color(30, 30, 80));
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, new Color(70, 70, 120),
                                               0, getHeight(), new Color(50, 50, 100));
                } else {
                    gradient = new GradientPaint(0, 0, new Color(40, 40, 80),
                                               0, getHeight(), new Color(20, 20, 60));
                }
                g2d.setPaint(gradient);
                
                // Draw button shape
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Draw glowing border
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(100, 100, 200, 150));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
                
                // Draw highlight
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.drawLine(5, 5, getWidth() - 5, 5);
                
                // Draw text with glow
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                
                // Draw text glow
                g2d.setColor(new Color(100, 100, 200, 100));
                g2d.drawString(getText(), x - 1, y - 1);
                g2d.drawString(getText(), x + 1, y + 1);
                
                // Draw main text
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 45));
        
        button.addActionListener(e -> {
            selectedDifficulty = difficulty;
            showGame();
        });
        
        return button;
    }

    public void showGame() {
        stopMenuSound();
        if (gamePanelInstance != null) {
            remove(gamePanelInstance);
        }
        gamePanelInstance = new GamePanel();
        gamePanelInstance.setDifficulty(selectedDifficulty);
        add(gamePanelInstance, "GAMEPANEL");
        cardLayout.show(getContentPane(), "GAMEPANEL");
        gamePanelInstance.requestFocusInWindow();
        revalidate();
        repaint();
    }

    public void showMenu() {
        playMenuSound();
        cardLayout.show(getContentPane(), "MENU");
    }

    public void saveScore(int score) {
        String name = JOptionPane.showInputDialog(this, "Game Over!\nEnter your name for High Score:", "High Score", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            dbConnection.addHighScore(name, score);
        }
    }

    public void showHighScorePanel() {
        updateHighScores();
        cardLayout.show(getContentPane(), "HIGHSCORE");
    }

    private void updateHighScores() {
        StringBuilder sb = new StringBuilder();
        ResultSet rs = null;
        try {
            rs = dbConnection.getHighScores();
            int rank = 1;
            while (rs != null && rs.next() && rank <= 10) {
                String name = rs.getString("player_name");
                int score = rs.getInt("score");
                // Format with fixed width for better alignment
                sb.append(String.format("%-2d.  %-20s  %5d", 
                    rank++,
                    name,
                    score));
                sb.append("\n");
            }
        } catch (Exception e) {
            sb.append("Failed to load high scores.");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        scoreArea.setText(sb.toString());
    }

    // Method untuk load sound menu
    private void loadMenuSound() {
        try {
            URL menuSoundURL = getClass().getResource("/resources/dashboard.wav");
            if (menuSoundURL != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(menuSoundURL);
                menuSound = AudioSystem.getClip();
                menuSound.open(audioIn);
                // Atur volume jika perlu
                FloatControl volume = (FloatControl) menuSound.getControl(FloatControl.Type.MASTER_GAIN);
                volume.setValue(3.0f); // dB, bisa disesuaikan
            } else {
                System.err.println("menu.wav not found in resources!");
            }
        } catch (Exception e) {
            System.err.println("Failed to load menu sound: " + e.getMessage());
        }
    }

    // Method untuk memainkan sound menu
    private void playMenuSound() {
        try {
            if (menuSound != null) {
                menuSound.setFramePosition(0);
                menuSound.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("Failed to play menu sound: " + e.getMessage());
        }
    }

    // Method untuk menghentikan sound menu
    private void stopMenuSound() {
        if (menuSound != null && menuSound.isRunning()) {
            menuSound.stop();
        }
    }

    @Override
    public void dispose() {
        if (starTimer != null) {
            starTimer.stop();
        }
        if (menuSound != null) {
            menuSound.stop();
            menuSound.close();
        }
        super.dispose();
    }
}