package Client;

import Client.bean.ServerInfo;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.Buffer;

public class TCPClient {
    public static void linkWith(ServerInfo info) throws IOException{
        Socket socket = new Socket();

        // 超过时间
        socket.setSoTimeout(3000);

        //链接本地，端口2000， 超过时间3000ms
        socket.connect(new InetSocketAddress(Inet4Address.getByName(
                info.getAddress()
        ), info.getPort()), 3000);

        System.out.println("send the server request， waiting ... ");
        System.out.println("Client info:" + socket.getLocalAddress() + "P:" + socket.getLocalPort());;
        System.out.println("Server info:" + socket.getInetAddress() +"P:" + socket.getPort());


        try{
            // 发送接受数据
            todo(socket);
        }catch(Exception e){
            System.out.println("Exception exit");
        }

        socket.close();
        System.out.println("Client exit~");

    }

    private static void todo(Socket client) throws IOException{
        // 构建输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        // 得到Socket输出流，并转换成打印流
        OutputStream outputStream = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);

        // 得到Socket输入流， 并转换成BufferReader
        InputStream inputStream = client.getInputStream();
        BufferedReader socketBufferReader = new BufferedReader(new InputStreamReader(inputStream));

        boolean flag = true;
        do {
            String str = input.readLine();
            socketPrintStream.println(str);

            //从服务器读取一行
            String echo = socketBufferReader.readLine();
            if("bye".equalsIgnoreCase(echo)){
                flag = false;
            }else{
                System.out.println(echo);
            }
        }while(flag);

        socketBufferReader.close();
        socketPrintStream.close();




    }

}
