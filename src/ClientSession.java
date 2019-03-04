
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.stream.Collectors;

class ClientSession {

    SelectionKey selkey;
    SocketChannel chan;
    ByteBuffer buf;
    static HashMap<SocketAddress, String> map = new HashMap<>();
    ClientSession(SelectionKey selkey, SocketChannel chan) throws Throwable {
        this.selkey = selkey;
        this.chan = (SocketChannel) chan.configureBlocking(false); // asynchronous/non-blocking
        buf = ByteBuffer.allocate(25); // 64 byte capacity

    }

    void disconnect() {
        MainServer.clientMap.remove(selkey);
        try {
            if (selkey != null)
                selkey.cancel();

            if (chan == null)
                return;

            System.out.println("bye bye " + (InetSocketAddress) chan.getRemoteAddress());
            chan.close();
        } catch (Throwable t) { /** quietly ignore  */ }
    }

    ByteBuffer read() {
        try {
            StringBuilder sb = new StringBuilder();
            int bytes_read = chan.read(buf);
            while (bytes_read > 0) {
                buf.flip();
                while (buf.hasRemaining()) {
                    sb.append((char) buf.get());
                }
                buf.clear();
                bytes_read = chan.read(buf);
            }

            buf.clear();


            String pong = "PONG";
            String mes_whole = sb.toString();

            if(mes_whole.equals("Hey?:")){
                String my_id=getKeysByValue(MainServer.map,chan.getRemoteAddress().toString()).iterator().next();

                if (MainServer.map.get(my_id)!=null&&MainServer.map.get(my_id).equals(chan.getRemoteAddress().toString())) {

                    if(MainServer.messageBox.get(my_id)!=null) {
                        pong =MainServer.messageBox.get(my_id);
                        chan.write(ByteBuffer.wrap(pong.getBytes()));
                        MainServer.messageBox.remove(my_id);
                        map.remove(my_id);
                        return null;

                    }
                }

                chan.write(ByteBuffer.wrap(pong.getBytes()));
                return null;

            }



            String mes_whole1 = null;
            if(mes_whole.contains("Hey?")){
                mes_whole1 = mes_whole.substring(mes_whole.indexOf(":")+1);
            }
            else{
                mes_whole1 = mes_whole;
            }
            String[]messages = mes_whole1.split(":");
            String id_s = messages[0];

            System.out.println("Id of client to send message to: "+id_s+" message(cyphered): "+messages[1] );


            String my_id=getKeysByValue(MainServer.map,chan.getRemoteAddress().toString()).iterator().next();
            MainServer.messageBox.put(id_s,mes_whole1);
            if(MainServer.messageBox.get(my_id)!=null)
            {pong =MainServer.messageBox.get(my_id);
                MainServer.messageBox.remove(my_id);
                map.remove(my_id);
            }


            chan.write(ByteBuffer.wrap(pong.getBytes()));

            return buf;
        } catch (Throwable t) {
            System.out.println("Something bad happened....");
            disconnect();

            return null;
        }
    }

    public static <String> Set<String> getKeysByValue(Map<String, String> map, String value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }



}
