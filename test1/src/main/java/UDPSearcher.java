

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class UDPSearcher {
    private static final int LISTEN_PORT = 30000;

    public static void main(String agrs[]) throws IOException{
        System.out.println("UDPSearcher start");

        Listener listener = listen();
        sendBoradcastMessage();

        System.in.read();
        listener.exit();
        List<Device> devices = listener.getDevices();

        System.out.println("yes");
        for(Device device : devices){
            System.out.println(device.toString());
        }

    }

    public static Listener listen(){
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, countDownLatch);
        listener.start();

        try{
            countDownLatch.await();
        }catch(Exception e){
            ;
        }
        return listener;

    }

    private static class Device{
        private final String ip;
        private final int port;
        private final String sn;
        public Device(String ip, int port, String sn){
            this.ip = ip;
            this.port = port;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" + "ip:" + ip
                    + ", port:" + port
                    + ", sn:" + sn;
        }
    }

    private static class Listener extends Thread{
        private boolean done = false;
        private final CountDownLatch countDownLatch;
        private final int listenport;
        private DatagramSocket ds = null;
        private static List<Device> devices = new ArrayList<>();

        public Listener(int listenport, CountDownLatch countDownLatch){
            this.listenport = listenport;
            this.countDownLatch = countDownLatch;
            done = false;
        }

        @Override
        public void run() {
            super.run();
            countDownLatch.countDown();
            System.out.println("Listener start");
            try{
                ds = new DatagramSocket(listenport);
                while(!done) {
                    byte[] listenbyte = new byte[512];
                    DatagramPacket listenpack = new DatagramPacket(listenbyte, listenbyte.length);
                    ds.receive(listenpack);

                    // 解析文档
                    String ip = listenpack.getAddress().getHostAddress();
                    int port = listenpack.getPort();
                    int datalen = listenpack.getLength();
                    String data = new String(listenpack.getData(), 0, datalen);
                    System.out.println("UDPSearcher receive from ip:" + ip
                            + "\tport:" + port + "\tdata:" + data);

                    String sn = MessageCreator.parseSn(data);
                    if (sn != null) {
                        Device device = new Device(ip, port, MessageCreator.parseSn(data));
                        devices.add(device);
                    }
                }
            }catch(Exception e){
                ;
            }finally{
                close();
            }
            System.out.println("Listener finished");
        }

        public List<Device> getDevices(){
            return devices;
        }

        private void close(){
            if(ds != null){
                ds.close();
                ds = null;
            }
        }

        private void exit(){
            close();
            done = true;
        }

    }



    private static void sendBoradcastMessage() {
        System.out.println("start sendbroadcast message");

        try {
            DatagramSocket ds = new DatagramSocket();
            String senddata = MessageCreator.buildWithport(LISTEN_PORT);
            byte[] sendbyte = senddata.getBytes();
            DatagramPacket sendpack = new DatagramPacket(sendbyte,
                    sendbyte.length);
            sendpack.setAddress(Inet4Address.getByName("255.255.255.255"));
            sendpack.setPort(20000);
            ds.send(sendpack);
            ds.close();
        }catch(Exception e){
            ;
        }finally{
            ;
        }
    }
}


