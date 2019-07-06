package gui;

import observer_subject.Configs;
import observer_subject.ConsoleColors;

import java.io.Serializable;
import java.util.Random;

public class Dot implements Serializable{

	private static final long serialVersionUID = 2253164140780319969L;

	private static int CONSTANT = 123455;

	private int id;
	private int x;
	private int y;
	private int [] color;
	private int size;

	public Dot(){
		this.id = x*CONSTANT+y;

		this.color = new int[3];

		this.setR(new Random().nextInt(256));
		this.setG(new Random().nextInt(256));
		this.setB(new Random().nextInt(256));

		this.setX(new Random().nextInt(Configs.TAMANHO_FRAME));
		this.setY(new Random().nextInt(Configs.TAMANHO_FRAME));

		size = new Random().nextInt(10) + 1;
	}

	public Dot(int x, int y, int [] color, int size){
		this.id = x*CONSTANT+y;
		this.x = x;
		this.y = y;
		this.color = color;
		this.size = size;
	}

	public void printDot() {
		System.out.println(ConsoleColors.RED_BACKGROUND + ConsoleColors.BLACK);
		System.out.println("x: " + x + "  y: " + y);
		System.out.println("r: " + color[0] + "  g: " + color[1] + "  b: " + color[2]);
		System.out.println(ConsoleColors.RESET);
	}


	/**********************
	* GETTERS AND SETTERS
	**********************/

	public int getId() {
		return id;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int[] getColor() {
		return color;
	}

	public void setColor(int[] color) {
		this.color = color;
	}

	public int getR() {
		return color[0];
	}

	public void setR(int r) {
		this.color[0] = r;
	}

	public int getG() {
		return color[1];
	}

	public void setG(int g) {
		this.color[1] = g;
	}

	public int getB() {
		return color[2];
	}

	public void setB(int b) {
		this.color[2] = b;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
