package Client;

import Client.bean.ServerInfo;
import Server.handle.ClientHandler;
import clink.utils.CloseUtils;

import java.io.*;
import java.net.*;

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

            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            readHandler.start();
            // 发送接受数据
            write(socket);

            //推出操作
            readHandler.exit();

        }catch(Exception e){
            System.out.println("Exception exit");
        }
        socket.close();
        System.out.println("Client exit~");

    }

    private static void write(Socket client) throws IOException{
        // 构建输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        // 得到Socket输出流，并转换成打印流
        OutputStream outputStream = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);

        do {
            String str = input.readLine();
            socketPrintStream.println(str);
            if("00bye00".equalsIgnoreCase(str)){
                break;
            }
        }while(true);
        socketPrintStream.close();


    }

    static class ReadHandler extends Thread{
        private boolean done = false;
        private final InputStream inputStream;

        public ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            super.run();
            try{
                // 得到输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do{
                    String str;
                    try {
                        //客户端拿到一条数据
                        str = socketInput.readLine();
                        if (str == null) {
                            System.out.println("connection have exit, can not read the data");
                            // 退出当前客户端
                            break;
                        }
                    }catch (SocketTimeoutException e){
                        continue;
                    }
                    System.out.println(str);
                }while(!done);

            }catch(IOException e){
                if(!done){
                    System.out.println("connection exit because of exception" + e.getMessage());
                }
            }finally {
                CloseUtils.close(inputStream);
            }
        }

        void exit(){
            done = true;
            CloseUtils.close(inputStream);
        }


    }

}
