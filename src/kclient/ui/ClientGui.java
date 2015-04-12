package kclient.ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import kclient.knuddels.GroupChat;
import kclient.knuddels.reflection.KLoader;
import kclient.knuddels.tools.ChatSystem;
import kclient.tools.Logger;
import kclient.tools.Parameter;

/**
 *
 * @author SeBi
 */
public class ClientGui implements ActionListener {
    private static ClientGui instance;
    private JFrame frame;
    private JLabel currentVersionLbl, versionLbl, progressLbl;
    private JProgressBar progress;
    private JTextArea logArea;
    private JTextField proxyField;
    
    private List<GroupChat> groupChats;
    
    private TrayIcon icon;
    private MenuItem trayClient;
    private MenuItem trayChannels;
    private MenuItem close;
    private MenuItem trayBingoFrames;
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.trayClient) {
            boolean show = this.trayClient.getLabel().equals("Show Client");
            this.trayClient.setLabel(show ? "Hide Client" : "Show Client");
            this.frame.setVisible(show);
        } else if (e.getSource() == this.trayChannels) {
            boolean show = this.trayChannels.getLabel().equals("Show Channels");
            this.trayChannels.setLabel(show ? "Hide Channels" : "Show Channels");
            for (GroupChat groupChat : this.groupChats) {
                Enumeration channels = groupChat.getChannels();

                while (channels.hasMoreElements()) {
                    ((Frame) channels.nextElement()).setVisible(show);
                }
            }
        } else if (e.getSource() == this.trayBingoFrames) {
            boolean show = this.trayBingoFrames.getLabel().equals("Show Bingo Frames");
            this.trayBingoFrames.setLabel(show ? "Hide Bingo Frames" : "Show Bingo Frames");
            for (GroupChat groupChat : this.groupChats) {
                groupChat.showBingoFrames = show;
                Enumeration frames = groupChat.getBingoFrames();
                while (frames.hasMoreElements()) {
                    ((Frame) frames.nextElement()).setVisible(show);
                }
            }
        } else if (e.getSource() == close) {
            for (GroupChat gc : this.groupChats)
                gc.stop();
            System.exit(0);
        }
    }

    private void start() {
        this.groupChats = new ArrayList<>();
        this.frame = new JFrame("KClient by SeBi - u-labs.de - Rev. 5");
         
        final TabbedPane tabbedPane = new TabbedPane();
        tabbedPane.setOpaque(false);
        
        //<editor-fold defaultstate="collapsed" desc="AddLogin">
        String[] strSystems = new String[ChatSystem.values().length];
        for (int i = 0; i < strSystems.length; i++)
            strSystems[i] = ChatSystem.values()[i].getName();
        final JComboBox systems = new JComboBox(strSystems);
        systems.addActionListener(new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                String system = systems.getSelectedItem().toString();
                ChatSystem sys = ChatSystem.fromName(system);
                ClientGui.this.currentVersionLbl.setText(sys.getCurrentVersion());
                ClientGui.this.versionLbl.setText(sys.getVersion());
            }
        });
        JButton btnAdd = new JButton("Hinzufügen");
        btnAdd.setOpaque(false);
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final ChatSystem system = ChatSystem.fromName(systems.getSelectedItem().toString());
                ClientGui.this.progress.setValue(0);
                ClientGui.this.progressLbl.setText("0 %");
                KLoader.getLoader(system).setProgress(ClientGui.this.progress, ClientGui.this.progressLbl);
                new Thread("GroupChatLoad") {
                    @Override
                    public void run() {
                        Logger.get().info(" Initializing Manipulation for " + system.getName());
                        if (!KLoader.getLoader(system).isReady())
                            KLoader.getLoader(system).prepare();
                        GroupChat groupChat = new GroupChat(system);
                        tabbedPane.addTab(system.getName(), groupChat.getComponent(), true);
                    }
                }.start();
            }
        });
        
        this.proxyField = new JTextField("");
        this.progress = new JProgressBar(0, 100);
        this.progressLbl = new JLabel("0 %");
        this.progressLbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        this.currentVersionLbl = new JLabel(ChatSystem.DE.getCurrentVersion());
        this.versionLbl = new JLabel(ChatSystem.DE.getVersion());
        
        JPanel root = new JPanel(new BorderLayout());
        
        JPanel labels = new JPanel(new GridLayout(4, 3, 0, 5));
        labels.setOpaque(false);
        labels.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 25));
        labels.add(new JLabel("<html><b>ChatSystem:</b></html>"));
        labels.add(systems);
        labels.add(new JLabel());
        labels.add(new JLabel("<html><b>Proxy:     </b></html>"));
        labels.add(this.proxyField);
        labels.add(new JLabel());
        labels.add(new JLabel());
        labels.add(btnAdd);
        labels.add(new JLabel());
        labels.add(new JLabel("<html><b>Manipulation:</b></html>"));
        labels.add(this.progress);
        labels.add(this.progressLbl);
        
        root.add(labels, BorderLayout.NORTH);
        //------------------------------------------------------------------
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.black));
        JPanel info = new JPanel(new GridLayout(2, 2));
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 100));
        info.add(new JLabel("<html><b>Version:</b></html>"));
        info.add(this.versionLbl);
        info.add(new JLabel("<html><b>Aktuelle Version:</b></html>"));
        info.add(this.currentVersionLbl);
        bottom.add(info, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        //</editor-fold>
        
        this.logArea = new JTextArea();
        
        TabbedPane clientTab = new TabbedPane();
        clientTab.addTab("Client", root, false);
        clientTab.addTab("Log", new JScrollPane(this.logArea), false);
        
        tabbedPane.addTab("KClient", clientTab, false);
        root.setBackground(Parameter.getDefault().getColor("background"));
        
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (GroupChat groupChat : ClientGui.this.groupChats)
                    groupChat.stop();
                SystemTray.getSystemTray().remove(icon);
                System.exit(0);
            }
        });
        
        ArrayList<Image> icons = new ArrayList<>();
        Image favicon = null;

        try {
            favicon = ImageIO.read(getClass().getResource("/res/icon/frame_icon_knuddel2.png"));
            icons.add(favicon);
        } catch (IOException e) {
            Logger.get().error(e);
        }

        this.frame.setIconImages(icons);

        if (SystemTray.isSupported()) {
            icon = new TrayIcon(favicon);
            PopupMenu menu = new PopupMenu();

            this.trayClient = new MenuItem("Hide Client");
            this.trayClient.addActionListener(this);
            menu.add(this.trayClient);

            this.trayBingoFrames = new MenuItem("Show Bingo Frames");
            this.trayBingoFrames.addActionListener(this);
            menu.add(this.trayBingoFrames);
            
            this.trayChannels = new MenuItem("Hide Channels");
            this.trayChannels.addActionListener(this);
            menu.add(this.trayChannels);

            this.close = new MenuItem("Close");
            this.close.addActionListener(this);
            menu.add(this.close);
            
            icon.setPopupMenu(menu);

            try {
                SystemTray.getSystemTray().add(icon);
            } catch (AWTException e) {
                Logger.get().error(e);
            }
        }
        
        this.frame.setSize(539, 280);
        this.frame.setMinimumSize(this.frame.getSize());
        this.frame.add(tabbedPane, BorderLayout.CENTER);
        this.frame.setVisible(true);
    }
   
    public void addLog(String str) {
        if (this.logArea != null)
            this.logArea.append(str + "\n\r");
    }
    public static ClientGui get() {
        return instance;
    }
    static {
        instance = new ClientGui();
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            Logger.get().info("Starting Client...");
            Logger.get().info(" Loading ChatSystems");
            ChatSystem.values();
            for (ChatSystem cs : ChatSystem.values())
                KLoader.getLoader(cs);
    
            instance.start();
        } catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | ClassNotFoundException ex) {
            Logger.get().error(ex);
        }
    }    
}
