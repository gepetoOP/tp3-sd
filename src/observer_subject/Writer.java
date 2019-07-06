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
			//server();
			generate4ever();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void server() throws IOException{
		servidor = new ServerSocket(6789);
		System.out.println(ConsoleColors.YELLOW + "SERVIDOR (Writer): Servidor criado");
		new Thread(() ->{
			try {
				while(true){
					Socket client = servidor.accept();
					new Thread(() ->{
						try {
							System.out.println(ConsoleColors.YELLOW + "SERVIDOR (Writer): Conexao aberta com " + client.getRemoteSocketAddress());

							ObjectOutputStream outStream = new ObjectOutputStream(client.getOutputStream());
							ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());

							Object [] msg = (Object []) inStream.readObject();
							System.out.println(ConsoleColors.YELLOW + "SERVIDOR (Writer): Mensagem Recebida de " + client.getRemoteSocketAddress());

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

	private synchronized void msgHandler(Object[] msg, ObjectInputStream inStream, ObjectOutputStream outStream, Socket client) {
		String host = client.getRemoteSocketAddress().toString();
		System.out.println(ConsoleColors.YELLOW_BRIGHT + "OBSERVER (Writer): Mensagem recebida de " + host);
		int msg_type = (int) msg[0];
		try {
			switch (msg_type) {
				case 1:
					String data = (String) msg[1];
					subjects.add(data);
					break;

				default:
					System.err.println("OBSERVER: Bad Request ERROR 500");
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateDot() throws IOException{
		int [] rgb = new int[3];
		rgb[0] = (int) (Math.random()*256);
		rgb[1] = (int) (Math.random()*256);
		rgb[2] = (int) (Math.random()*256);

		int x = (int) (Math.random()*1250);
		int y = (int) (Math.random()*700);

		Dot d = new Dot(x,y,rgb, 10);

		write(d);
	}

	public void generate4ever() throws InterruptedException, IOException{
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
		String [] s = null;
		Object [] msg = {1, d};
		int pos = new Random().nextInt(3);

		try{
			while(pos == last_pos){
				pos = new Random().nextInt(3);
			}

			last_pos = pos;

			s = subjects.get(pos).split(":");
			sub = new Socket();
			sub.connect(new InetSocketAddress(s[0], Integer.parseInt(s[1])), 1500);
			sub.setSoTimeout(1500);
			inSub = new ObjectInputStream(sub.getInputStream());
			outSub = new ObjectOutputStream(sub.getOutputStream());

			outSub.writeObject(msg);

			WRT_SUB++;
			System.out.println(ConsoleColors.YELLOW + "(Writer) Conexoes WRT_SUB: " + WRT_SUB);
		}catch(Exception e){
			System.err.println("(Writer) falha no subject " + s[0]);

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
}
