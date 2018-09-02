	import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GUI extends JFrame {
	private int width, height ,it;
	int perc_width;
	private drawingsetup draw;
	
	    public GUI(int width, int height, int iterations) {
	    	this.width = width;
	    	this.height = height;
	    	it = iterations;
	    	perc_width= (int) (width / 100.0);
	        initUI(0);
	    }
	    
	    public void load(int percent) {
	    	int w = (int) (percent / 100.0 * width);
	    	draw.changeRect(0, w);
	    }
	    
	    private void initUI(int percent) {
	    	
	    	draw = new drawingsetup();
	        add(draw);
	        draw.addRect(0, 0, 0, height);
	        
	        
	        setTitle("Progress Bar");
	        setSize(width, height);
	        setLocationRelativeTo(null);
	        setDefaultCloseOperation(EXIT_ON_CLOSE);
	    }
}

class Rectangle {
	int x,y,width,height;
	Rectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}

class drawingsetup extends JPanel {
	private ArrayList<Rectangle> bars = new ArrayList<Rectangle>();
	
    public void addRect(int x, int y, int width, int height) {
    	bars.add(new Rectangle(x, y, width, height));
    	repaint();
    }
    
    public void changeRect(int index, int width) {
    	bars.get(index).width = width;
    	repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setColor(new Color(0, 200, 0));
        
        for(Rectangle bar : bars)
        g2d.fillRect(bar.x, bar.y, bar.width, bar.height);
    }
}
