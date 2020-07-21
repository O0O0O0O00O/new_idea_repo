package Client;

import Client.bean.ServerInfo;
import clink.utils.ByteUtils;
import constants.UDPConstants;

import javax.xml.crypto.Data;
import java.io.IOError;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UDPSearcher {
    private static final int LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    public static ServerInfo searchServer(int timeout){
        System.out.println("UDPSearcher Started");

        // 成功收到回送的栅栏
        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null;
        try{
            listener = listen(receiveLatch);
            sendBroadcast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        }catch(Exception e){
            e.printStackTrace();;
        }
        List<ServerInfo> devices = listener.getServerAndClose();
        if(devices.size() > 0)
            return devices.get(0);
        return null;
    }


    private static Listener listen(CountDownLatch receiverLatch) throws InterruptedException{
        System.out.println("UDPSearcher start listen.");
        CountDownLatch startDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, startDownLatch, receiverLatch);
        listener.start();
        startDownLatch.await();
        return listener;
    }

    private static void sendBroadcast() throws IOException{
        System.out.println("UDPSearcher sendBroadcast started.");

        //作为搜索方，让系统自动分配端口
        DatagramSocket ds = new DatagramSocket();

        //构建一份请求数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        //头部
        byteBuffer.put(UDPConstants.HEADER);
        //CMD命令
        byteBuffer.putShort((short)1);
        //回送端口信息
        byteBuffer.putInt(LISTEN_PORT);
        //直接构建packet
        DatagramPacket requestPacket = new DatagramPacket(byteBuffer.array(),
                byteBuffer.position() + 1);
        //广播地址
        requestPacket.setAddress(Inet4Address.getByName("255.255.255.255"));
        requestPacket.setPort(UDPConstants.PORT_SERVER);

        ds.send(requestPacket);
        ds.close();

        System.out.println("UDPSearcher sendBroadcast finished");
    }



    public static class Listener extends Thread{
        private final int listenPort;
        private final CountDownLatch startDownLatch;
        private final CountDownLatch receiveDownLatch;
        private final List<ServerInfo> serverInfoList = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private final int minlen = UDPConstants.HEADER.length + 2 + 4;
        private boolean done  = false;
        private DatagramSocket ds = null;

        private Listener(int listenPort, CountDownLatch startDownLatch, CountDownLatch receiveDownLatch){
            this.listenPort = listenPort;
            this.startDownLatch = startDownLatch;
            this.receiveDownLatch = receiveDownLatch;
        }

        @Override
        public void run() {
            super.run();

            //通知已启动
            startDownLatch.countDown();
            try{
                // 监听回送端口
                ds = new DatagramSocket(listenPort);
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while(!done){
                    ds.receive(receivePack);

                    //打印接收到的信息与发送者的信息
                    //发送者的IP地址
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int datalen = receivePack.getLength();
                    byte[] data = receivePack.getData();
                    boolean isValid = datalen >= minlen
                            && ByteUtils.startsWith(data, UDPConstants.HEADER);
                    System.out.println("UDPSearcher receive form ip:" + ip
                            + "\tport:" + port + "\tdataValid:" + isValid);

                    if(!isValid){
                        continue;
                    }

                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, datalen);
                    final short cmd = byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();
                    if(cmd != 2 || serverPort <= 0){
                        System.out.println("UDPSearcher receive cmd:" + cmd + "\tsercerPort:"+
                                serverPort);
                        continue;
                    }

                    String sn = new String(buffer, minlen, datalen - minlen);
                    ServerInfo info = new ServerInfo(sn, serverPort, ip);
                    serverInfoList.add(info);
                    //成功收到一份
                    receiveDownLatch.countDown();
                }
            }catch(Exception ignore){
                ;
            }finally{
                close();
            }
            System.out.println("UDPSearcher listener finished");
        }

        private void close(){
            if(ds != null){
                ds.close();
                ds = null;
            }
        }

        List<ServerInfo> getServerAndClose(){
            done = true;
            close();
            return serverInfoList;
        }

    }


}
