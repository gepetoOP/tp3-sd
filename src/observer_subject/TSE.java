package observer_subject;

import gui.Dot;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import misc.Configs;
import misc.ConsoleColors;

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
import java.util.concurrent.CopyOnWriteArrayList;


public class TSE {

	private int totalMachines;
	private int nSubjects = Configs.QTD_SUBJECTS;

	private List<String> subjects;
	private List<String> activeSubjects;
	private List<String> failedSubjects;
	private Map<String,Integer> s_ip_port;

	private int nWriters;
	private List<String> writers;

	private int nObservers;
	private List<String> observers;
	private Map<String,Integer> o_ip_port;



	public static  void main(String [] args){

		ArrayList<String> ips = new ArrayList<String>();

		// PRIMEIRA FILEIRA LAB 30
//		ips.add("200.239.138.211");
//		ips.add("200.239.139.25");
//		ips.add("200.239.139.27");
//      	ips.add("200.239.139.23");
//		ips.add("200.239.139.28");
//		ips.add("200.239.139.32");

		// SEGUNDA FILEIRA LAB 30
		ips.add("200.239.139.110");
		ips.add("200.239.139.118");
		ips.add("200.239.139.120");
//		ips.add("200.239.139.113");
//		ips.add("200.239.139.122");
//
		// TERCEIRA FILEIRA LAB 30
 		ips.add("200.239.139.119");
		ips.add("200.239.139.125");
		ips.add("200.239.139.123");
//		ips.add("200.239.139.111");
//		ips.add("200.239.139.116");

		// QUARTA FILEIRA LAB 30
//		ips.add("200.239.139.26");
//		ips.add("200.239.139.29");
//		ips.add("200.239.139.61");
//		ips.add("200.239.138.214");
//		ips.add("200.239.138.240");


		List<Integer> obs_ports = new ArrayList<Integer>();
		for(int i=0; i<Configs.QTD_SUBJECTS; i++){
			obs_ports.add(Configs.OBSERVER_PORTA + i);
		}


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
		totalMachines = ips.size();

		int machinesLeft = totalMachines;
		print("(TSE) ASSIGNING SUBJECTS");

		subjects = new ArrayList<String>();
		s_ip_port = new HashMap<String, Integer>();

		while(subjects.size() < nSubjects){
			int pos = new Random().nextInt(machinesLeft);
			if(!subjects.contains(ips.get(pos))){
				subjects.add(ips.remove(pos));
				machinesLeft = ips.size();
			}
		}
		for(int i=0; i<nSubjects; i++){
			s_ip_port.put(subjects.get(i),obs_ports.get(i%3));
		}
		print("(TSE) Subjects: " + subjects);



//		nWriters = machinesLeft/2;
		nWriters = Configs.QTD_WRITER;
		print("(TSE) ASSIGNING WRITERS");

		writers = new ArrayList<String>();

		while(writers.size() < nWriters){
			int pos = new Random().nextInt(machinesLeft);
			if (!writers.contains(ips.get(pos))){
				writers.add(ips.remove(pos));
				machinesLeft = ips.size();
			}
		}
		System.out.println("(TSE) Writers: " + writers);


//		nObservers = machinesLeft;
		nObservers = Configs.QTD_OBSERVER;
		print("(TSE) ASSIGNING OBSERVERS");

		observers = new ArrayList<>(ips);
		o_ip_port = new HashMap<String, Integer>();

		for(int i=0; i<nObservers; i++){
			o_ip_port.put(observers.get(i), obs_ports.get(i%Configs.QTD_SUBJECTS));
		}
		print("(TSE) Observers: " + observers);


		activeSubjects = subjects;
	}

	public void invokeObservers(){
		
		for(Entry<String,Integer> obs_ip_port : o_ip_port.entrySet()){
			String thisIp = obs_ip_port.getKey();
			Integer thisPort = obs_ip_port.getValue();
			Socket obs;
			try {
				print("(TSE.invokeObservers) invoking observer: " + thisIp);
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
				print("(TSE.invokeSubjects) invoking subject: " + thisIp);
				sub = new Socket(thisIp, Configs.INVOKER_PORTA);
				sub.setSoTimeout(1500);
				ObjectInputStream inSub = new ObjectInputStream(sub.getInputStream());
				ObjectOutputStream outSub = new ObjectOutputStream(sub.getOutputStream());

				List<String> remainingSubjects = new ArrayList<String>(subjects);
				remainingSubjects.remove(thisIp);

				Object [] args = {1, remainingSubjects, s_ip_port, thisIp};
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
				print("(TSE.invokeWriters) invoking writer: " + wri_ip);
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
		print(ConsoleColors.WHITE + "(TSE.server) SERVER CREATED");

		new Thread(() ->{
			try {
				while(true){
					Socket client = servidor.accept();

					new Thread(() ->{
						try {

							print("(TSE.server) Opened connection with " + client.getRemoteSocketAddress());

							ObjectOutputStream outStream = new ObjectOutputStream(client.getOutputStream());
							ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());

							Object[] fail_msg = (Object []) inStream.readObject();
							String ipFail = (String) fail_msg[0];

							String senderIp = ((InetSocketAddress) client.getRemoteSocketAddress()).getAddress().toString().replaceAll("/", "");
							print("(TSE) IP FAIL " + ipFail);

							print("(TSE) Received message from " + senderIp);

							if(activeSubjects.contains(ipFail)) {
								activeSubjects.remove(ipFail);
								failedSubjects.add(ipFail);
							}

							int oldPort = s_ip_port.get(ipFail);
							int newPort;

							if(oldPort == Configs.OBSERVER_PORTA){
								newPort = Configs.OBSERVER_PORTA+1;
							} else {
								newPort = Configs.OBSERVER_PORTA;
							}

							contactObservers(oldPort, newPort);

//							invokeSubjects();
							invokeSubject(ipFail);

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

	public void invokeSubject(String ip){

		Socket sub;
		try {
			print("(TSE.invokeSubjects) subject: " + ip);
			sub = new Socket(ip, Configs.INVOKER_PORTA);
			sub.setSoTimeout(1500);
			ObjectInputStream inSub = new ObjectInputStream(sub.getInputStream());
			ObjectOutputStream outSub = new ObjectOutputStream(sub.getOutputStream());

			List<String> remainingSubjects = new ArrayList<String>(subjects);
			remainingSubjects.remove(ip);

			Object [] args = {1, remainingSubjects, s_ip_port, ip};
			outSub.writeObject(args);

			inSub.close();
			outSub.close();
			sub.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}

	}

	public void contactObservers(int old_port, int new_port){

		for(String obs_ip : observers){
			Socket obs;
			try {
				print("(TSE.contactObservers) observer: " + obs_ip);
				obs = new Socket(obs_ip, o_ip_port.get(obs_ip));
				obs.setSoTimeout(1500);
				ObjectInputStream inWrt = new ObjectInputStream(obs.getInputStream());
				ObjectOutputStream outWrt = new ObjectOutputStream(obs.getOutputStream());

				Object [] args = {5, old_port, new_port};
				outWrt.writeObject(args);

				inWrt.close();
				outWrt.close();
				obs.close();

			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	// FORMATA A SAIDA (ESTETICA)
	private void print(String s) {
		System.out.println(ConsoleColors.WHITE + s + ConsoleColors.RESET);
	}
	

}
