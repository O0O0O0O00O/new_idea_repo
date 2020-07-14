import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class UDPProvide {
    public static void main(String[] args) throws IOException{
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();

        System.in.read();
        provider.exit();


    }

    private static class Provider extends Thread{
        private final String sn;
        private boolean done;
        private DatagramSocket ds = null;

        public Provider(String sn){
            super();
            this.sn = sn;
            done = false;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("UDPProvide start");
            try {
                ds = new DatagramSocket(20000);
                while(!done) {
                    final byte[] bytes = new byte[512];
                    DatagramPacket receivepack = new DatagramPacket(bytes, bytes.length);

                    ds.receive(receivepack);

                    //获取发送者的信息
                    String ip = receivepack.getAddress().getHostAddress();
                    int port = receivepack.getPort();
                    int datalen = receivepack.getLength();
                    String data = new String(receivepack.getData(),0, datalen);
                    System.out.println("UDPProvider receive from ip:" + ip
                            + "\tport:" + port + "\tdata:" + data);

                    int responseport = MessageCreator.parsePort(data);
                    if(responseport != -1){
                        String responsedata = MessageCreator.buildWithdSn(sn);
                        byte[] responsebyte = responsedata.getBytes();
                        DatagramPacket response = new DatagramPacket(responsebyte,
                                responsebyte.length,
                                receivepack.getAddress(),
                                responseport);
                        ds.send(response);

                    }


                }

            }catch(Exception e){
                ;
            }finally {
                close();
            }

            System.out.println("UDPProvide finished");
        }

        private void close(){
            if(ds != null){
                ds.close();
                ds = null;
            }
        }

        private void exit(){
            done = true;
            close();
        }

    }

}

