package clink.utils;

public class ByteUtils {

    public static boolean startsWith(byte[] source, byte[] match){
        return startsWith(source, 0, match);
    }

    public static boolean startsWith(byte[] source, int offset, byte[] match){
        if(match.length > (source.length - offset)){
            return false;
        }

        for(int i = 0; i < match.length; ++i){
            if(source[offset + i] != match[i]){
                return false;
            }
        }
        return true;
    }

    public static boolean equals(byte[] source, byte[] match){
        if(match.length != source.length){
            return false;
        }
        return startsWith(source, 0, match);
    }

    /**
     * Copies bytes from the source byte array to the destination array
     *
     * @param source      The source array
     * @param srcBegin    Index of the first source byte to copy
     * @param srcEnd      Index after the last source byte to copy
     * @param destination The destination array
     * @param desBegin    The starting offset in the destination array
     */
    public static void getBytes(byte[] source, int srcBegin, int srcEnd,
                                byte[] destination, int desBegin){
        System.arraycopy(source, srcBegin, destination, desBegin,srcEnd-srcBegin);
    }

    public static byte[] subbytes(byte[] source, int srcBegin, int srcEnd){
        byte[] destination = new byte[srcEnd - srcBegin];
        getBytes(source, srcBegin, srcEnd, destination, 0);
        return destination;
    }


}
