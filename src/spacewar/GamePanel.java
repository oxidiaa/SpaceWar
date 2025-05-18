package spacewar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import javax.imageio.ImageIO;
import java.io.File;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private int playerX;
    private int playerY;
    private int playerWidth = 60;
    private int playerHeight = 30;
    private boolean leftPressed = false, rightPressed = false, spacePressed = false, upPressed = false, downPressed = false;
    private ArrayList<Bullet> bullets = new ArrayList<>(50);
    private ArrayList<Asteroid> asteroids = new ArrayList<>(20);
    private ArrayList<Explosion> explosions = new ArrayList<>(10);
    private ArrayList<Particle> particles = new ArrayList<>(100);
    private ArrayList<Trail> trails = new ArrayList<>(20);
    private int score = 0;
    private boolean gameOver = false;
    private Random rand = new Random();
    private BufferedImage playerImg;
    private ArrayList<BufferedImage> asteroidImgs;
    private BufferedImage explosionImg;
    private Clip shootSound;
    private Clip explosionSound;
    private Clip dashboardSound;
    private Clip fireSound;
    private double angle = 0;
    private BufferedImage buffer;
    private Graphics2D bufferGraphics;
    private static final int MAX_BULLETS = 100;
    private static final int MAX_ASTEROIDS = 20;
    private static final int MAX_EXPLOSIONS = 20;
    private float starAngle = 0;
    private int shootCooldown = 0;
    private static final int SHOOT_COOLDOWN = 5;
    private static final int SPAWN_WARNING_TIME = 60; // Frames before asteroid spawn
    private ArrayList<SpawnWarning> spawnWarnings = new ArrayList<>();
    private BufferedImage backgroundImg;

    // Difficulty settings
    public enum Difficulty {
        EASY(6, 1.5, 1.5),    // player speed, enemy speed, score multiplier
        NORMAL(7, 2, 1.0),
        HARD(9, 3, 0.7);
        
        final int playerSpeed;
        final double enemySpeed;
        final double scoreMultiplier;
        
        Difficulty(int playerSpeed, double enemySpeed, double scoreMultiplier) {
            this.playerSpeed = playerSpeed;
            this.enemySpeed = enemySpeed;
            this.scoreMultiplier = scoreMultiplier;
        }
    }
    
    private Difficulty currentDifficulty = Difficulty.NORMAL;
    private JComboBox<Difficulty> difficultySelector;

    // Add health-related variables
    private int playerHealth = 3;
    private static final int MAX_PLAYER_HEALTH = 3;

    public GamePanel() {
        setFocusable(true);
        setPreferredSize(new Dimension(800, 600));
        addKeyListener(this);
        timer = new Timer(16, this);
        playerX = 370;
        playerY = 520;
        
        // Create difficulty selector
        difficultySelector = new JComboBox<>(Difficulty.values());
        difficultySelector.setSelectedItem(Difficulty.NORMAL);
        difficultySelector.addActionListener(e -> {
            currentDifficulty = (Difficulty) difficultySelector.getSelectedItem();
        });
        difficultySelector.setBounds(10, 10, 100, 25);
        add(difficultySelector);
        
        // Initialize buffer
        buffer = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        bufferGraphics = buffer.createGraphics();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bufferGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Load and optimize images
        try {
            // Load background first
            try {
                File backgroundFile = new File("src/resources/ingame.png");
                if (backgroundFile.exists()) {
                    backgroundImg = ImageIO.read(backgroundFile);
                    System.out.println("Background image loaded successfully from file");
                } else {
                    System.out.println("Background file not found at: " + backgroundFile.getAbsolutePath());
                    // Try loading from resources as fallback
                    URL backgroundURL = getClass().getResource("/resources/ingame.png");
                    if (backgroundURL != null) {
                        backgroundImg = ImageIO.read(backgroundURL);
                        System.out.println("Background image loaded successfully from resources");
                    } else {
                        System.out.println("Failed to find background image in resources");
                        backgroundImg = null;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading background: " + e.getMessage());
                e.printStackTrace();
                backgroundImg = null;
            }
            
            // Load other images
            try {
                playerImg = toBufferedImage(new ImageIcon(getClass().getResource("/resources/ship.png")).getImage());
                
                // Load multiple asteroid images
                asteroidImgs = new ArrayList<>();
                asteroidImgs.add(toBufferedImage(new ImageIcon(getClass().getResource("/resources/asteroid.png")).getImage()));
                asteroidImgs.add(toBufferedImage(new ImageIcon(getClass().getResource("/resources/asteroid1.png")).getImage()));
                asteroidImgs.add(toBufferedImage(new ImageIcon(getClass().getResource("/resources/asteroid2.png")).getImage()));
                
                explosionImg = toBufferedImage(new ImageIcon(getClass().getResource("/resources/explosion.png")).getImage());
            } catch (Exception e) {
                System.err.println("Error loading game images: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error in image loading process: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Load sounds with higher volume
        try {
            URL shootSoundURL = getClass().getResource("/resources/shoot.wav");
            URL explosionSoundURL = getClass().getResource("/resources/explosion.wav");
            URL dashboardSoundURL = getClass().getResource("/resources/dashboard.wav");
            URL fireSoundURL = getClass().getResource("/resources/fire.wav");
            
            if (shootSoundURL != null && explosionSoundURL != null && 
                dashboardSoundURL != null && fireSoundURL != null) {
                
                AudioInputStream shootAudio = AudioSystem.getAudioInputStream(shootSoundURL);
                AudioInputStream explosionAudio = AudioSystem.getAudioInputStream(explosionSoundURL);
                AudioInputStream dashboardAudio = AudioSystem.getAudioInputStream(dashboardSoundURL);
                AudioInputStream fireAudio = AudioSystem.getAudioInputStream(fireSoundURL);
                
                shootSound = AudioSystem.getClip();
                explosionSound = AudioSystem.getClip();
                dashboardSound = AudioSystem.getClip();
                fireSound = AudioSystem.getClip();
                
                shootSound.open(shootAudio);
                explosionSound.open(explosionAudio);
                dashboardSound.open(dashboardAudio);
                fireSound.open(fireAudio);
                
                // Set volume higher
                FloatControl shootVolume = (FloatControl) shootSound.getControl(FloatControl.Type.MASTER_GAIN);
                FloatControl explosionVolume = (FloatControl) explosionSound.getControl(FloatControl.Type.MASTER_GAIN);
                FloatControl dashboardVolume = (FloatControl) dashboardSound.getControl(FloatControl.Type.MASTER_GAIN);
                FloatControl fireVolume = (FloatControl) fireSound.getControl(FloatControl.Type.MASTER_GAIN);
                
                shootVolume.setValue(6.0f);
                explosionVolume.setValue(6.0f);
                dashboardVolume.setValue(6.0f);
                fireVolume.setValue(6.0f);
                
                // Start dashboard sound
                dashboardSound.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Clear buffer
        bufferGraphics.setColor(new Color(0, 0, 0, 0));
        bufferGraphics.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw space background
        drawSpaceBackground();
        
        if (!gameOver) {
            // Draw trails
            for (Trail trail : trails) {
                trail.draw(bufferGraphics);
            }
            
            // Draw particles
            for (Particle p : particles) {
                p.draw(bufferGraphics);
            }
            
            // Draw player with glow effect
            drawPlayerWithGlow();
            
            // Draw bullets with trail
            bufferGraphics.setColor(Color.YELLOW);
            for (Bullet b : bullets) {
                // Draw bullet trail
                GradientPaint trailGradient = new GradientPaint(
                    b.x, b.y, new Color(255, 255, 0, 150),
                    b.x - b.dx * 2, b.y - b.dy * 2, new Color(255, 255, 0, 0)
                );
                bufferGraphics.setPaint(trailGradient);
                bufferGraphics.fillRect(b.x - b.dx * 2, b.y - b.dy * 2, 4, 12);
                
                // Draw bullet
                bufferGraphics.setColor(Color.YELLOW);
                bufferGraphics.fillRect(b.x, b.y, 4, 12);
            }
            
            // Draw asteroids with health
            for (Asteroid a : asteroids) {
                // Draw asteroid glow
                RadialGradientPaint glow = new RadialGradientPaint(
                    a.x + a.size/2, a.y + a.size/2, a.size,
                    new float[]{0.0f, 0.7f, 1.0f},
                    new Color[]{
                        new Color(100, 100, 100, 100),
                        new Color(50, 50, 50, 50),
                        new Color(0, 0, 0, 0)
                    }
                );
                bufferGraphics.setPaint(glow);
                bufferGraphics.fillOval(a.x - 10, a.y - 10, a.size + 20, a.size + 20);
                
                // Draw asteroid with random image
                if (asteroidImgs != null && !asteroidImgs.isEmpty()) {
                    bufferGraphics.drawImage(asteroidImgs.get(a.imageIndex), a.x, a.y, a.size, a.size, null);
                }
                
                // Draw health number
                bufferGraphics.setColor(Color.WHITE);
                bufferGraphics.setFont(new Font("Arial", Font.BOLD, 16));
                String healthStr = String.valueOf(a.health);
                FontMetrics fm = bufferGraphics.getFontMetrics();
                int healthX = a.x + (a.size - fm.stringWidth(healthStr)) / 2;
                int healthY = a.y + (a.size + fm.getAscent()) / 2;
                bufferGraphics.drawString(healthStr, healthX, healthY);
            }
            
            // Draw explosions with particles
            for (Explosion e : explosions) {
                // Draw explosion glow
                RadialGradientPaint explosionGlow = new RadialGradientPaint(
                    e.x + e.size/2, e.y + e.size/2, e.size,
                    new float[]{0.0f, 0.5f, 1.0f},
                    new Color[]{
                        new Color(255, 100, 0, 150),
                        new Color(255, 50, 0, 100),
                        new Color(255, 0, 0, 0)
                    }
                );
                bufferGraphics.setPaint(explosionGlow);
                bufferGraphics.fillOval(e.x - e.size/2, e.y - e.size/2, e.size * 2, e.size * 2);
                
                // Draw explosion
                bufferGraphics.drawImage(explosionImg, e.x, e.y, e.size, e.size, null);
            }
            
            // Draw spawn warnings
            for (SpawnWarning warning : spawnWarnings) {
                warning.draw(bufferGraphics);
            }
            
            // Draw score with glow
            bufferGraphics.setColor(new Color(255, 255, 255, 200));
            bufferGraphics.setFont(new Font("Arial", Font.BOLD, 24));
            String scoreText = "Score: " + score;
            FontMetrics fm = bufferGraphics.getFontMetrics();
            int scoreX = 20;
            int scoreY = 40;
            
            // Draw score glow
            bufferGraphics.setColor(new Color(255, 255, 255, 100));
            bufferGraphics.drawString(scoreText, scoreX - 2, scoreY - 2);
            bufferGraphics.drawString(scoreText, scoreX + 2, scoreY + 2);
            
            // Draw score
            bufferGraphics.setColor(Color.WHITE);
            bufferGraphics.drawString(scoreText, scoreX, scoreY);
            
            // Draw difficulty info
            bufferGraphics.setColor(new Color(255, 255, 255, 200));
            bufferGraphics.setFont(new Font("Arial", Font.BOLD, 16));
            String difficultyText = "Difficulty: " + currentDifficulty.name();
            bufferGraphics.drawString(difficultyText, getWidth() - 150, 30);
            
            // Draw player health
            bufferGraphics.setColor(new Color(255, 255, 255, 200));
            bufferGraphics.setFont(new Font("Arial", Font.BOLD, 24));
            String healthText = "Health: " + playerHealth;
            bufferGraphics.drawString(healthText, 20, 70);
        } else {
            // Draw game over with effects
            bufferGraphics.setColor(new Color(255, 0, 0, 200));
            bufferGraphics.setFont(new Font("Arial", Font.BOLD, 48));
            String gameOverText = "GAME OVER";
            FontMetrics fm = bufferGraphics.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(gameOverText)) / 2;
            int y = getHeight() / 2;
            
            // Draw game over glow
            for (int i = 0; i < 5; i++) {
                bufferGraphics.setColor(new Color(255, 0, 0, 100 - i * 20));
                bufferGraphics.drawString(gameOverText, x - i, y - i);
                bufferGraphics.drawString(gameOverText, x + i, y + i);
            }
            
            // Draw game over text
            bufferGraphics.setColor(Color.RED);
            bufferGraphics.drawString(gameOverText, x, y);
            
            // Draw final score
            bufferGraphics.setFont(new Font("Arial", Font.BOLD, 24));
            String scoreText = "Final Score: " + score;
            fm = bufferGraphics.getFontMetrics();
            x = (getWidth() - fm.stringWidth(scoreText)) / 2;
            y += 50;
            
            // Draw score glow
            bufferGraphics.setColor(new Color(255, 255, 255, 100));
            bufferGraphics.drawString(scoreText, x - 2, y - 2);
            bufferGraphics.drawString(scoreText, x + 2, y + 2);
            
            // Draw score
            bufferGraphics.setColor(Color.WHITE);
            bufferGraphics.drawString(scoreText, x, y);
            
            // Draw continue text
            bufferGraphics.setFont(new Font("Arial", Font.BOLD, 18));
            String continueText = "Press ENTER to return to menu";
            fm = bufferGraphics.getFontMetrics();
            x = (getWidth() - fm.stringWidth(continueText)) / 2;
            y += 40;
            
            // Draw continue text glow
            bufferGraphics.setColor(new Color(255, 255, 255, 100));
            bufferGraphics.drawString(continueText, x - 1, y - 1);
            bufferGraphics.drawString(continueText, x + 1, y + 1);
            
            // Draw continue text
            bufferGraphics.setColor(Color.WHITE);
            bufferGraphics.drawString(continueText, x, y);
        }
        
        // Draw buffer to screen
        g.drawImage(buffer, 0, 0, null);
    }

    private void drawSpaceBackground() {
        if (backgroundImg != null) {
            bufferGraphics.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Fallback to black background if image loading fails
            bufferGraphics.setColor(Color.BLACK);
            bufferGraphics.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    private void drawPlayerWithGlow() {
        // Draw engine glow
        double engineX = playerX + playerWidth/2 + Math.cos(angle + Math.PI) * playerWidth/2;
        double engineY = playerY + playerHeight/2 + Math.sin(angle + Math.PI) * playerWidth/2;
        
        RadialGradientPaint engineGlow = new RadialGradientPaint(
            (float)engineX, (float)engineY, playerWidth,
            new float[]{0.0f, 0.5f, 1.0f},
            new Color[]{
                new Color(255, 100, 0, 150),
                new Color(255, 50, 0, 100),
                new Color(255, 0, 0, 0)
            }
        );
        bufferGraphics.setPaint(engineGlow);
        bufferGraphics.fillOval((int)engineX - playerWidth, (int)engineY - playerWidth, 
                              playerWidth * 2, playerHeight * 2);
        
        // Draw ship glow
        RadialGradientPaint shipGlow = new RadialGradientPaint(
            playerX + playerWidth/2, playerY + playerHeight/2,
            playerWidth,
            new float[]{0.0f, 0.7f, 1.0f},
            new Color[]{
                new Color(100, 100, 255, 100),
                new Color(50, 50, 200, 50),
                new Color(0, 0, 0, 0)
            }
        );
        bufferGraphics.setPaint(shipGlow);
        bufferGraphics.fillOval(playerX - playerWidth/2, playerY - playerHeight/2, 
                              playerWidth * 2, playerHeight * 2);
        
        // Save the current transform
        AffineTransform oldTransform = bufferGraphics.getTransform();
        
        // Move to center of ship
        bufferGraphics.translate(playerX + playerWidth/2, playerY + playerHeight/2);
        // Rotate around center
        bufferGraphics.rotate(angle);
        // Draw ship centered
        bufferGraphics.drawImage(playerImg, -playerWidth/2, -playerHeight/2, playerWidth, playerHeight, null);
        
        // Restore the transform
        bufferGraphics.setTransform(oldTransform);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            // Player movement with difficulty-based speed
            int moveDx = 0, moveDy = 0;
            if (leftPressed) moveDx -= currentDifficulty.playerSpeed;
            if (rightPressed) moveDx += currentDifficulty.playerSpeed;
            if (upPressed) moveDy -= currentDifficulty.playerSpeed;
            if (downPressed) moveDy += currentDifficulty.playerSpeed;
            
            // Update angle based on movement
            if (moveDx != 0 || moveDy != 0) {
                angle = Math.atan2(moveDy, moveDx);
                // Add trail
                if (trails.size() < 20) {
                    trails.add(new Trail(playerX + playerWidth/2, playerY + playerHeight/2));
                }
            }
            
            playerX += moveDx;
            playerY += moveDy;
            playerX = Math.max(0, Math.min(getWidth() - playerWidth, playerX));
            playerY = Math.max(0, Math.min(getHeight() - playerHeight, playerY));

            // Update trails
            Iterator<Trail> trailIt = trails.iterator();
            while (trailIt.hasNext()) {
                Trail trail = trailIt.next();
                trail.update();
                if (trail.isDead()) {
                    trailIt.remove();
                }
            }

            // Update particles
            Iterator<Particle> particleIt = particles.iterator();
            while (particleIt.hasNext()) {
                Particle p = particleIt.next();
                p.update();
                if (p.isDead()) {
                    particleIt.remove();
                }
            }

            // Update bullets
            Iterator<Bullet> it = bullets.iterator();
            while (it.hasNext()) {
                Bullet b = it.next();
                b.x += b.dx;
                b.y += b.dy;
                if (b.x < 0 || b.x > getWidth() || b.y < 0 || b.y > getHeight()) {
                    it.remove();
                }
            }

            // Update explosions
            Iterator<Explosion> expIt = explosions.iterator();
            while (expIt.hasNext()) {
                Explosion exp = expIt.next();
                exp.lifetime--;
                if (exp.lifetime <= 0) {
                    expIt.remove();
                }
            }

            // Update spawn warnings
            Iterator<SpawnWarning> warningIt = spawnWarnings.iterator();
            while (warningIt.hasNext()) {
                SpawnWarning warning = warningIt.next();
                warning.update();
                if (warning.isDead()) {
                    warningIt.remove();
                }
            }

            // Update asteroids with difficulty-based speed
            Iterator<Asteroid> ait = asteroids.iterator();
            while (ait.hasNext()) {
                Asteroid a = ait.next();
                double dx = playerX - a.x;
                double dy = playerY - a.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > 0) {
                    a.x += (int) (dx / dist * currentDifficulty.enemySpeed);
                    a.y += (int) (dy / dist * currentDifficulty.enemySpeed);
                }

                // Collision with player
                if (a.y + a.size > playerY && a.x < playerX + playerWidth && 
                    a.x + a.size > playerX && a.y < playerY + playerHeight) {
                    playerHealth--;
                    if (playerHealth <= 0) {
                        gameOver = true;
                        // Create explosion particles
                        for (int i = 0; i < 50; i++) {
                            particles.add(new Particle(playerX + playerWidth/2, playerY + playerHeight/2));
                        }
                        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                        if (topFrame instanceof SpaceWarGame spaceWarGame) {
                            spaceWarGame.saveScore(score);
                        }
                    }
                    ait.remove();
                    continue;
                }

                // Collision with bullets
                Iterator<Bullet> bit = bullets.iterator();
                while (bit.hasNext()) {
                    Bullet b = bit.next();
                    if (b.x > a.x && b.x < a.x + a.size && 
                        b.y > a.y && b.y < a.y + a.size) {
                        a.health--;
                        bit.remove();
                        
                        if (a.health <= 0) {
                            // Handle different enemy types
                            switch(a.type) {
                                case 0: // Normal enemy
                                    createExplosion(a);
                                    break;
                                case 1: // Boss enemy
                                    createExplosion(a);
                                    break;
                            }
                            
                            if (explosionSound != null) {
                                explosionSound.setFramePosition(0);
                                explosionSound.start();
                            }
                            ait.remove();
                            score += (int)(10 * currentDifficulty.scoreMultiplier);
                            break;
                        }
                    }
                }

                // Remove asteroid if out of screen
                if (a.x < -a.size || a.x > getWidth() || 
                    a.y < -a.size || a.y > getHeight()) {
                    ait.remove();
                }
            }

            // Spawn asteroids based on difficulty and type
            int spawnChance = switch (currentDifficulty) {
                case EASY -> 15;
                case NORMAL -> 20;
                case HARD -> 25;
            };
            
            if (rand.nextInt(spawnChance) == 0 && asteroids.size() < MAX_ASTEROIDS) {
                int side = rand.nextInt(4);
                // Add spawn warning
                spawnWarnings.add(new SpawnWarning(side, SPAWN_WARNING_TIME));
            }

            // Spawn asteroid when warning is about to expire
            Iterator<SpawnWarning> spawnIt = spawnWarnings.iterator();
            while (spawnIt.hasNext()) {
                SpawnWarning warning = spawnIt.next();
                if (warning.timeLeft == 1) { // Spawn on last frame of warning
                    int size = 40 + rand.nextInt(30);
                    // Randomly choose enemy type
                    int type = rand.nextInt(100);
                    int enemyType = type < 90 ? 0 : 1; // 90% normal, 10% boss
                    asteroids.add(new Asteroid(warning.spawnX, warning.spawnY, size, enemyType));
                }
            }

            // Shoot (with limit and cooldown)
            if (spacePressed && bullets.size() < MAX_BULLETS) {
                if (shootCooldown <= 0) {
                    // Normalize the direction vector to ensure consistent speed
                    double speed = 20.0; // Increased base speed
                    
                    // Calculate shooting direction based on current angle
                    double shootAngle = angle;
                    double dx = Math.cos(shootAngle);
                    double dy = Math.sin(shootAngle);
                    
                    // Create multiple bullets in a spread pattern
                    for (int i = -1; i <= 1; i++) {
                        double spreadAngle = shootAngle + (i * 0.1); // 0.1 radians spread
                        double spreadDx = Math.cos(spreadAngle) * speed;
                        double spreadDy = Math.sin(spreadAngle) * speed;
                        
                        // Calculate bullet spawn position at the front of the ship
                        double bulletX = playerX + playerWidth/2 + dx * playerWidth/2;
                        double bulletY = playerY + playerHeight/2 + dy * playerHeight/2;
                        
                        // Create bullet with normalized direction
                        double magnitude = Math.sqrt(spreadDx * spreadDx + spreadDy * spreadDy);
                        if (magnitude > 0) {
                            spreadDx = (spreadDx / magnitude) * speed;
                            spreadDy = (spreadDy / magnitude) * speed;
                        }
                        
                        // Ensure bullet spawns at the correct position
                        if (moveDx != 0 || moveDy != 0) {
                            bulletX = playerX + playerWidth/2 + dx * playerWidth/2;
                            bulletY = playerY + playerHeight/2 + dy * playerHeight/2;
                        }
                        
                        bullets.add(new Bullet(
                            (int)bulletX, 
                            (int)bulletY, 
                            (int)spreadDx, (int)spreadDy));
                    }
                    
                    if (shootSound != null) {
                        shootSound.setFramePosition(0);
                        shootSound.start();
                    }
                    
                    if (fireSound != null) {
                        fireSound.setFramePosition(0);
                        fireSound.start();
                    }
                    
                    shootCooldown = SHOOT_COOLDOWN;
                }
            }
            
            if (shootCooldown > 0) {
                shootCooldown--;
            }
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = true;
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = true;
            if (e.getKeyCode() == KeyEvent.VK_SPACE) spacePressed = true;
            if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = true;
            if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = true;
        } else {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                // Kembali ke menu
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (topFrame instanceof SpaceWarGame) {
                    ((SpaceWarGame) topFrame).showMenu();
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_SPACE) spacePressed = false;
        if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // Bullet class
    private static class Bullet {
        int x, y, dx, dy;
        Bullet(int x, int y, int dx, int dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }
    }

    // Asteroid class
    private static class Asteroid {
        int x, y, size;
        int health;
        int type; // 0: normal, 1: boss
        int imageIndex; // Index for asteroid image
        
        Asteroid(int x, int y, int size, int type) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.type = type;
            this.imageIndex = new Random().nextInt(3); // Random image index
            
            // Set random health based on type
            Random rand = new Random();
            switch(type) {
                case 0: // Normal enemy
                    this.health = 3 + rand.nextInt(8); // 3-10 health
                    break;
                case 1: // Boss enemy
                    this.health = 20 + rand.nextInt(21); // 20-40 health
                    break;
            }
        }
    }

    // Explosion class
    private static class Explosion {
        int x, y, size, lifetime;
        Explosion(int x, int y, int size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.lifetime = 20;
        }
    }

    // Particle class for explosion effects
    private class Particle {
        float x, y;
        float dx, dy;
        float size;
        float alpha;
        Color color;
        
        Particle(float x, float y) {
            this.x = x;
            this.y = y;
            this.dx = (float) (Math.random() * 12 - 6); // Increased speed
            this.dy = (float) (Math.random() * 12 - 6); // Increased speed
            this.size = (float) (Math.random() * 6) + 2; // Larger particles
            this.alpha = 1.0f;
            // Random color between orange and red
            this.color = new Color(
                255,
                (int)(Math.random() * 100),
                0
            );
        }
        
        void update() {
            x += dx;
            y += dy;
            alpha -= 0.015f; // Slower fade
            size *= 0.98f; // Slower size reduction
        }
        
        void draw(Graphics2D g) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255)));
            g.fillOval((int)x, (int)y, (int)size, (int)size);
        }
        
        boolean isDead() {
            return alpha <= 0 || size <= 0.5f;
        }
    }

    // Trail class for ship movement
    private class Trail {
        float x, y;
        float alpha;
        float size;
        
        Trail(float x, float y) {
            this.x = x;
            this.y = y;
            this.alpha = 1.0f;
            this.size = 10;
        }
        
        void update() {
            alpha -= 0.05f;
            size *= 0.95f;
        }
        
        void draw(Graphics2D g) {
            g.setColor(new Color(100, 100, 255, (int)(alpha * 255)));
            g.fillOval((int)(x - size/2), (int)(y - size/2), (int)size, (int)size);
        }
        
        boolean isDead() {
            return alpha <= 0 || size <= 1;
        }
    }

    // Add SpawnWarning class
    private static class SpawnWarning {
        int x, y;
        int side; // 0: top, 1: right, 2: bottom, 3: left
        int timeLeft;
        int spawnX, spawnY; // Actual spawn position
        private static final Random random = new Random();
        
        SpawnWarning(int side, int timeLeft) {
            this.side = side;
            this.timeLeft = timeLeft;
            
            // Calculate actual spawn position
            int size = 40 + random.nextInt(30);
            switch (side) {
                case 0: // top
                    spawnX = random.nextInt(800 - size);
                    spawnY = -size;
                    x = spawnX + size/2;
                    y = 20;
                    break;
                case 1: // right
                    spawnX = 800;
                    spawnY = random.nextInt(600 - size);
                    x = 780;
                    y = spawnY + size/2;
                    break;
                case 2: // bottom
                    spawnX = random.nextInt(800 - size);
                    spawnY = 600;
                    x = spawnX + size/2;
                    y = 580;
                    break;
                case 3: // left
                    spawnX = -size;
                    spawnY = random.nextInt(600 - size);
                    x = 20;
                    y = spawnY + size/2;
                    break;
            }
        }
        
        void draw(Graphics2D g) {
            // Draw warning arrow
            g.setColor(new Color(255, 0, 0, (int)((timeLeft / (float)SPAWN_WARNING_TIME) * 255)));
            int[] xPoints = new int[3];
            int[] yPoints = new int[3];
            
            // Calculate arrow size based on time left
            int arrowSize = 15 + (int)((1 - timeLeft / (float)SPAWN_WARNING_TIME) * 10);
            
            switch (side) {
                case 0: // top
                    xPoints = new int[]{x - arrowSize, x, x + arrowSize};
                    yPoints = new int[]{y + arrowSize, y, y + arrowSize};
                    break;
                case 1: // right
                    xPoints = new int[]{x - arrowSize, x, x - arrowSize};
                    yPoints = new int[]{y - arrowSize, y, y + arrowSize};
                    break;
                case 2: // bottom
                    xPoints = new int[]{x - arrowSize, x, x + arrowSize};
                    yPoints = new int[]{y - arrowSize, y, y - arrowSize};
                    break;
                case 3: // left
                    xPoints = new int[]{x + arrowSize, x, x + arrowSize};
                    yPoints = new int[]{y - arrowSize, y, y + arrowSize};
                    break;
            }
            
            g.fillPolygon(xPoints, yPoints, 3);
            
            // Draw pulsing circle at spawn position
            float pulseSize = 1.0f + (float)Math.sin(timeLeft * 0.2) * 0.2f;
            g.setColor(new Color(255, 0, 0, (int)((timeLeft / (float)SPAWN_WARNING_TIME) * 100)));
            g.fillOval(spawnX - 10, spawnY - 10, 20, 20);
            
            // Draw line connecting arrow to spawn point
            g.setColor(new Color(255, 0, 0, (int)((timeLeft / (float)SPAWN_WARNING_TIME) * 100)));
            g.setStroke(new BasicStroke(2));
            g.drawLine(x, y, spawnX, spawnY);
        }
        
        void update() {
            timeLeft--;
        }
        
        boolean isDead() {
            return timeLeft <= 0;
        }
    }

    public int getScore() { return score; }
    public boolean isGameOver() { return gameOver; }

    // Add getter for current difficulty
    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }
    
    // Add setter for current difficulty
    public void setDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
        difficultySelector.setSelectedItem(difficulty);
    }

    private void createExplosion(Asteroid a) {
        // Create multiple explosions
        for (int i = 0; i < 3; i++) {
            if (explosions.size() < MAX_EXPLOSIONS) {
                explosions.add(new Explosion(
                    a.x + (int)(Math.random() * a.size),
                    a.y + (int)(Math.random() * a.size),
                    a.size/2 + (int)(Math.random() * a.size/2)
                ));
            }
        }
        // Create more explosion particles
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(a.x + a.size/2, a.y + a.size/2));
        }
    }

    public void stopDashboardSound() {
        if (dashboardSound != null) {
            dashboardSound.stop();
        }
    }
}
