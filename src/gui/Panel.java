package gui;

import observer_subject.Configs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class Panel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final BufferedImage image = new BufferedImage(Configs.TAMANHO_FRAME, Configs.TAMANHO_FRAME, BufferedImage.TYPE_INT_RGB);

	public Panel(){

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 10, 10, this);
	}

	public void addDot(Dot dot){
		Graphics g = image.getGraphics();
		g.setColor(new Color(dot.getR(),dot.getG(),dot.getB()));
		g.fillOval(dot.getX(), dot.getY(), dot.getSize(), dot.getSize());
		g.dispose();
	}

	// TODO
	public void removeDot(int x, int y){
		Graphics g = image.getGraphics();
		g.setColor(new Color(0,0,0));
		g.fillOval(x, y, 5, 5);
		g.dispose();
	}

	// TODO
	public void editDot(int x, int y){
		Graphics g = image.getGraphics();
		g.setColor(new Color(0,0,0));
		g.fillOval(x, y, 5, 5);
		g.dispose();
	}

}
