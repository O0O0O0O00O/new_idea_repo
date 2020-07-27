package Server;

import Client.Client;
import Server.handle.ClientHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {
    private final int port;
    private ClientListener mListener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>();


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

        for(ClientHandler clientHandler:clientHandlerList){
            clientHandler.exit();
        }
    }

    public void broadcast(String str){
        for(ClientHandler clientHandler:clientHandlerList){
            clientHandler.send(str);
        }
    }





    private class ClientListener extends Thread{
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

                ClientHandler clientHandler = null;
                try {
                    // 客户端构建异步线程
                    clientHandler = new ClientHandler(client, new ClientHandler.CloseNotify() {
                        @Override
                        public void onSelfClosed(ClientHandler handler) {
                            clientHandlerList.remove(handler);
                        }
                    });
                    // 读取数据并打印
                    clientHandler.readToPrint();
                    clientHandlerList.add(clientHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("client connection exception:" + e.getMessage());
                }

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

}
