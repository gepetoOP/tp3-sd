package observer_subject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Random;

import gui.Dot;
import gui.Frame;
import gui.Panel;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import misc.ConsoleColors;


public class Observer {
    private Int2ObjectMap<Dot> dots = new Int2ObjectOpenHashMap<Dot>();
    private Panel panel;
    private ServerSocket server;
    private Integer VERSION = 0;

    private Long t0 = (long) 0;
    private Long t1 = (long) 0;
    private Long t2 = (long) 0;
    private Double time;

    private int port;

    private boolean subjectFailed = false;

    public Observer(int port){

        print("(Observer) ----- OBSERVER ----- ");

        panel = new Panel();
        Frame frame = new Frame(panel);


        this.port = port;

        try {
            server();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void server() throws IOException, ClassNotFoundException{

        print("(Observer.server) ----- OBSERVER ----- ");
        t0 = System.currentTimeMillis();

        DatagramPacket packet = null;
        DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
        socket.setBroadcast(true);

        while(true){
            byte[] buf = new byte[socket.getReceiveBufferSize()];
            packet = new DatagramPacket(buf, buf.length);
            t1 = System.currentTimeMillis();

            socket.receive(packet);

            t2 = System.currentTimeMillis();
            time = (double) (t2.longValue() - t0.longValue())/1000;
            print(time + "s (Observer.server) time: " + (((double) t2.longValue()) - t1.longValue())/1000 + "s");

            ObjectInputStream iStream;
            iStream = new ObjectInputStream(new ByteArrayInputStream(packet.getData()));

            new Thread(() ->{
                try {
                    Object[] msg = (Object []) iStream.readObject();

                    msgHandler(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @SuppressWarnings("unchecked")
    private void msgHandler(Object[] msg) {
        int msg_type = (int) msg[0];
        print("(Observer.msgHandler) MESSAGE TYPE: " + msg_type);
        try {
            switch (msg_type) {
                case 1:	// print a nuvem inteira
                    Int2ObjectMap<Dot> nuvem = (Int2ObjectMap<Dot>) msg[1];
                    dots.putAll(nuvem);
                    synchronized(VERSION){
                        VERSION = 1;
                    }
                    printDots(nuvem);
                    print("(Observer.msgHandler) RECEIVED VERSION: " + VERSION);
                    break;
                case 2:	// print nos pontos atualizados
                    Int2ObjectMap<Dot> nuvemUpdate = (Int2ObjectMap<Dot>) msg[1];
                    print("(Observer.msgHandler) size: " + nuvemUpdate.size());
                    dots.putAll(nuvemUpdate);
                    synchronized (VERSION) {
                        VERSION++;
                    }
                    printDots(nuvemUpdate);
                    print("(Observer.msgHandler) RECEIVED VERSION: " + VERSION);
                    break;
                case 5:	// subject caiu
                    if(port == (int) msg[1]){
                        port = (int) msg[2];
                        this.print("(Observer.msgHandler) CHANGED PORT: " + port);
                        subjectFailed = true;
                    }
                    this.print("(Observer.msgHandler) CHANGED PORT: " + port);
                    break;
                case 6:	// subject voltou
                    if(port == (int) msg[1] && subjectFailed){
                        port = (int) msg[2];
                        this.print("(Observer.msgHandler) CHANGED PORT: " + port);
                        subjectFailed = false;
                    }
                    this.print("(Observer.msgHandler) CHANGED PORT: " + port);
                    break;

                default:
                    System.err.println("(Observer.msgHandler) Bad Request ERROR 500");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void printDots(Int2ObjectMap<Dot> dotBatch){

        for(Dot d : dotBatch.values()){
//            d.printDot();
            panel.addDot(d);
        }
        panel.repaint();
    }


    // FORMATA A SAIDA (ESTETICA)
    private void print(String s) {
        System.out.println(ConsoleColors.GREEN + s + ConsoleColors.RESET);
    }


}
