package org.datasand.port;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by saichler on 6/1/16.
 */
public class PortRedirect extends Thread{
    private final ServerSocket source;
    private final String sourceHost;
    private final int sourcePort;
    private final String destHost;
    private final int destPort;

    public PortRedirect(String sourceHost,int sourcePort,String destHost,int destPort){
        this.destHost = destHost;
        this.destPort = destPort;
        this.sourceHost = sourceHost;
        this.sourcePort = sourcePort;
        ServerSocket ss = null;
        try{
            ss = new ServerSocket(sourcePort,10, InetAddress.getByName(sourceHost));
        }catch(IOException e){
            e.printStackTrace();
        }
        this.source = ss;
        this.start();
    }

    public void run(){
        System.out.println("Port redirect started ("+this.sourceHost+":"+this.sourcePort+" <--> "+this.destHost+":"+this.destPort+")");
        while(true){
            try{
                Socket s = this.source.accept();
                Socket d = new Socket(destHost,destPort);
                System.out.println("New connection from "+s.getInetAddress().getHostAddress());
                new Redirect(s,d);
                new Redirect(d,s);
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
                    if(numberRead==-1){
                        break;
                    }
                    this.outSocket.getOutputStream().write(data,0,numberRead);
                    this.outSocket.getOutputStream().flush();
                }catch(IOException e){
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public static void main(String args[]){
        new PortRedirect(args[0],Integer.parseInt(args[1]),args[2],Integer.parseInt(args[3]));
    }
}
