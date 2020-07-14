public class MessageCreator {
    private static final String SN_HEADER = "receive password, i am SN:";
    private static final String PORT_Header = "this is passowrd, " +
            "please answer use this port:";

    public static String buildWithport(int port){
        return PORT_Header + port;
    }

    public static int parsePort(String data){
        if(data.startsWith(PORT_Header)){
            return Integer.parseInt(data.substring(PORT_Header.length()));
        }
        return -1;
    }

    public static String buildWithdSn(String data){
        return SN_HEADER + data;
    }

    public static String parseSn(String data){
        if(data.startsWith(SN_HEADER)){
            return data.substring(SN_HEADER.length());
        }
        return null;
    }

}
