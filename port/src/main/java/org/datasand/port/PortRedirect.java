package org.datasand.port;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by saichler on 6/1/16.
 */
public class PortRedirect extends Thread{
    private final ServerSocket source;
    private final ServerSocket destination;
    private final String sourceHost;
    private final int sourcePort;
    private final String destHost;
    private final int destPort;
    private final int connectionType;

    public static final int TYPE_LISTEN_AND_CONNECT  = 1;
    public static final int TYPE_CONNECT_AND_CONNECT = 2;
    public static final int TYPE_LISTEN_AND_LISTEN   = 3;

    public PortRedirect(String sourceHost,int sourcePort,String destHost,int destPort,int connectionType){
        this.destHost = destHost;
        this.destPort = destPort;
        this.sourceHost = sourceHost;
        this.sourcePort = sourcePort;
        this.connectionType = connectionType;

        ServerSocket sourceS = null;
        ServerSocket destS = null;

        try {
            switch (this.connectionType) {
                case TYPE_LISTEN_AND_CONNECT:
                    sourceS = new ServerSocket(sourcePort, 10, InetAddress.getByName(sourceHost));
                    break;
                case TYPE_CONNECT_AND_CONNECT:
                    Socket s = new Socket(sourceHost, sourcePort);

                    byte[] data = new byte[1024];
                    int numberRead = s.getInputStream().read(data);
                    Socket d = new Socket(destHost, destPort);
                    d.getOutputStream().write(data,0,numberRead);
                    d.getOutputStream().flush();

                    new Redirect(s, d);
                    new Redirect(d, s);
                    break;
                case TYPE_LISTEN_AND_LISTEN:
                    sourceS = new ServerSocket(sourcePort, 10, InetAddress.getByName(sourceHost));
                    destS = new ServerSocket(destPort, 10, InetAddress.getByName(destHost));
                    break;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        this.source = sourceS;
        this.destination = destS;
        this.start();
    }

    public void run(){
        System.out.println("Port redirect started ("+this.sourceHost+":"+this.sourcePort+" <--> "+this.destHost+":"+this.destPort+")");
        if(this.connectionType==TYPE_CONNECT_AND_CONNECT){
            return;
        }
        while(true){
            try{
                Socket s = this.source.accept();
                if(this.connectionType==TYPE_LISTEN_AND_CONNECT) {
                    Socket d = new Socket(destHost, destPort);
                    System.out.println("New connection from " + s.getInetAddress().getHostAddress());
                    new Redirect(s, d);
                    new Redirect(d, s);
                } else {
                    Socket d = this.destination.accept();
                    System.out.println("New connection from " + s.getInetAddress().getHostAddress());
                    new Redirect(s, d);
                    new Redirect(d, s);
                }
            }catch(IOException e){
                e.printStackTrace();
                break;
            }
        }
    }

    private static class Redirect extends Thread {
        private final Socket inSocket;
        private final Socket outSocket;

        public Redirect(Socket inSocket,Socket outSocket){
            this.inSocket = inSocket;
            this.outSocket = outSocket;
            this.start();
        }

        public void run(){
            while(true){
                try{
                    byte[] data = new byte[1024];
                    int numberRead = this.inSocket.getInputStream().read(data);
                    //System.out.println("Read "+numberRead+ " from "+this.inSocket.getInetAddress().getHostAddress()+":"+this.inSocket.getPort());
                    if(numberRead==-1){
                        System.out.println("End Listening");
                        break;
                    }
                    this.outSocket.getOutputStream().write(data,0,numberRead);
                    this.outSocket.getOutputStream().flush();
                    //System.out.println("Wrote "+numberRead+ " from "+this.outSocket.getInetAddress().getHostAddress()+":"+this.outSocket.getPort());
                }catch(IOException e){
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public static void main(String args[]){
        new PortRedirect(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]),Integer.parseInt(args[4]));
    }
}
