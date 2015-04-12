package kclient.tools;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author SeBi
 */
public class Util {
    public static String join(String deli, Object[] arr) {
        StringBuilder buffer = new StringBuilder();
        for (Object obj : arr)
            buffer.append(obj.toString()).append(deli);
        buffer.delete(buffer.length() - deli.length(), buffer.length());
        return buffer.toString();
    }
    
    public static int rnd(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }
    
    public static void downloadFile(String save, String url) {
        try {
            BufferedInputStream in = null;
            FileOutputStream fout = null;
            try {
                in = new BufferedInputStream(new URL(url).openStream());
                fout = new FileOutputStream(save);

                final byte data[] = new byte[1024];
                int count;
                while ((count = in.read(data, 0, 1024)) != -1) {
                    fout.write(data, 0, count);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (fout != null) {
                    fout.close();
                }
            }
        } catch (IOException e) {
            Logger.get().error(e.toString());
        }
    }
}
