
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

           /* while (buf.hasRemaining()) {
                buf.get();
            }*/

            /*
            int amount_read = -1;

            try { amount_read = chan.read((ByteBuffer)(buf.clear()));
            } catch (Throwable t) { }

            if (amount_read == -1)
                disconnect();

            if (amount_read < 1)
                return null; // if zero
*/
            //   int length = buf.array().length;
            System.out.println("String builder " + sb.toString());
            //System.out.println("sending back " + buf.position() + " bytes");
            // String s = new String(buf.array());
            //   System.out.println("String "+ s);
            // turn this bus right around and send it back!

            //   buf.flip();

            // String s = new String(buf.array());
            // map.put(chan.getRemoteAddress(),new String(buf.array()));
            // System.out.println(s);
            //   System.out.println();
            String pong = "PONG";
            String mes_whole = sb.toString();
            System.out.println("IS HEY CAME???: "+mes_whole.contains("Hey?"));
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
//////////
                /*else if (MainServer.map.get("1")!=null&&MainServer.map.get("1").equals(chan.getRemoteAddress().toString())) {
                    if(MainServer.messageBox.get("1")!=null) {
                        pong =MainServer.messageBox.get("1");
                        chan.write(ByteBuffer.wrap(pong.getBytes()));
                        MainServer.messageBox.remove("1");
                        map.remove("1");
                        return null;
                    }
                }*/
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

            System.out.println("Id of senderine: "+id_s+" message: "+messages[1] );


           /* if (MainServer.map.get("1")!=null&&MainServer.map.get("1").equals(chan.getRemoteAddress().toString())) {
                MainServer.messageBox.put("2",mes_whole1);
                System.out.println("Remote address: "+ getKeysByValue(MainServer.map,chan.getRemoteAddress().toString()).iterator().next());
            }
            else   if (MainServer.map.get("2")!=null&&MainServer.map.get("2").equals(chan.getRemoteAddress().toString())){
                MainServer.messageBox.put("1",mes_whole1);
                System.out.println("Remote address: "+ getKeysByValue(MainServer.map,chan.getRemoteAddress().toString()).iterator().next());
            }*/
            String my_id=getKeysByValue(MainServer.map,chan.getRemoteAddress().toString()).iterator().next();
            MainServer.messageBox.put(id_s,mes_whole1);
            if(MainServer.messageBox.get(my_id)!=null)
            {pong =MainServer.messageBox.get(my_id);
                MainServer.messageBox.remove(my_id);
                map.remove(my_id);
            }

           /* if (MainServer.map.get("2")!=null&&MainServer.map.get("2").equals(chan.getRemoteAddress().toString())) {

                if(MainServer.messageBox.get("2")!=null) {
                    pong =MainServer.messageBox.get("2");
                    MainServer.messageBox.remove("2");
                    map.remove("2");

                }
            }
//////////
            else if (MainServer.map.get("1")!=null&&MainServer.map.get("1").equals(chan.getRemoteAddress().toString())) {
                if(MainServer.messageBox.get("1")!=null) {
                    pong =MainServer.messageBox.get("1");
                    MainServer.messageBox.remove("1");
                    map.remove("1");
                }
            }
*/

            System.out.println(pong);
            chan.write(ByteBuffer.wrap(pong.getBytes()));/*


             */
            return buf;
        } catch (Throwable t) {
            System.out.println("Something bad happened....");
            disconnect();
            // t.printStackTrace();
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

    /*void write(ByteBuffer buffer){
        try {
            chan.write(buffer);
        } catch (IOException e) {
            System.out.println("NOOOOOOO....");
            e.printStackTrace();
        }

    }*/

}
