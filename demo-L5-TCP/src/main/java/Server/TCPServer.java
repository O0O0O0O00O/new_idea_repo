package Server;

import Client.Client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private final int port;
    private ClientListener mListener;

    public TCPServer(int port){
        this.port = port;
    }

    public boolean start(){
        try{
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            listener.start();
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop(){
        if(mListener != null){
            mListener.exit();
        }
    }



    private static class ClientListener extends Thread{
        private ServerSocket server;
        private boolean done = false;

        private ClientListener(int port) throws IOException{
            server = new ServerSocket(port);
            System.out.println("server info:" + server.getInetAddress() + "P:" +
                    port);
        }

        @Override
        public void run() {
            super.run();
            System.out.println("server" +
                    "ready");

            do{
                Socket client;
                try{
                    client = server.accept();
                }catch(IOException e){
                    continue;
                }

                ClientHandler clientHandler = new ClientHandler(client);
                clientHandler.start();

            }while(!done);
            System.out.println("server exit");
        }

        void exit(){
            done = true;
            try{
                server.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }


    }

    private static class ClientHandler extends Thread{
        private Socket socket;
        private boolean flag = true;

        ClientHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("new connection " + socket.getInetAddress() + "P:" +
                    socket.getPort());
            try{
                PrintStream socketOutput = new PrintStream(socket.getOutputStream());
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                do{
                    //客户端拿到一条数据
                    String str = socketInput.readLine();
                    if("bye".equalsIgnoreCase(str)){
                        flag = false;
                        socketOutput.println("bye");
                    }else{
                        System.out.println(str);
                        socketOutput.println(str.length());
                    }
                }while(flag);
                socketInput.close();
                socketOutput.close();

            }catch(IOException e){
                e.printStackTrace();
            }
            System.out.println("client have exit" + socket.getInetAddress() + "P:" +
                    socket.getPort());
        }
    }


}
