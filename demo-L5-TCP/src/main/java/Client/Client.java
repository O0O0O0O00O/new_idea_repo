package Client;

import Client.bean.ServerInfo;

import java.io.IOException;

public class Client {
    public static void main(String[] args){
        ServerInfo info = UDPSearcher.searchServer(1000);
        System.out.println("Server:" + info);
        if(info != null){
            try{
                TCPClient.linkWith(info);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}

