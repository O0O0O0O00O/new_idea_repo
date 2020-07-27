package clink.utils;

import java.io.Closeable;

public class CloseUtils {
    public static void close(Closeable...closeables){
        if(closeables == null){
            return;
        }
        for(Closeable closeable: closeables){
            try {
                closeable.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
