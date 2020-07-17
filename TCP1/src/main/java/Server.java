import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;

public class Server {
    private static final int PORT = 20000;
    private static final int LOCAL_PORT = 20001;
    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = createServerSocket();
        initServerSocket(serverSocket);
        // baklog设置当前可允许等待链接的队列为50个
        serverSocket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 50);

        System.out.println("server ready");
        System.out.println("server info:" + serverSocket.getInetAddress() + "P:" + serverSocket.getLocalPort());

        for(;;){
            Socket client = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(client);
            clientHandler.start();
        }


    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private boolean flag;

        ClientHandler(Socket client) {
            this.socket = client;
        }

        public void run() {
            super.run();
            System.out.println("new client connection:" + this.socket.getInetAddress() + "P:" + this.socket.getPort());
            this.flag = true;

            try {
                PrintStream socketOutput = new PrintStream(this.socket.getOutputStream());
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

                do {
                    String str = socketInput.readLine();
                    if ("bye".equalsIgnoreCase(str)) {
                        socketOutput.println("bye");
                        this.flag = false;
                    } else {
                        System.out.println(str);
                        socketOutput.println("answer:" + str.length());
                    }
                } while(this.flag);

                socketInput.close();
                socketOutput.close();
            } catch (Exception var12) {
                System.out.println("exception, connection close");
            } finally {
                try {
                    this.socket.close();
                } catch (IOException var11) {
                    var11.printStackTrace();
                }

            }

            System.out.println("server close" + this.socket.getInetAddress() + "P:" + this.socket.getPort());
        }
    }

    private static ServerSocket createServerSocket() throws IOException{
        ServerSocket serverSocket = new ServerSocket();

        return serverSocket;
    }

    private static void initServerSocket(ServerSocket serverSocket) throws IOException{
        serverSocket.setReuseAddress(true);

        serverSocket.setReceiveBufferSize(64*1024*1024);

        // serverSocket.setSoTimeout(2000);

        serverSocket.setPerformancePreferences(1, 1, 1);
    }
}
