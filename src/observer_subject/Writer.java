package observer_subject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;

import gui.Dot;

public class Writer{

	private List<String> subjects;
	private int ERROR=0;
	private int WRT_SUB = 0;
	private ServerSocket server;
	private int lastPosition = -1;

	public Writer(List<String> subjects){
		this.subjects = subjects;

		try {
			generateDots();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void generateDot() throws IOException{
//		int [] color = new int[3];
//
//		color[0] = new Random().nextInt(256);
//		color[1] = new Random().nextInt(256);
//		color[2] = new Random().nextInt(256);
//
//		int x = new Random().nextInt(Configs.TAMANHO_FRAME);
//		int y = new Random().nextInt(Configs.TAMANHO_FRAME);
//
//		int size = new Random().nextInt(10) + 1;
//
//		Dot d = new Dot(x,y,color, size);

		Dot d = new Dot();

		write(d);
	}

	public void generateDots() throws InterruptedException, IOException{
		while(true){
			generateDot();
			int tempo = new Random().nextInt(1000) + 1;
			Thread.sleep(tempo);
		}
	}

	public void write(Dot d) throws IOException{
		Socket sub = null;
		ObjectInputStream inSub = null;
		ObjectOutputStream outSub = null;
		String subjIp = null;
		Object [] msg = {1, d};
		int pos = new Random().nextInt(Configs.QTD_SUBJECTS);

		try{
			while(pos == lastPosition){
				pos = new Random().nextInt(Configs.QTD_SUBJECTS);
			}

			lastPosition = pos;

			subjIp = subjects.get(pos);
			sub = new Socket();
			sub.connect(new InetSocketAddress(subjIp, Integer.parseInt(Configs.SUBJECT_PORTA_STRING)), 1500);
			sub.setSoTimeout(1500);
			inSub = new ObjectInputStream(sub.getInputStream());
			outSub = new ObjectOutputStream(sub.getOutputStream());

			outSub.writeObject(msg);

			WRT_SUB++;

			print("(Writer.write) CONECTIONS WRT_SUB: " + WRT_SUB);
		}catch(Exception e){
			System.err.println("(Writer.write) SUBJECT FAIL: " + subjIp);


			String str = subjects.remove(0);
			subjects.add(str);

			ERROR = 1;
		}
		finally{
			if(outSub!=null && inSub!=null && sub!=null){
				outSub.close();
				inSub.close();
				sub.close();
			}
			if(ERROR == 1){
				ERROR = 0;
				write(d);
			}
		}
	}


	// FORMATA A SAIDA (ESTETICA)
	private void print(String s) {
		System.out.println(ConsoleColors.YELLOW + s + ConsoleColors.RESET);
	}

}
