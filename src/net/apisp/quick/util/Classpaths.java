package net.apisp.quick.util;

public class Classpaths {
    public static boolean existFile(String filename){
        if(filename.charAt(0) == '/'){
            filename = filename.substring(1);
        }
        return Classpaths.class.getResource("/" + filename) == null ? false : true;
    }
}
