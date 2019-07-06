package gui;

import observer_subject.Configs;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class Frame extends JFrame{

	private static final long serialVersionUID = 1L;


	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.add(new Panel(), BorderLayout.CENTER);
		frame.setSize(500, 500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public Frame(Panel panel) {
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		this.setSize(Configs.TAMANHO_FRAME, Configs.TAMANHO_FRAME);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
