package observer_subject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/*****************
 *
 *  Porque a separacao de writers e observers?? (nao tem writer no enunciado)
 *
 *  nao entendi a distribuiçao de portas pros observers e subjects??? (linha 143, 2 ultimos for do construtor)
 *  pq 3 portas diferentes?
 *
 *  entender melhor as funções de invoke e server
 *
 **********************/


public class TSE {

	private int totalMaquinas;

	private int nSubjects = Configs.QTD_SUBJECTS;
	private List<String> subjects;
	private Map<String,Integer> subsIp_port;

	private int nWriters;
	private List<String> writers;
	private Map<String,Integer> wriIp_port;

	private int nObservers;
	private List<String> observers;
	private Map<String,Integer> obsIp_port;



	public static  void main(String [] args){

		ArrayList<String> ips = new ArrayList<String>();

		// PRIMEIRA FILEIRA LAB 30
//		ips.add("200.239.138.211");
//		ips.add("200.239.139.25");
//		ips.add("200.239.139.27");
//      ips.add("200.239.139.23");
//		ips.add("200.239.139.28");
//		ips.add("200.239.139.32");

		// SEGUNDA FILEIRA LAB 30
		ips.add("200.239.139.110");
		ips.add("200.239.139.118");
		ips.add("200.239.139.120");
		ips.add("200.239.139.113");
		ips.add("200.239.139.122");
//
		// TERCEIRA FILEIRA LAB 30
 		ips.add("200.239.139.119");
		ips.add("200.239.139.125");
		ips.add("200.239.139.123");
		ips.add("200.239.139.111");
		ips.add("200.239.139.116");

		// QUARTA FILEIRA LAB 30
//		ips.add("200.239.139.26");
//		ips.add("200.239.139.29");
//		ips.add("200.239.139.61");
//		ips.add("200.239.138.214");
//		ips.add("200.239.138.240");


		List<Integer> obs_ports = new ArrayList<Integer>();
		obs_ports.add(3321);
		obs_ports.add(3322);
		obs_ports.add(3323);


		TSE tse = new TSE(ips, obs_ports);


		tse.invokeObservers();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		tse.invokeSubjects();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		tse.invokeWriters();
		try {
			tse.server();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TSE(ArrayList<String> ips, List<Integer> obs_ports){
		totalMaquinas = ips.size();

		int nMaquinas = totalMaquinas;
		print("(TSE) ASSIGNING SUBJECTS");

		subjects = new ArrayList<String>();
		subsIp_port = new HashMap<String, Integer>();

		while(subjects.size() < nSubjects){
			int pos = new Random().nextInt(nMaquinas);
			if(!subjects.contains(ips.get(pos))){
				subjects.add(ips.remove(pos));
				nMaquinas = ips.size();
			}
		}
		for(int i=0; i<nSubjects; i++){
			subsIp_port.put(subjects.get(i),obs_ports.get(i%3));
		}
		print("(TSE) Subjects: " + subjects);


		nWriters = nMaquinas/2;
		print("(TSE) ASSIGNING WRITERS");

		writers = new ArrayList<String>();

		while(writers.size() < nWriters){
			int pos = new Random().nextInt(nMaquinas);
			if (!writers.contains(ips.get(pos))){
				writers.add(ips.remove(pos));
				nMaquinas = ips.size();
			}
		}
		System.out.println("(TSE) Writers: " + writers);


		nObservers = nMaquinas;
		print("(TSE) ASSIGNING OBSERVERS");

		observers = new ArrayList<>(ips);
		obsIp_port = new HashMap<String, Integer>();

		for(int i=0; i<nObservers; i++){
			obsIp_port.put(observers.get(i), obs_ports.get(i%3));
		}
		print("(TSE) Observers: " + observers);



	}

	public void invokeObservers(){
		
		for(Entry<String,Integer> obsIp_port : this.obsIp_port.entrySet()){
			String thisIp = obsIp_port.getKey();
			Integer thisPort = obsIp_port.getValue();
			Socket obs;
			try {
				print("(TSE.invokeObservers) observer: " + thisIp);
				obs = new Socket(thisIp, Configs.INVOKER_PORTA);
				obs.setSoTimeout(1500);
				ObjectInputStream inObs = new ObjectInputStream(obs.getInputStream());
				ObjectOutputStream outObs = new ObjectOutputStream(obs.getOutputStream());

				Object [] args = {3, thisPort};

				outObs.writeObject(args);

				inObs.close();
				outObs.close();
				obs.close();

			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void invokeSubjects(){

		for(String thisIp : subjects){
			Socket sub;
			try {
				print("(TSE.invokeSubjects) subject: " + thisIp);
				sub = new Socket(thisIp, Configs.INVOKER_PORTA);
				sub.setSoTimeout(1500);
				ObjectInputStream inSub = new ObjectInputStream(sub.getInputStream());
				ObjectOutputStream outSub = new ObjectOutputStream(sub.getOutputStream());

				List<String> remainingSubjects = new ArrayList<String>(subjects);
				remainingSubjects.remove(thisIp);

				Object [] args = {1, remainingSubjects, subsIp_port, thisIp};
				outSub.writeObject(args);

				inSub.close();
				outSub.close();
				sub.close();
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void invokeWriters(){

		for(String wri_ip : writers){
			Socket wrt;
			try {
				print("(TSE.invokeWriters) writer: " + wri_ip);
				wrt = new Socket(wri_ip, Configs.INVOKER_PORTA);
				wrt.setSoTimeout(1500);
				ObjectInputStream inWrt = new ObjectInputStream(wrt.getInputStream());
				ObjectOutputStream outWrt = new ObjectOutputStream(wrt.getOutputStream());

				Object [] args = {2, subjects};
				outWrt.writeObject(args);

				inWrt.close();
				outWrt.close();
				wrt.close();

			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void server() throws IOException{
		ServerSocket servidor = new ServerSocket(Configs.TSE_PORTA);
		System.out.println(ConsoleColors.WHITE + "(TSE.server) SERVER CREATED");

		new Thread(() ->{
			try {
				while(true){
					Socket client = servidor.accept();

					new Thread(() ->{
						try {
							print("(TSE.server) Opened connection with " + client.getRemoteSocketAddress());

							ObjectOutputStream outStream = new ObjectOutputStream(client.getOutputStream());
							ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());

							inStream.readObject();
							String senderIp = ((InetSocketAddress) client.getRemoteSocketAddress()).getAddress().toString().replaceAll("/", "");
							print("(TSE) Received message from " + senderIp);
							
							ArrayList<String> remainingSubjects = new ArrayList<String>(subjects);
							String sub = null;


							// TODO: TESTAR!
							if(subjects.contains(senderIp)) {
								remainingSubjects.remove(senderIp);
								sub = senderIp;
							}

//							for(String s : subjects) {
//								if(s.contains(senderIp)) {
//									remainingSubjects.remove(s);
//									sub = s;
//									break;
//								}
//							}

							
							Object [] msg = {sub, remainingSubjects};
							outStream.writeObject(msg);

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



	// FORMATA A SAIDA (ESTETICA)
	private void print(String s) {
		System.out.println(ConsoleColors.WHITE_BACKGROUND + ConsoleColors.BLACK + s + ConsoleColors.RESET);
	}
	

}