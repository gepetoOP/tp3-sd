package observer_subject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class Invoker {

	public static void main(String [] args){
		try {
			Invoker invoker = new Invoker();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Invoker() throws IOException{

		ServerSocket server = new ServerSocket(Configs.INVOKER_PORTA);
		print("(Invoker) SERVER CREATED");

		new Thread(() ->{
			try {
				while(true){
					Socket client = server.accept();
					new Thread(() ->{
						try {
							print("(Invoker) Opened connection with " + client.getRemoteSocketAddress());

							ObjectOutputStream outStream = new ObjectOutputStream(client.getOutputStream());
							ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());

							Object [] msg = (Object []) inStream.readObject();
							print("(Invoker) Received message from " + client.getRemoteSocketAddress());

							msgHandler(msg, inStream, outStream, client);

							inStream.close();
							outStream.close();
							client.close();
						} catch (IOException | ClassNotFoundException e) {
							e.printStackTrace();
						}
					}).start();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}).start();
	}

	@SuppressWarnings("unchecked")
	private void msgHandler(Object[] msg, ObjectInputStream inStream, ObjectOutputStream outStream, Socket client) {
		String host = client.getRemoteSocketAddress().toString();
		print("(Invoker.msgHandler) Received message from " + host + " - " + msg[0]);
		int msg_type = (int) msg[0];
		switch(msg_type){
		case 1:
			Subject s = new Subject((List<String>) msg[1], (Map<String, Integer>) msg[2], (String) msg[3]);
			break;

		case 2:
			Writer w = new Writer((List<String>) msg[1]);
			break;

		case 3:
			Observer o = new Observer((Integer) msg[1]);
			break;

		default:
			break;
		}
	}

	// FORMATA A SAIDA (ESTETICA)
	private void print(String s) {
		System.out.println(ConsoleColors.BLUE + s + ConsoleColors.RESET);
	}
	
}
