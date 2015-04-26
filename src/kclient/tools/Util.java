package kclient.tools;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.TrayIcon;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import kclient.ui.ClientGui;

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
            Logger.get().error(e);
        }
    }

    public static String getMac() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for(byte b: mac)
               sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        } catch (IOException e) {
            Logger.get().error(e);
        }
        return "";
    }
    
    public static String sendStats(String action, String... param) {
        try {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < param.length; i += 2)
                buffer.append("&").append(param[i]).append("=").append(param[i + 1]);
            URL requestUrl = new URL("http://knds.sebitm.info/kclient/stats.php?a=" + action + buffer.toString());
            URLConnection con = requestUrl.openConnection();
            con.setConnectTimeout(3000);
            con.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; de; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3");
            con.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
            con.connect();

            buffer = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String decodedString;
            while ((decodedString = in.readLine()) != null)
                buffer.append(decodedString);
            in.close();
            return buffer.toString();
        } catch (IOException e) {
            Logger.get().error(e);
        }
        return "";
    }
    
    public static String loadSite(String url) {
        try {
            StringBuilder buffer = new StringBuilder();
            URL requestUrl = new URL(url);
            URLConnection con = requestUrl.openConnection();
            con.setConnectTimeout(3000);
            con.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; de; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3");
            con.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
            con.connect();

            buffer = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String decodedString;
            while ((decodedString = in.readLine()) != null)
                buffer.append(decodedString);
            in.close();
            return buffer.toString();
        } catch (IOException e) {
            Logger.get().error(e);
        }
        return "";
    }
    
    public static String escape(String packet) {
        return packet.replace("\u0000", "\\0").replace("\n", "\\n");
    }
    
    public static String escapeKCode(String message) {
        return message.replace("\\>", ">").replace("\\<", "<").replace("\\", "\\\\").replace("\"", "\\\"")
                        .replace("#", "\\#").replace("_", "\\_").replace("§", "\\§")
                        .replace("°", "\\°").trim();
    }
    
    public static void showNotification(String title, String message) {
        ClientGui.get().getTray().displayMessage(title, message, TrayIcon.MessageType.INFO);
    }
    
    public static void playSound(String file) {
        new Thread("Sound [" + file + "]") {
            @Override
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inp = AudioSystem.getAudioInputStream(new File("data" + File.separator + "sounds" + File.separator + file + ".wav"));
                    clip.open(inp);
                    clip.start();
                } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
                    Logger.get().error(e);
                }
            }
        }.start();
    }
}
