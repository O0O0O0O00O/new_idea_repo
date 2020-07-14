import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

public class UDPProvider {
    public static void main(String[] args) throws IOException{
        //生成一份唯一表示
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();

        //读取任意键盘信息后可以退出
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
            System.out.println("UDPProvider Started.");

            try {
                //作为接收者，指定一个端口用于数据接受
                ds = new DatagramSocket(20000);
                while(!done){
                    //构建接收实体
                    final byte[] buf = new byte[512];
                    DatagramPacket receivePack = new DatagramPacket(buf, buf.length);

                    ds.receive(receivePack);

                    //打印接收到的信息与发布者的信息
                    //发送者的ip地址
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int datalen = receivePack.getLength();
                    String data = new String(receivePack.getData(), 0, datalen);
                    System.out.println("UDPProvider receive from ip:" + ip
                            + "\tport:" + port + "\tdata:" + data);

                    //解析端口号
                    int responsePort = MessageCreator.parsePort(data);
                    if(responsePort != -1){
                        //构建一份回送报告
                        String responseData = MessageCreator.buildWithdSn(sn);
                        byte[] responseDataBytes = responseData.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes,
                                responseDataBytes.length,
                                receivePack.getAddress(),
                                responsePort);
                        ds.send(responsePacket);
                    }

                }
            }catch(Exception ignored){
                ;
            }finally {
                close();
            }
            System.out.println("UDPProvider Finished");
        }

        private void close(){
            if(ds!=null){
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
