package com.Rafa.retrotennis;

import javax.swing.*;  
import java.awt.*;  
import java.awt.event.*;  

public class Game extends JPanel implements Runnable {  
    private static final int PANEL_WIDTH = 800;  
    private static final int PANEL_HEIGHT = 600;  
    private static final int RACKET_WIDTH = 15;  
    private static final int RACKET_LENGTH = 100;  
    
    private int player1Score = 0;  
    private int computerScore = 0;  
    private boolean gameEnded = false;  

    private int racket1Y;  
    private int racket2Y;  
    private int ballX;  
    private int ballY;  
    private int ballXSpeed;  
    private int ballYSpeed;  
    private int ballSpeed = 3; // Initial speed of the ball  
    private long startTime; // To track the start time  

    // Ability variables  
    private boolean slowAbilityActive = false;  
    private boolean enlargeBallActive = false;  
    private boolean loseControlActive = false;  

    private long slowAbilityEndTime;  
    private long enlargeBallEndTime;  
    private long loseControlEndTime;  

    private long abilityCooldown = 5000; // 5 seconds cooldown for abilities  
    private long lastAbilityUsedTime = 0;  

    public Game() {  
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));  
        setBackground(Color.BLACK); // Set the background color to black  
        racket1Y = (PANEL_HEIGHT - RACKET_LENGTH) / 2;  
        racket2Y = (PANEL_HEIGHT - RACKET_LENGTH) / 2;  
        resetBall();  
        startTime = System.currentTimeMillis(); // Initialize start time  
        
        // KeyListener to handle control of player 1's racket and abilities  
        addKeyListener(new KeyAdapter() {  
            @Override  
            public void keyPressed(KeyEvent e) {  
                if (e.getKeyCode() == KeyEvent.VK_UP) {  
                    moveRacket1Up();  
                }  
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {  
                    moveRacket1Down();  
                }  
                if (e.getKeyCode() == KeyEvent.VK_X) {  
                    restartGame(); // Restart the game on pressing 'X'  
                }  
                if (e.getKeyCode() == KeyEvent.VK_1) {  
                    activateSlowAbility(); // Activate slow ability  
                }  
                if (e.getKeyCode() == KeyEvent.VK_2) {  
                    activateEnlargeBallAbility(); // Activate enlarge ball ability  
                }  
                if (e.getKeyCode() == KeyEvent.VK_3) {  
                    activateLoseControlAbility(); // Activate lose control ability  
                }  
            }  
        });  
        setFocusable(true);  
    }  

    // Draw the player's and AI's rackets, the ball, and scores  
    private void drawRacket(Graphics g, int x, int y) {  
        g.setColor(Color.WHITE); // Racket color  
        g.fillRect(x, y, RACKET_WIDTH, RACKET_LENGTH);   
    }  

    private void drawBall(Graphics g) {  
        g.setColor(Color.RED); // Ball color  
        int ballSize = enlargeBallActive ? 30 : 20; // Change size if enlarged  
        g.fillOval(ballX - ballSize / 2, ballY - ballSize / 2, ballSize, ballSize);   
    }  

    private void drawScores(Graphics g) {  
        g.setColor(Color.WHITE); // Score color  
        g.setFont(new Font("Arial", Font.BOLD, 30));   
        g.drawString("Player 1: " + player1Score, 50, 50);   
        g.drawString("Computer: " + computerScore, PANEL_WIDTH - 200, 50);   

        // Calculate elapsed time  
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // time in seconds  
        g.drawString("Time: " + elapsedTime + "s", PANEL_WIDTH / 2 - 50, 50); // Display timer  

        // Draw ability status  
        g.drawString("Abilities: 1: Slow, 2: Enlarge, 3: Lose Control", 50, PANEL_HEIGHT - 30);  
        if (slowAbilityActive) {  
            g.drawString("Slow Active!", 50, PANEL_HEIGHT - 60);  
        }  
        if (enlargeBallActive) {  
            g.drawString("Ball Enlarged!", 200, PANEL_HEIGHT - 60);  
        }  
        if (loseControlActive) {  
            g.drawString("AI Lost Control!", 400, PANEL_HEIGHT - 60);  
        }  

        if (gameEnded) {  
            String message = (player1Score >= 10) ? "Player 1 Won! Press X to Restart." : "Computer Won! Press X to Restart.";  
            g.drawString(message, PANEL_WIDTH / 2 - 150, PANEL_HEIGHT / 2);   
        }  
    }  

    // Move the first player's racket up  
    private void moveRacket1Up() {  
        if (!gameEnded) {  
            racket1Y = Math.max(0, racket1Y - 10);   
            repaint();   
        }  
    }  

    // Move the first player's racket down  
    private void moveRacket1Down() {  
        if (!gameEnded) {  
            racket1Y = Math.min(PANEL_HEIGHT - RACKET_LENGTH, racket1Y + 10);   
            repaint();   
        }  
    }  

    // Computer moves the racket based on the ball's position  
    private void moveRacket2() {  
        if (!gameEnded) {  
            if (ballX >= PANEL_WIDTH / 2 && ballXSpeed > 0) {   
                // Slow down AI if ability is active  
                int speed = (slowAbilityActive) ? 2 : 5; // Slow AI if ability is active  
                if (ballY < racket2Y + RACKET_LENGTH / 4) {  
                    racket2Y = Math.max(0, racket2Y - speed); // Move up  
                } else if (ballY > racket2Y + RACKET_LENGTH * 3 / 4) {  
                    racket2Y = Math.min(PANEL_HEIGHT - RACKET_LENGTH, racket2Y + speed); // Move down  
                }  
            }  
        }  
    }  

    // Update the ball position and handle collisions  
    private void updateBall() {  
        ballX += ballXSpeed;   
        ballY += ballYSpeed;  

        // Bounce off top and bottom walls  
        if (ballY <= 0 || ballY >= PANEL_HEIGHT) {  
            ballYSpeed = -ballYSpeed;   
        }  

        // Bounce off the rackets  
        if (ballX <= 60 && ballY >= racket1Y && ballY <= racket1Y + RACKET_LENGTH) {  
            ballXSpeed = -ballXSpeed;   
        }  
        if (ballX >= PANEL_WIDTH - 60 && ballY >= racket2Y && ballY <= racket2Y + RACKET_LENGTH) {  
            ballXSpeed = -ballXSpeed;   
        }  

        // Reset ball if it goes out of bounds  
        if (ballX < 0) {  
            computerScore++;   
            checkGameEnd();  
            resetBall();  
            resetAbilities(); // Reset abilities on scoring  
        } else if (ballX > PANEL_WIDTH) {  
            player1Score++;   
            checkGameEnd();  
            resetBall();  
            resetAbilities(); // Reset abilities on scoring  
        }  
    }  

    // Check if the game has ended  
    private void checkGameEnd() {  
        if (player1Score >= 10 || computerScore >= 10) {  
            gameEnded = true;   
        }  
    }  

    // Reset the ball to the center  
    private void resetBall() {  
        ballX = PANEL_WIDTH / 2;  
        ballY = PANEL_HEIGHT / 2;  
        ballXSpeed = (Math.random() < 0.5) ? ballSpeed : -ballSpeed; // Use ballSpeed  
        ballYSpeed = (Math.random() < 0.5) ? ballSpeed : -ballSpeed;   
    }  

    // Restart the game after someone wins  
    private void restartGame() {  
        player1Score = 0;   
        computerScore = 0;   
        gameEnded = false;   
        racket1Y = (PANEL_HEIGHT - RACKET_LENGTH) / 2;   
        racket2Y = (PANEL_HEIGHT - RACKET_LENGTH) / 2;   
        ballSpeed = 3; // Reset ball speed  
        startTime = System.currentTimeMillis(); // Reset start time  
        resetBall();   
        resetAbilities(); // Reset abilities when restarting  
        repaint();   
    }  

    // Activate the slow ability  
    private void activateSlowAbility() {  
        if (System.currentTimeMillis() - lastAbilityUsedTime >= abilityCooldown) {  
            slowAbilityActive = true;  
            slowAbilityEndTime = System.currentTimeMillis() + 5000; // 5 seconds active  
            lastAbilityUsedTime = System.currentTimeMillis(); // Update last used time  
        }  
    }  

    // Activate the enlarge ball ability  
    private void activateEnlargeBallAbility() {  
        if (System.currentTimeMillis() - lastAbilityUsedTime >= abilityCooldown) {  
            enlargeBallActive = true;  
            enlargeBallEndTime = System.currentTimeMillis() + 5000; // 5 seconds active  
            lastAbilityUsedTime = System.currentTimeMillis(); // Update last used time  
        }  
    }  

    // Activate the lose control ability  
    private void activateLoseControlAbility() {  
        if (System.currentTimeMillis() - lastAbilityUsedTime >= abilityCooldown) {  
            loseControlActive = true;  
            loseControlEndTime = System.currentTimeMillis() + 5000; // 5 seconds active  
            lastAbilityUsedTime = System.currentTimeMillis(); // Update last used time  
        }  
    }  

    // Reset abilities  
    private void resetAbilities() {  
        slowAbilityActive = false;  
        enlargeBallActive = false;  
        loseControlActive = false;  
    }  

    @Override  
    protected void paintComponent(Graphics g) {  
        super.paintComponent(g);  
        drawRacket(g, 50, racket1Y);   
        drawRacket(g, PANEL_WIDTH - 60, racket2Y);   
        drawBall(g);   
        drawScores(g);   
    }  

    public void run() {  
        while (true) {  
            // Check if abilities have expired  
            long currentTime = System.currentTimeMillis();  
            if (currentTime > slowAbilityEndTime) {  
                slowAbilityActive = false;  
            }  
            if (currentTime > enlargeBallEndTime) {  
                enlargeBallActive = false;  
            }  
            if (currentTime > loseControlEndTime) {  
                loseControlActive = false;  
            }  

            moveRacket2(); // Update AI racket position  
            updateBall();   
            repaint();   
            try {  
                Thread.sleep(16); // Roughly 60 FPS  
            } catch (InterruptedException e) {  
                e.printStackTrace();  
            }  
        }  
    }  

    public static void main(String[] args) {  
        JFrame frame = new JFrame("Retro Tennis Game");  
        Game game = new Game();  
        frame.add(game);  
        frame.pack();  
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        frame.setVisible(true);  
        
        new Thread(game).start(); // Start the game loop in a new thread  
    }  
}