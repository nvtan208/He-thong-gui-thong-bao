package notification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NotificationClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;
    private static JPanel notificationPanel;
    private static JScrollPane scrollPane;
    private static Socket socket;
    private static JLabel statusLabel;
    private static Thread receiveThread;
    private static JButton disconnectButton;
    private static JButton reconnectButton;
    private static JFrame mainFrame;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Ứng Dụng Nhận Thông Báo");
        mainFrame = frame;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 700);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        frame.add(headerPanel, BorderLayout.NORTH);

        notificationPanel = new JPanel();
        notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));
        notificationPanel.setBackground(new Color(248, 250, 252));
        notificationPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(248, 250, 252));
        wrapperPanel.add(notificationPanel, BorderLayout.NORTH);

        scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        addWelcomeMessage();
        connectToServer();

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                disconnect();
            }
        });
    }

    private static void createNotificationCard(String type, String content, String time) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        LineBorder originalBorder = new LineBorder(new Color(229, 231, 235), 1, true);
        card.setBorder(BorderFactory.createCompoundBorder(originalBorder, new EmptyBorder(10, 10, 10, 10)));
        card.setMinimumSize(new Dimension(100, 100));
        
        // **SỬA LỖI KHOẢNG TRẮNG: Vô hiệu hóa dòng này**
        // card.setPreferredSize(new Dimension(400, 110)); 
        
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        String title = type.equalsIgnoreCase("WEATHER") ? "Thông Báo Thời Tiết" : "Tin Tức Mới";
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showNotificationDetails(title, content, time);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(59, 130, 246), 2, true),
                    new EmptyBorder(9, 9, 9, 9)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(originalBorder, new EmptyBorder(10, 10, 10, 10)));
            }
        });

        ImageIcon icon;
        switch (type.toUpperCase()) {
            case "WEATHER": icon = IconManager.ICON_WEATHER; break;
            case "NEWS": icon = IconManager.ICON_NEWS; break;
            default: icon = IconManager.ICON_GENERAL; break;
        }
        JLabel iconLabel = new JLabel(icon);
        card.add(iconLabel, BorderLayout.WEST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(17, 24, 39));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contentArea.setForeground(new Color(75, 85, 99));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(Color.WHITE);
        contentArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(156, 163, 175));
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(contentArea);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(timeLabel);
        
        card.add(contentPanel, BorderLayout.CENTER);

        notificationPanel.add(card);
        notificationPanel.add(Box.createVerticalStrut(10));
        notificationPanel.revalidate();
        notificationPanel.repaint();

        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }
    
    // (Các phương thức còn lại không thay đổi)
    // ...
    private static JPanel createHeaderPanel(){
        JPanel headerPanel=new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(37,99,235));
        headerPanel.setBorder(new EmptyBorder(20,25,20,25));
        JLabel titleLabel=new JLabel("THÔNG BÁO",IconManager.ICON_GENERAL,JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI",Font.BOLD,22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIconTextGap(10);
        headerPanel.add(titleLabel,BorderLayout.WEST);
        statusLabel=new JLabel("Đang kết nối...",IconManager.ICON_NO_CONN,JLabel.RIGHT);
        statusLabel.setFont(new Font("Segoe UI",Font.BOLD,14));
        statusLabel.setForeground(new Color(255,255,255,180));
        headerPanel.add(statusLabel,BorderLayout.EAST);
        return headerPanel;
    }
    private static JPanel createBottomPanel(){
        JPanel bottomPanel=new JPanel(new FlowLayout(FlowLayout.CENTER,20,15));
        bottomPanel.setBackground(new Color(249,250,251));
        bottomPanel.setBorder(new EmptyBorder(10,20,15,20));
        disconnectButton=createStyledButton("Ngắt Kết Nối",new Color(239,68,68),IconManager.ICON_NO_CONN);
        disconnectButton.addActionListener(e->disconnect());
        reconnectButton=createStyledButton("Kết Nối Lại",new Color(34,197,94),IconManager.ICON_CONNECTED);
        reconnectButton.setEnabled(false);
        reconnectButton.addActionListener(e->connectToServer());
        bottomPanel.add(disconnectButton);
        bottomPanel.add(reconnectButton);
        return bottomPanel;
    }
    private static JButton createStyledButton(String text,Color backgroundColor,ImageIcon icon){
        JButton button=new JButton(text,icon);
        button.setFont(new Font("Segoe UI",Font.BOLD,14));
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(12,25,12,25));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseEntered(java.awt.event.MouseEvent evt){
                if(button.isEnabled()){
                    button.setBackground(backgroundColor.darker());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt){
                button.setBackground(backgroundColor);
            }
        });
        return button;
    }
    private static void connectToServer(){
        try{
            if(socket!=null&&!socket.isClosed())socket.close();
            socket=new Socket(SERVER_ADDRESS,SERVER_PORT);
            statusLabel.setText("Đã kết nối");
            statusLabel.setIcon(IconManager.ICON_CONNECTED);
            statusLabel.setForeground(new Color(34,197,94));
            addStatusMessage("Đã kết nối đến server thành công!",new Color(34,197,94));
            disconnectButton.setEnabled(true);
            reconnectButton.setEnabled(false);
            receiveThread=new Thread(()->{
                try(BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()))){
                    String message;
                    while((message=in.readLine())!=null){
                        if(message.startsWith("BLOCKED|")){
                            handleBlockMessage(message);
                            return;
                        }
                        processNotification(message);
                    }
                }catch(IOException e){
                    SwingUtilities.invokeLater(()->{
                        statusLabel.setText("Mất kết nối");
                        statusLabel.setIcon(IconManager.ICON_ERROR);
                        statusLabel.setForeground(new Color(239,68,68));
                        addStatusMessage("Mất kết nối với server.",new Color(239,68,68));
                        disconnectButton.setEnabled(false);
                        reconnectButton.setEnabled(true);
                    });
                }
            });
            receiveThread.start();
        }catch(IOException e){
            statusLabel.setText("Kết nối thất bại");
            statusLabel.setIcon(IconManager.ICON_ERROR);
            statusLabel.setForeground(new Color(239,68,68));
            addStatusMessage("Không thể kết nối đến server.",new Color(239,68,68));
            disconnectButton.setEnabled(false);
            reconnectButton.setEnabled(true);
        }
    }
    private static void disconnect(){
        try{
            if(receiveThread!=null)receiveThread.interrupt();
            if(socket!=null)socket.close();
            statusLabel.setText("Đã ngắt kết nối");
            statusLabel.setIcon(IconManager.ICON_NO_CONN);
            statusLabel.setForeground(new Color(239,68,68));
            addStatusMessage("Đã ngắt kết nối theo yêu cầu.",new Color(251,146,60));
            disconnectButton.setEnabled(false);
            reconnectButton.setEnabled(true);
        }catch(IOException e){
            addStatusMessage("Lỗi khi ngắt kết nối: "+e.getMessage(),new Color(239,68,68));
        }
    }
    private static void handleBlockMessage(String message){
        String[] parts=message.split("\\|",3);
        String reason=parts.length>1?parts[1]:"Không có lý do cụ thể";
        SwingUtilities.invokeLater(()->{
            statusLabel.setText("Bị chặn");
            statusLabel.setIcon(IconManager.ICON_ERROR);
            statusLabel.setForeground(new Color(239,68,68));
            showBlockDialog(reason);
            disconnectButton.setEnabled(false);
            reconnectButton.setEnabled(true);
        });
        disconnect();
    }
    private static void showBlockDialog(String reason){
        JPanel dialogPanel=new JPanel(new BorderLayout(15,15));
        dialogPanel.setBorder(new EmptyBorder(20,25,20,25));
        JLabel iconLabel=new JLabel(IconManager.ICON_ERROR);
        dialogPanel.add(iconLabel,BorderLayout.WEST);
        JPanel textPanel=new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel,BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        JLabel titleLabel=new JLabel("Bạn Đã Bị Chặn");
        titleLabel.setFont(new Font("Segoe UI",Font.BOLD,18));
        titleLabel.setForeground(new Color(220,53,69));
        JLabel reasonLabel=new JLabel("<html><body style='width: 200px'><b>Lý do:</b> "+reason+"</body></html>");
        reasonLabel.setFont(new Font("Segoe UI",Font.PLAIN,14));
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(reasonLabel);
        dialogPanel.add(textPanel,BorderLayout.CENTER);
        JOptionPane.showMessageDialog(mainFrame,dialogPanel,"Thông Báo Chặn",JOptionPane.PLAIN_MESSAGE,null);
    }
    private static void addWelcomeMessage(){
        JPanel welcomePanel=new JPanel(new BorderLayout());
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(new LineBorder(new Color(229,231,235),1,true));
        welcomePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,100));
        JLabel welcomeIcon=new JLabel(IconManager.ICON_GENERAL);
        welcomeIcon.setBorder(new EmptyBorder(10,15,10,15));
        JPanel textPanel=new JPanel(new BorderLayout());
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(new EmptyBorder(15,0,15,15));
        JLabel titleLabel=new JLabel("Chào mừng!");
        titleLabel.setFont(new Font("Segoe UI",Font.BOLD,16));
        titleLabel.setForeground(new Color(17,24,39));
        JLabel descLabel=new JLabel("Bạn sẽ nhận được thông báo thời tiết và tin tức ở đây");
        descLabel.setFont(new Font("Segoe UI",Font.PLAIN,12));
        descLabel.setForeground(new Color(107,114,128));
        textPanel.add(titleLabel,BorderLayout.NORTH);
        textPanel.add(descLabel,BorderLayout.CENTER);
        welcomePanel.add(welcomeIcon,BorderLayout.WEST);
        welcomePanel.add(textPanel,BorderLayout.CENTER);
        notificationPanel.add(welcomePanel);
        notificationPanel.add(Box.createVerticalStrut(10));
        notificationPanel.revalidate();
        notificationPanel.repaint();
    }
    private static void addStatusMessage(String message,Color color){
        SwingUtilities.invokeLater(()->{
            JPanel statusPanel=new JPanel(new FlowLayout(FlowLayout.CENTER));
            statusPanel.setBackground(new Color(248,250,252));
            statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
            JLabel statusLabel=new JLabel(message);
            statusLabel.setFont(new Font("Segoe UI",Font.PLAIN,12));
            statusLabel.setForeground(color);
            statusPanel.add(statusLabel);
            notificationPanel.add(statusPanel);
            notificationPanel.add(Box.createVerticalStrut(5));
            notificationPanel.revalidate();
            notificationPanel.repaint();
            JScrollBar vertical=scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    private static void processNotification(String message){
        SwingUtilities.invokeLater(()->{
            try{
                String[] parts=message.split("\\|",3);
                if(parts.length>=2){
                    String type=parts[0];
                    String content=parts[1];
                    String time=parts.length>2?parts[2]:getCurrentTime();
                    createNotificationCard(type,content,time);
                }else{
                    createSimpleNotificationCard(message);
                }
            }catch(Exception e){
                createSimpleNotificationCard(message);
            }
        });
    }
    private static void showNotificationDetails(String title,String content,String time){
        JDialog dialog=new JDialog((Frame)null,"Chi Tiết Thông Báo",true);
        dialog.setSize(400,300);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());
        JPanel mainPanel=new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(new EmptyBorder(20,20,20,20));
        JLabel titleLabel=new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI",Font.BOLD,18));
        mainPanel.add(titleLabel,BorderLayout.NORTH);
        JTextArea contentArea=new JTextArea(content);
        contentArea.setFont(new Font("Segoe UI",Font.PLAIN,14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(mainPanel.getBackground());
        JScrollPane contentScroll=new JScrollPane(contentArea);
        mainPanel.add(contentScroll,BorderLayout.CENTER);
        JLabel timeLabel=new JLabel("Thời gian: "+time);
        timeLabel.setFont(new Font("Segoe UI",Font.PLAIN,12));
        timeLabel.setForeground(new Color(107,114,128));
        mainPanel.add(timeLabel,BorderLayout.SOUTH);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    private static void createSimpleNotificationCard(String message){
        createNotificationCard("GENERAL",message,getCurrentTime());
    }
    private static String getCurrentTime(){
        return new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date());
    }
}