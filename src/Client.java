

import com.sun.xml.internal.bind.v2.runtime.output.SAXOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.Scanner;

public class Client {
    static StringBuilder sb1;
    static{
        sb1=new StringBuilder();
        for (int i = 0; i <1000 ; i++) {
            sb1.append(String.valueOf(1 + new Random().nextInt(8)));
        }
    }
    static volatile String sbf;
    static volatile String client;
    static volatile int count = 1;
    static volatile int phase = 1;
    public static void main(String[] args) throws IOException, InterruptedException {
        SocketAddress address = new InetSocketAddress("localhost",1337);
        SocketChannel
                socketChannel = SocketChannel.open(address);

        new Thread(()->{

            do {
                if (phase >1) {
                    while (sbf == null || chacker(sbf.trim())) {

                        String st = "Hey?:";
                        byte[] bytes = st.getBytes();
                        ByteBuffer buffer = ByteBuffer.wrap(bytes);
                        try {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            socketChannel.write(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    String ph = sbf.trim().split(":")[2];
                    String mes = sbf.trim().split(":")[1];

                    String ch=null;

                    switch (ph.trim()) {
                        case "1":
                            System.out.println("I'm in phase 1");
                            ch = cypher(mes.trim());
                            ph="2";
                            phase = 2;
                            break;
                        case "2":
                           System.out.println("I'm in phase 2");
                            ch = cypher(mes.trim());
                            ph="3";
                            phase = 3;
                            break;
                        case"3":
                            System.out.println("I'm in phase 3");
                            ch = cypher(mes.trim());
                            System.out.println("Decyphered: "+ch);
                            ph="1";
                            phase=1;
                            count=1;

                    }


                    if(ph.trim().equals("2")||ph.trim().equals("3")) {
                        String string = client + ":" + ch + ":" + ph;
                        byte[] bytes = string.getBytes();
                        ByteBuffer buffer = ByteBuffer.wrap(bytes);
                        try {
                            socketChannel.write(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        while (buffer.hasRemaining()) {
                            buffer.get();
                        }


                        buffer.clear();
                    }
                }
                sbf=null;
            }while(true);

        }).start();

        new Thread(()-> {
            while (phase>1);

            do {

                byte[] bytess = new byte[100];
                ByteBuffer buf = ByteBuffer.wrap(bytess);

                try {
                    socketChannel.read(buf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sbf=new String(buf.array());


            } while (true);
        }).start();


        new Thread(()-> {

            do {
                while (phase>1);
                if(count!=1) {

                    while (sbf == null || chacker(sbf)) {

                        String st="Hey?";
                        byte[] bytes = st.getBytes();
                        ByteBuffer buffer = ByteBuffer.wrap(bytes);
                        try {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            socketChannel.write(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    sbf=null;
                    phase++;
                    count=2;
                }

                System.out.println("Enter please: ");
                Scanner scanner = new Scanner(System.in);
                String str = scanner.nextLine();
                String ch = cypher(str);
                System.out.println("Enter client: ");

                client = scanner.nextLine();
                System.out.println("Wait for a reply...");
                String string = client+":" + ch+":"+phase;

                byte[] bytes = string.getBytes();
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                try {
                    socketChannel.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while (buffer.hasRemaining()) {
                    buffer.get();
                }


                buffer.clear();
                phase++;
                count=2;
            } while (true);
        }).start();


    }
    public static boolean chacker(String string){
        if(string.trim().length()!=4) return false;
        String pong ="PONG";
        for(int i=0;i<4;i++) {
            if(string.charAt(i)==pong.charAt(i)) continue;
            else return false;
        }
        return true;

    }
    public static String cypher(String str){
        StringBuilder sb;
        String key;


        key= sb1.toString();
        sb = new StringBuilder();
        for(int i = 0; i < str.length(); i++)
            sb.append((char)(str.charAt(i) ^ key.charAt(i)));
        return sb.toString();
    }

}
