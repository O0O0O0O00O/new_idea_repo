package Server.handle;

import clink.utils.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private Socket socket;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final CloseNotify closeNotify;

    public ClientHandler(Socket socket, CloseNotify closeNotify) throws IOException {

        this.socket = socket;
        this.readHandler = new ClientReadHandler(socket.getInputStream());
        this.writeHandler = new ClientWriteHandler(socket.getOutputStream());
        this.closeNotify = closeNotify;
        System.out.println("new client connection:" + socket.getInetAddress() +
                " P:" + socket.getPort());
    }



    public void exit() {
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socket);
        System.out.println("client exit:" + socket.getInetAddress() +
                " P:" + socket.getPort());
    }

    public void send(String str) {
        writeHandler.send(str);
    }

    public void readToPrint() {
        readHandler.start();
    }


    private void exitBySelf(){
        exit();
        closeNotify.onSelfClosed(this);
    }

    public interface  CloseNotify{
        void onSelfClosed(ClientHandler handler);

    }

    class ClientReadHandler extends Thread{
        private boolean done = false;
        private final InputStream inputStream;

        public ClientReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            super.run();
            try{
                // 得到输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do{
                    //客户端拿到一条数据
                    String str = socketInput.readLine();
                    if(str == null){
                        System.out.println("client can not read the data");
                        // 退出当前客户端
                        ClientHandler.this.exitBySelf();
                        break;
                    }
                    System.out.println(str);

                }while(!done);

            }catch(IOException e){
                if(!done){
                    System.out.println("connection exit because of exception");
                    ClientHandler.this.exitBySelf();
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


    class ClientWriteHandler{
        private boolean done = false;
        private final PrintStream printStream;
        private final ExecutorService executorService;

        public ClientWriteHandler(OutputStream outputStream) {
            this.printStream = new PrintStream(outputStream);
            this.executorService = Executors.newSingleThreadExecutor();
        }


        private void exit(){
            done = true;
            CloseUtils.close(printStream);
            executorService.shutdownNow();
        }

        public void send(String str) {
            executorService.execute(new WriteRunnalbe(str));
        }

        class WriteRunnalbe implements  Runnable{
            private final String msg;

            public WriteRunnalbe(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                if(ClientWriteHandler.this.done){
                    return;
                }
                try {
                    ClientWriteHandler.this.printStream.println(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    }

}
