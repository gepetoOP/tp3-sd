package observer_subject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;

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

    private Long t1;
    private Long t2=(long) 0;

    private int port;

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

        DatagramPacket packet = null;
        DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
        socket.setBroadcast(true);

        while(true){
            byte[] buf = new byte[socket.getReceiveBufferSize()];
            packet = new DatagramPacket(buf, buf.length);
            t1 = System.currentTimeMillis();

            socket.receive(packet);

            t2 = System.currentTimeMillis();
            print("(Observer.server) time: " + (((double) t2.longValue()) - t1.longValue())/1000 + "s");

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
                    print(ConsoleColors.RED_BACKGROUND + "(Observer.msgHandler) CHANGED PORT: " + port);
                    if(port == (int) msg[1]){
                        port = (int) msg[2];
                        print("(Observer.msgHandler) CHANGED PORT: " + port);
                    }
                    break;
                case 6:	// subject voltou
//                    port = (int) msg[2];
//                    print("(Observer.msgHandler) CHANGED PORT: " + port);
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
