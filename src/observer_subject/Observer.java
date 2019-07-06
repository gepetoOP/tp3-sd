package observer_subject;

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;

import gui.Dot;
import gui.Panel;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class Observer {
    private Int2ObjectMap<Dot> dots = new Int2ObjectOpenHashMap<Dot>();
    private Panel panel;
    private ServerSocket servidor;
    private Integer VERSAO=0;

    private Long t1;
    private Long t2=(long) 0;

    private int port;

    public Observer(int port){
        JFrame frame = new JFrame();
        panel = new Panel();
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.setSize(500, 500);
        frame.setVisible(true);

        this.port = port;

        try {
            serverUDP();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void serverUDP() throws IOException, ClassNotFoundException{
        DatagramPacket packet = null;
        DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
        socket.setBroadcast(true);

        while(true){
            byte[] buf = new byte[socket.getReceiveBufferSize()];
            packet = new DatagramPacket(buf, buf.length);
            t1 = System.currentTimeMillis();

            socket.receive(packet);

            t2 = System.currentTimeMillis();
            System.out.println((((double) t2.longValue()) - t1.longValue())/1000 + "s");

            ObjectInputStream iStream;
            iStream = new ObjectInputStream(new ByteArrayInputStream(packet.getData()));

            new Thread(() ->{
                try {
                    Object[] msg = (Object []) iStream.readObject();

                    msgHandlerUDP(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @SuppressWarnings("unchecked")
    private void msgHandlerUDP(Object[] msg) {
        int msg_type = (int) msg[0];
        try {
            switch (msg_type) {
                case 1:
                    // print a nuvem inteira
                    Int2ObjectMap<Dot> nuvem = (Int2ObjectMap<Dot>) msg[1];
                    dots.putAll(nuvem);
                    synchronized(VERSAO){
                        VERSAO = 1;
                    }
                    printDots(nuvem);
                    //System.out.println("VERSÃO RECEBIDA: " + VERSAO);
                    break;
                case 2:
                    // print nos pontos atualizados
                    Int2ObjectMap<Dot> nuvemUpdate = (Int2ObjectMap<Dot>) msg[1];
                    System.out.println(nuvemUpdate.size());
                    dots.putAll(nuvemUpdate);
                    synchronized (VERSAO) {
                        VERSAO++;
                    }

                    printDots(nuvemUpdate);
                    //System.out.println("VERSÃO RECEBIDA: " + VERSAO);
                    break;
                default:
                    System.err.println("OBSERVER: Bad Request ERROR 500");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void server() throws IOException{
        servidor = new ServerSocket(1234);
        System.out.println(ConsoleColors.GREEN + "SERVIDOR (Observer): Servidor criado");
        new Thread(() ->{
            try {
                while(true){
                    t1 = System.currentTimeMillis();
                    Socket client = servidor.accept();

                    new Thread(() ->{
                        try {
                            System.out.println(ConsoleColors.GREEN + "SERVIDOR (Observer): Conexao aberta com " + client.getRemoteSocketAddress());

                            ObjectOutputStream outStream = new ObjectOutputStream(client.getOutputStream());
                            ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());

                            Object [] msg = (Object []) inStream.readObject();
                            System.out.println(ConsoleColors.GREEN + "SERVIDOR (Observer): Mensagem Recebida de " + client.getRemoteSocketAddress());
                            synchronized(t2){
                                t2 = System.currentTimeMillis();
                                System.out.println((((double) t2.longValue()) - t1.longValue())/1000 + "s");
                            }

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
    private synchronized void msgHandler(Object[] msg, ObjectInputStream inStream, ObjectOutputStream outStream, Socket client) {
        String host = client.getRemoteSocketAddress().toString();
        System.out.println(ConsoleColors.GREEN_BRIGHT + "OBSERVER (Observer): Mensagem recebida de " + host);
        int msg_type = (int) msg[0];
        try {
            switch (msg_type) {
                case 1:
                    // print a nuvem inteira
                    Int2ObjectMap<Dot> nuvem = (Int2ObjectMap<Dot>) msg[1];
                    dots.putAll(nuvem);
                    synchronized(VERSAO){
                        VERSAO = 1;
                    }
                    outStream.writeObject(1);
                    printDots(nuvem);
                    //System.out.println("VERSÃO RECEBIDA: " + VERSAO);
                    break;
                case 2:
                    // print nos pontos atualizados
                    Int2ObjectMap<Dot> nuvemUpdate = (Int2ObjectMap<Dot>) msg[1];
                    dots.putAll(nuvemUpdate);
                    synchronized (VERSAO) {
                        VERSAO++;
                    }

                    printDots(nuvemUpdate);
                    //System.out.println("VERSÃO RECEBIDA: " + VERSAO);
                    break;
                default:
                    System.err.println("OBSERVER: Bad Request ERROR 500");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void printDots(Int2ObjectMap<Dot> dotBatch){
        for(Dot d : dotBatch.values()){
            panel.addDot(d);
        }
        panel.repaint();
    }
}