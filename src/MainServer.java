import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class MainServer {

    public static void main(String[] args) throws Throwable {
        new MainServer(new InetSocketAddress("localhost", 1337));
    }
    static int count = 0;
    ServerSocketChannel serverChannel;
    Selector selector;
    SelectionKey serverKey;

    SelectionKey readKey;
    MainServer(InetSocketAddress listenAddress) throws Throwable {
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverKey = serverChannel.register(selector = Selector.open(), SelectionKey.OP_ACCEPT);
        serverChannel.bind(listenAddress);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                loop();
            } catch (Throwable t) {
                //t.printStackTrace();
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }


    String generateId(){
        return ""+ (++count);
    }


    static HashMap<SelectionKey, ClientSession> clientMap = new HashMap<SelectionKey, ClientSession>();
    static HashMap<String, String> map = new HashMap<>();
    static HashMap<String, String> messageBox = new HashMap<>();

    void loop() throws Throwable {
        selector.selectNow();

        for (SelectionKey key : selector.selectedKeys()) {
            try {
                if (!key.isValid())
                    continue;

                if (key == serverKey) {  //if key is acceptable

                    SocketChannel acceptedChannel = serverChannel.accept();
                    String string = generateId();
                    System.out.println("Accepted connection from: "+string+" with address: "+acceptedChannel.getRemoteAddress());
                    map.put(string,acceptedChannel.getRemoteAddress().toString());
                    if (acceptedChannel == null)
                        continue;

                    acceptedChannel.configureBlocking(false);

                    readKey = acceptedChannel.register(selector, SelectionKey.OP_READ);


                    clientMap.put(readKey, new ClientSession(readKey, acceptedChannel));

                    System.out.println("New client ip=" + acceptedChannel.getRemoteAddress() + ", total clients=" + MainServer.clientMap.size());
                    // Thread.sleep(5000);





//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                }

                if (key.isReadable()) {

                    System.out.println("Key is readable");
                    ClientSession sesh = clientMap.get(key);

                    if (sesh == null)
                        continue;

                    sesh.read();

                    selector.selectedKeys().clear();

                }

                /////////////////////////////////////////////////////////////////
              /*  else if (key.isWritable()) {

                    ClientSession sesh = clientMap.get(key);



                    System.out.println("Begin writing...");
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    toWrite = serverChannel.accept();

                    if ( toWrite == null)
                        continue;

                    toWrite.configureBlocking(false);

                    if (sesh == null)
                        continue;

                    sesh.write(buf);
                }*/
////////////////////////////////////////////////////////////////////////////////////////
            } catch (Throwable t) {
                //t.printStackTrace();
            }
        }

        selector.selectedKeys().clear();
    }

}
