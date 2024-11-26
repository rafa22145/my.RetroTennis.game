package com.Rafa.retrotennis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class TennisGame extends JPanel {  

	    public TennisGame() {  
	        setPreferredSize(new Dimension(800, 600));  
	        setBackground(Color.BLACK);  
	    }  

	    protected void paintComponent(Graphics g) {  
	        super.paintComponent(g);  
	        // You can draw the court and the paddles here  
	    }  

	    public static void main(String[] args) {  
	        JFrame frame = new JFrame("Retro Tennis Game");  
	        TennisGame game = new TennisGame();  
	        frame.add(game);  
	        frame.pack();  
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	        frame.setVisible(true);  
	    }
}

