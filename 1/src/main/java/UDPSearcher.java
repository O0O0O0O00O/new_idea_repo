import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class UDPSearcher {
    private static final int LISTEN_PORT = 30000;


    public static void main(String[] args) throws IOException {
        System.out.println("UDPSearcher Started.");

        Listener listener = listen();
        sendBroadcast();

        System.in.read();

        List<Device>devices = listener.getDevicesAndclose();

        for (Device device : devices){
            System.out.println("Device:" + device.toString());
        }

        //完成
        System.out.println("UDPSearcher Finished");
    }

    public static Listener listen(){
        System.out.println("UDPSearcher listen start");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, countDownLatch);
        listener.start();

        try {
            countDownLatch.await();
        }catch(Exception e){
            ;
        }
        return listener;
    }

    private static void sendBroadcast(){
        System.out.println("UDPSearcher sendBroadcast Started.");

        try {
            //作为搜索放, 让系统自动分配
            DatagramSocket ds = new DatagramSocket();

            //构建一份回送报告
            String requestData = MessageCreator.buildWithport(LISTEN_PORT);
            byte[] requestDataBytes = requestData.getBytes();
            //直接根据发送者构建一份回送信息
            DatagramPacket requestPacket = new DatagramPacket(requestDataBytes,
                    requestDataBytes.length);
            // 20000端口，广播地址
            requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
            requestPacket.setPort(20000);

            //发送
            ds.send(requestPacket);
            ds.close();

            // 完成
            System.out.println("UDPSearcher sendBroadcast finished");
        }catch(Exception e){
            ;
        }finally {
            ;
        }
    }

    private static class Device{
        final int port;
        final String ip;
        final String sn;
        private Device(int port, String ip, String sn){
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{ +" +
                    "port=" + port +
                    ", ip=" + ip +
                    ", sn=" + sn +
                    "}";
        }
    }

    private static class Listener extends Thread{
        private final int listenPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> devices = new ArrayList<>();
        private boolean done = false;
        private DatagramSocket ds = null;


        public Listener(int listenPort, CountDownLatch couuntDownLatch){
            super();
            this.countDownLatch = couuntDownLatch;
            this.listenPort = listenPort;

        }



        @Override
        public void run() {
            super.run();

            //通知已启动
            countDownLatch.countDown();
            try{
                ds = new DatagramSocket(listenPort);
                while(!done){
                    final byte[] buf = new byte[512];
                    DatagramPacket receivePack = new DatagramPacket(buf, buf.length);

                    // 接收
                    ds.receive(receivePack);

                    //打印接收到的信息与发布者的信息
                    //发送者的ip地址
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int datalen = receivePack.getLength();
                    String data = new String(receivePack.getData(), 0, datalen);
                    System.out.println("UDPSearcher receive from ip:" + ip
                            + "\tport:" + port + "\tdata:" + data);

                    String sn = MessageCreator.parseSn(data);
                    if(sn!=null){
                        Device device = new Device(port, ip, sn);
                        devices.add(device);
                    }
                }

            }catch(Exception e){

            }finally {
                close();
            }

            System.out.println("UDPSearcher Listen Finished.");

        }

        public List<Device> getDevicesAndclose(){
            exit();
            return this.devices;
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
