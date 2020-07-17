import java.io.*;
import java.net.*;

public class Client {
    private static final int PORT = 20000;
    private static final int LOCAL_PORT = 20001;

    public static void main(String[] agrs) throws IOException{

        Socket socket = createSocket();

        initSocket(socket);

        // 链接到本地20000端口，超时时间3秒，超过则抛出异常
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 30000);

        System.out.println("send the request of connecting the server, waiting for the next progress");
        System.out.println("client info:" + socket.getLocalAddress() + "P:" + socket.getLocalPort());
        System.out.println("server info:" + socket.getInetAddress() + "P" + socket.getPort());

        try{
            todo(socket);
        }catch(Exception e){
            System.out.println("client exception");
        }

        socket.close();
        System.out.println("client exit");

    }

    private static Socket createSocket() throws IOException{
        /*
        // 无代理模式，等效于空构造函数
        Socket socket = new Socket(Proxy.NO_PROXY);

        //新建一份具有HTTP代理的套接字，船速数据将通过www.baidu.com：8080端口转发
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Inet4Address.getByName("www.baidu.com"), 8080));
        socket = new Socket(proxy);

        // 锌价一个套接字， 并且直接链接到本地20000的服务器上
        socket = new Socket("localhost", 20000);

        //新建一个套接字，冰洁直接链接到本地20000的服务器上
        socket = new Socket(Inet4Address.getLocalHost(), 20000);

        //新建一个套接字，并且直接链接到本地20000的服务器上，并且绑定到本地20001端口上
        socket = new Socket("localhost", PORT, Inet4Address.getLocalHost(), LOCAL_PORT);
        socket = new Socket(Inet4Address.getLocalHost(), PORT, Inet4Address.getLocalHost(), LOCAL_PORT);

         */
        Socket socket = new Socket();
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), LOCAL_PORT));

        return socket;
    }

    private static void initSocket(Socket socket) throws SocketException{

        // 设置读取超时时间位2秒
        socket.setSoTimeout(2000);

        //是否复用未完全关闭的Socket地址，对于指定bind操作后的套接字有效
        socket.setReuseAddress(true);

        // 是否开启Nagle算法
        // 避免每次发送只有一个字节
        //socket。setTcpNoDelay(false);

        // 是否需要在长时无数据响应发送确认数据（类似心跳包）， 时间大约为2小时
        socket.setKeepAlive(true);

        // 对于close关闭操作行为进行怎样的处理；默认未false，0
        // false，0：默认情况，关闭时立即返回，底层系统接管输出流，将缓冲区内的数据发送完毕
        // true, 0:关闭时立即返回， 缓冲区数据抛弃，直接发送RST结束命令到对方，并无需经过2MSL等待
        // true，200：关闭时最长阻塞200毫秒， 随后按第二种情况处理
        socket.setSoLinger(true, 20);

        // 是否让紧急数据内敛，默认未false；紧急数据通过 socket.sendUrgentData(1);发送
        socket.setOOBInline(true);

        // 这只接收发送缓冲器大小
        socket.setReceiveBufferSize(64*1024*1024);
        socket.setSendBufferSize(64*1024*1024);

        //设置性能参数：短链接，延迟，带宽的相对重要性
        socket.setPerformancePreferences(1, 1, 1);

    }

    private static void todo(Socket client) throws IOException{


        // 得到Socket输出流，并转化为打印流
        OutputStream outputStream = client.getOutputStream();

        // 得到socket输入流，并转换成BufferedReader
        InputStream inputStream = client.getInputStream();
        byte[] buffer = new byte[128];

        outputStream.write(new byte[]{1});

        int read = inputStream.read(buffer);
        if(read > 0){
            System.out.println("data length：" + read + " data:" + new String(buffer));
        }else{
            System.out.println("data length:" + read + " data:" + new String(buffer));
        }


        // 资源释放
        inputStream.close();
        outputStream.close();
    }

    private void cl(Socket client) throws IOException{
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        OutputStream outputStream = client.getOutputStream();
        PrintStream socketoutputStream = new PrintStream(outputStream);

        InputStream inputStream = client.getInputStream();
        BufferedReader socketinputStream = new BufferedReader(new InputStreamReader(inputStream));



    }
}





