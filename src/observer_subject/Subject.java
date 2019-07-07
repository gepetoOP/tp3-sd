package observer_subject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

import gui.Dot;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import misc.Configs;
import misc.ConsoleColors;


public class Subject {
	private Int2ObjectMap<Dot> dots = new Int2ObjectOpenHashMap<Dot>();
    Int2ObjectMap<Dot> dotBatch = new Int2ObjectOpenHashMap<Dot>();

    private List<String> otherSubjects;
    private List<String> subjectsDown = new ArrayList<String>();

    private List<Integer> obsPorts_current = new ArrayList<Integer>();
    private Map<String, Integer> subs_ip_port;

    private ServerSocket server;
    private Integer VERSION;

    private int SUB_SUB = 0;
	private boolean portTaken = false;

	@SuppressWarnings("unchecked")
	public Subject(List<String> remainingSubjects, Map<String,Integer> subsIp_port, String thisIp){

		this.otherSubjects = new ArrayList<String>(remainingSubjects);

		if(!subsIp_port.isEmpty()) {
			this.subs_ip_port = subsIp_port;
			obsPorts_current.add(this.subs_ip_port.get(thisIp));
		}

		VERSION = 0;

		boolean send_nuvem = false;

		try {
			nuvemInicial(Configs.TAMANHO_NUVEM_INICIAL);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try{
			server();
		} catch (Exception e) {}

	}

	public void server() throws IOException{

		server = new ServerSocket(Configs.SUBJECT_PORTA);
		print("(Subject.server) SERVER CREATED");

		new Thread(() ->{
			try {
				while(true){

					Socket client = server.accept();

					new Thread(() ->{
						try {
							print("(Subject.server) Opened connection with " + client.getRemoteSocketAddress());

							ObjectOutputStream outStream = new ObjectOutputStream(client.getOutputStream());
							ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());

							Object [] msg = (Object []) inStream.readObject();
							print("(Subject.server) Received message from " + client.getRemoteSocketAddress());

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

	private void msgHandler(Object[] msg, ObjectInputStream inStream, ObjectOutputStream outStream, Socket client){
		String host = client.getRemoteSocketAddress().toString();
			print("(Subject.msgHandler) RECEIVED MESSAGE FROM " + host);
		Dot d = null;
		int msg_type = (int) msg[0];
		try {
			switch (msg_type) {
			case 1:
				// recebe atulização de escrita, sincroniza e notifica
				print("(Subject.msgHandler) Write received from " + host);
				d = (Dot) msg[1];

				synchronized (dots) {
					dots.put(d.getId(),d);
				};

				synchronized (dotBatch) {
					dotBatch.put(d.getId(),d);
				};

				VERSION++;
				sync(d);

				if(dotBatch.size() >= Configs.BATCH_SIZE){
					notifyObserversUDP();
				}
				break;

			case 2:
				// recebe sincronização de outro sub e notifica
				print("(Subject.msgHandler) Sync received from " + host);

				d = (Dot) msg[1];

				synchronized (dots) {
					dots.put(d.getId(), d);
				};

				synchronized (dotBatch) {
					dotBatch.put(d.getId(),d);
				};

				outStream.writeObject(1); // envia ack de syncronizacao

				if(dotBatch.size() >= Configs.BATCH_SIZE*1.05){
					dotBatch.clear();
				}

				print("(Subject.msgHandler) Ack sent to " + host);
				break;

			case 3:
				// recebe
				portTaken = true;
				break;

			case 4:
				// recebe notificação de que o sub voltou
				synchronized(otherSubjects){
					String ipPort = (String) msg[1];
					int index = subjectsDown.indexOf(ipPort);
					String str = null;
					if (index != -1){
						str = subjectsDown.remove(index);
						otherSubjects.add(str);
						boolean send_nuvem = (boolean) msg[2];
						Object [] resp = {dots, subs_ip_port};
						if(!send_nuvem) outStream.writeObject(resp);

						index = obsPorts_current.indexOf(subs_ip_port.get(str));
						if (index != -1){
							obsPorts_current.remove(index);
						}
					}
				}

				break;

			default:
				System.err.println("(Subject.msgHandler) Bad Request ERROR 500");
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void sync(Dot d) throws UnknownHostException, IOException, InterruptedException, ClassNotFoundException{
		Object [] msg = {2,d};

		Socket sub = null;
		ObjectInputStream inSub = null;
		ObjectOutputStream outSub = null;

		synchronized (otherSubjects) {
			for(String sIp : otherSubjects){
				try{
					sub = new Socket();
					sub.connect(new InetSocketAddress(sIp, Configs.SUBJECT_PORTA), 1500);
					sub.setSoTimeout(1500);
					inSub = new ObjectInputStream(sub.getInputStream());
					outSub = new ObjectOutputStream(sub.getOutputStream());

					outSub.writeObject(msg);

					inSub.readObject();

					SUB_SUB++;
					print("(Subject.sync) Conections SUB_SUB: " + SUB_SUB);
				}catch (Exception e) {
					otherSubjects.remove(sIp);
					subjectsDown.add(sIp);

					System.err.println("(Subject.sync) SUBJECT FAIL " + sIp);

					if(!portTaken){
						obsPorts_current.add(subs_ip_port.get(sIp));
						Object [] msg1 = {3};
						for(String subjIp : otherSubjects) {
							sub = new Socket();
							sub.connect(new InetSocketAddress(sIp, Configs.SUBJECT_PORTA), 1500);
							sub.setSoTimeout(1500);
							inSub = new ObjectInputStream(sub.getInputStream());
							outSub = new ObjectOutputStream(sub.getOutputStream());

							outSub.writeObject(msg1);
						}
					}
				} finally {
					if (outSub != null && inSub != null && sub != null) {
						outSub.close();
						inSub.close();
						sub.close();
					}
				} 
			}			
		}
	}


	public void notifyObserversUDP() throws IOException{
		Object [] msg = {2,dotBatch};

		for(Integer i : obsPorts_current){
			DatagramPacket packet;
			DatagramSocket socket = new DatagramSocket(i);
			ByteArrayOutputStream bos = null;
			try {
				InetAddress group = getBrodcastAddr(); //InetAddress.getByName("192.168.0.255");
				byte[] buf = new byte[socket.getReceiveBufferSize()];

				bos = new ByteArrayOutputStream();
				ObjectOutput out = null;

				out = new ObjectOutputStream(bos);   
				out.writeObject(msg);
				out.flush();
				buf = bos.toByteArray();
				print("(Subject.notifyObserversUDP) MESSAGE SERIALIZED");
				packet = new DatagramPacket(buf, buf.length, group, i);
				socket.send(packet);
				print("(Subject.notifyObserversUDP) PACKET SENT");
			}
			catch (IOException e) {
				e.printStackTrace();
			}finally {
				try {
					bos.close();
					socket.close();
				} catch (IOException ex) {
					// ignore close exception
				}
			}	
		}
		dotBatch.clear();
	}

	public void nuvemInicial(int size) throws IOException {
		for(int i=0;i<size;i++){
            int [] color = new int[3];

            color[0] = new Random().nextInt(256);
            color[1] = new Random().nextInt(256);
            color[2] = new Random().nextInt(256);

            int x = new Random().nextInt(Configs.TAMANHO_FRAME);
            int y = new Random().nextInt(Configs.TAMANHO_FRAME);

            int s = new Random().nextInt(Configs.TAMANHO_PONTO) + 1;

            Dot d = new Dot(x,y,color, s);

//            Dot d = new Dot();
			dotBatch.put(d.getId(), d);
		}
		dots.putAll(dotBatch);

		notifyObserversUDP();
	}

	public InetAddress getBrodcastAddr() throws SocketException{
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

			if (networkInterface.isLoopback() || !networkInterface.isUp())
				continue;

			InetAddress broadcast = null; 
			for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
				broadcast = interfaceAddress.getBroadcast();
				if (broadcast != null)
					break;
			}
			print("(Subject.getBrodcastAddr) broadcastAddress: " + broadcast.getHostAddress());
			if (broadcast != null) return broadcast;
		}
		return null;
	}

	// FORMATA A SAIDA (ESTETICA)
	private void print(String s) {
		System.out.println(ConsoleColors.PURPLE + s + ConsoleColors.RESET);
	}

}
