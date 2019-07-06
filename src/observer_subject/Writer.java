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
	private ServerSocket servidor;
	private int last_pos = -1;
	
	public Writer(List<String> subjects){
		this.subjects = subjects;
		
		try {
			generateDots();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
//	public void server() throws IOException{
//		servidor = new ServerSocket(Configs.INVOKER_PORTA.get);
//		print("(Writer.server) Servidor criado");
//		new Thread(() ->{
//			try {
//				while(true){
//					Socket client = servidor.accept();
//					new Thread(() ->{
//						try {
//							print("(Writer.server) Conexao aberta com " + client.getRemoteSocketAddress());
//
//							ObjectOutputStream outStream = new ObjectOutputStream(client.getOutputStream());
//							ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());
//
//							Object [] msg = (Object []) inStream.readObject();
//							print("(Writer.server) Mensagem Recebida de " + client.getRemoteSocketAddress());
//
//							msgHandler(msg, inStream, outStream, client);
//
//							inStream.close();
//							outStream.close();
//							client.close();
//						} catch (IOException | ClassNotFoundException e) {
//							e.printStackTrace();
//						}
//					}).start();
//				}
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//		}).start();
//	}
	
//	private synchronized void msgHandler(Object[] msg, ObjectInputStream inStream, ObjectOutputStream outStream, Socket client) {
//		String host = client.getRemoteSocketAddress().toString();
//		System.out.println(ConsoleColors.YELLOW_BRIGHT + "(Writer.msgHandler) Received message from " + host + ConsoleColors.RESET);
//		int msg_type = (int) msg[0];
//		try {
//			switch (msg_type) {
//			case 1:
//				String data = (String) msg[1];
//				subjects.add(data);
//				break;
//
//			default:
//				System.err.println("(Writer.msgHandler) Bad Request ERROR 500");
//				break;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	public void generateDot() throws IOException{
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
			while(pos == last_pos){
				pos = new Random().nextInt(Configs.QTD_SUBJECTS);
			}
			
			last_pos = pos;
			
			subjIp = subjects.get(pos);
			sub = new Socket();
			sub.connect(new InetSocketAddress(subjIp, Configs.SUBJECT_PORTA), 1500);
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




