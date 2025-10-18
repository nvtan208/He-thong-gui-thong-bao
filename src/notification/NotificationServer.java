package notification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class NotificationServer {
    private static final int PORT = 8080;
    private static final ArrayList<ClientHandler> clients = new ArrayList<>();
    private static JTextArea logArea;
    private static DefaultTableModel clientTableModel;
    private static JTable clientTable;
    private static DefaultListModel<String> notificationListModel;
    private static JList<String> notificationJList;
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logException(e);
        }
        
        JFrame frame = createMainFrame();
        frame.setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        frame.add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = createTabbedPane();
        frame.add(tabbedPane, BorderLayout.CENTER);

        JPanel controlPanel = createControlPanel(frame);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
        appendLog("Server đang khởi động...");

        scheduleAPIDataFetching();
        startServer();
    }

    private static void showCreateNotificationDialog() {
        JDialog dialog = new JDialog((JFrame) null, "Tạo Thông Báo Mới", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Tạo Thông Báo Mới", IconManager.ICON_GENERAL, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        JLabel typeLabel = new JLabel("Loại thông báo:");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Thời tiết", "Tin tức"});
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel contentLabel = new JLabel("Nội dung:");
        contentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField contentField = new JTextField();
        contentField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        contentPanel.add(typeLabel);
        contentPanel.add(typeCombo);
        contentPanel.add(contentLabel);
        contentPanel.add(contentField);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton createBtn = createStyledButton("Tạo", new Color(34, 197, 94));
        createBtn.setIcon(IconManager.ICON_CONNECTED);
        JButton cancelBtn = createStyledButton("Hủy", new Color(239, 68, 68));
        cancelBtn.setIcon(IconManager.ICON_ERROR);

        createBtn.addActionListener(e -> {
            String content = contentField.getText().trim();
            if (!content.isEmpty()) {
                String type = typeCombo.getSelectedIndex() == 0 ? "WEATHER" : "NEWS";
                String displayText = (typeCombo.getSelectedIndex() == 0 ? "Thời tiết: " : "Tin tức: ") + content;
                String notification = type + "|" + content + "|" + getCurrentTime();
                
                // **SỬA LỖI EXCEPTION: Thêm thời gian vào chuỗi để định dạng nhất quán**
                notificationListModel.addElement(displayText + " (" + getCurrentTime() + ")");
                
                broadcast(notification);
                appendLog("Đã tạo và gửi thông báo thủ công: " + displayText);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập nội dung thông báo!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    // (Các phương thức còn lại không thay đổi)
    // ...
    private static JFrame createMainFrame(){
        JFrame frame=new JFrame("Bảng Điều Khiển Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900,700);
        frame.setLocationRelativeTo(null);
        return frame;
    }
    private static JPanel createHeaderPanel(){
        JPanel headerPanel=new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(37,99,235));
        headerPanel.setBorder(new EmptyBorder(15,20,15,20));
        JLabel titleLabel=new JLabel("SERVER THÔNG BÁO",IconManager.ICON_SERVER,JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI",Font.BOLD,24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIconTextGap(10);
        headerPanel.add(titleLabel,BorderLayout.WEST);
        JLabel statusLabel=new JLabel("Đang hoạt động",IconManager.ICON_CONNECTED,JLabel.LEFT);
        statusLabel.setFont(new Font("Segoe UI",Font.BOLD,16));
        statusLabel.setForeground(new Color(34,197,94));
        headerPanel.add(statusLabel,BorderLayout.EAST);
        return headerPanel;
    }
    private static JTabbedPane createTabbedPane(){
        JTabbedPane tabbedPane=new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI",Font.PLAIN,14));
        logArea=new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas",Font.PLAIN,12));
        logArea.setBackground(new Color(248,250,252));
        logArea.setBorder(new EmptyBorder(10,10,10,10));
        JScrollPane logScroll=new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Nhật ký hệ thống"));
        tabbedPane.addTab("Nhật Ký",logScroll);
        notificationListModel=new DefaultListModel<>();
        notificationJList=new JList<>(notificationListModel);
        notificationJList.setFont(new Font("Segoe UI",Font.PLAIN,12));
        notificationJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notificationJList.setBorder(new EmptyBorder(5,5,5,5));
        JScrollPane notificationScroll=new JScrollPane(notificationJList);
        notificationScroll.setBorder(BorderFactory.createTitledBorder("Danh sách thông báo"));
        tabbedPane.addTab("Thông Báo",notificationScroll);
        String[] columns={"Địa chỉ IP","Cổng","Trạng thái","Thời gian kết nối","Hành động"};
        clientTableModel=new DefaultTableModel(columns,0){
            private static final long serialVersionUID=1L;
            @Override
            public boolean isCellEditable(int row,int column){
                return column==4;
            }
        };
        clientTable=new JTable(clientTableModel);
        clientTable.setFont(new Font("Segoe UI",Font.PLAIN,12));
        clientTable.setRowHeight(25);
        clientTable.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        clientTable.getTableHeader().setBackground(new Color(243,244,246));
        TableColumn actionColumn=clientTable.getColumnModel().getColumn(4);
        actionColumn.setCellRenderer(new ButtonRenderer());
        actionColumn.setCellEditor(new ButtonEditor(new JCheckBox()));
        JScrollPane clientScroll=new JScrollPane(clientTable);
        clientScroll.setBorder(BorderFactory.createTitledBorder("Client đã kết nối"));
        tabbedPane.addTab("Client",clientScroll);
        return tabbedPane;
    }
    private static JPanel createControlPanel(JFrame frame){
        JPanel controlPanel=new JPanel(new FlowLayout(FlowLayout.CENTER,20,15));
        controlPanel.setBackground(new Color(249,250,251));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Bảng điều khiển"));
        JButton createNotificationBtn=createStyledButton("Tạo Thông Báo",new Color(34,197,94));
        createNotificationBtn.addActionListener(e->showCreateNotificationDialog());
        JButton sendSelectedBtn=createStyledButton("Gửi Thông Báo Đã Chọn",new Color(59,130,246));
        sendSelectedBtn.addActionListener(e->sendSelectedNotification(frame));
        controlPanel.add(createNotificationBtn);
        controlPanel.add(sendSelectedBtn);
        return controlPanel;
    }
    private static JButton createStyledButton(String text,Color backgroundColor){
        JButton button=new JButton(text);
        button.setFont(new Font("Segoe UI",Font.BOLD,14));
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseEntered(java.awt.event.MouseEvent evt){
                button.setBackground(backgroundColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt){
                button.setBackground(backgroundColor);
            }
        });
        return button;
    }
    private static void scheduleAPIDataFetching(){
        ScheduledExecutorService scheduler=Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(()->{
            SwingUtilities.invokeLater(()->{
                try{
                    String weather="Trời nắng, 32°C";
                    String news="Một sự kiện quan trọng vừa diễn ra.";
                    String weatherNotification="WEATHER|"+weather+"|"+getCurrentTime();
                    notificationListModel.addElement("Thời tiết: "+weather+" ("+getCurrentTime()+")");
                    broadcast(weatherNotification);
                    String newsNotification="NEWS|"+news+"|"+getCurrentTime();
                    notificationListModel.addElement("Tin tức: "+news+" ("+getCurrentTime()+")");
                    broadcast(newsNotification);
                    appendLog("Đã lấy dữ liệu API và gửi thông báo tự động");
                }catch(Exception e){
                    appendLog("Lỗi khi lấy dữ liệu API: "+e.getMessage());
                }
            });
        },5,15,TimeUnit.SECONDS);
    }
    private static void startServer(){
        new Thread(()->{
            try(ServerSocket serverSocket=new ServerSocket(PORT)){
                appendLog("Server đang chạy trên cổng "+PORT);
                while(true){
                    Socket clientSocket=serverSocket.accept();
                    String clientIP=clientSocket.getInetAddress().getHostAddress();
                    appendLog("Client mới kết nối: "+clientIP);
                    ClientHandler clientHandler=new ClientHandler(clientSocket);
                    synchronized(clients){
                        clients.add(clientHandler);
                    }
                    updateClientTable();
                    new Thread(clientHandler).start();
                }
            }catch(IOException e){
                appendLog("Lỗi server: "+e.getMessage());
            }
        }).start();
    }
    private static void sendSelectedNotification(JFrame frame){
        int selectedIndex=notificationJList.getSelectedIndex();
        if(selectedIndex!=-1){
            String selected=notificationListModel.getElementAt(selectedIndex);
            String type=selected.startsWith("Thời tiết")?"WEATHER":"NEWS";
            String content=selected.substring(selected.indexOf(":")+2,selected.lastIndexOf(" ("));
            String notification=type+"|"+content+"|"+getCurrentTime();
            broadcast(notification);
            appendLog("Đã gửi lại thông báo: "+selected);
        }else{
            JOptionPane.showMessageDialog(frame,"Vui lòng chọn một thông báo để gửi!","Thông báo",JOptionPane.WARNING_MESSAGE);
        }
    }
    private static void showBlockClientDialog(ClientHandler client){
        JDialog dialog=new JDialog((JFrame)null,"Chặn Client",true);
        dialog.setSize(400,250);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());
        JPanel mainPanel=new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(new EmptyBorder(20,20,20,20));
        JLabel titleLabel=new JLabel("Chặn Client: "+client.getSocket().getInetAddress().getHostAddress());
        titleLabel.setFont(new Font("Segoe UI",Font.BOLD,18));
        mainPanel.add(titleLabel,BorderLayout.NORTH);
        JPanel contentPanel=new JPanel(new GridLayout(2,1,10,10));
        JLabel reasonLabel=new JLabel("Lý do chặn:");
        reasonLabel.setFont(new Font("Segoe UI",Font.BOLD,14));
        JTextArea reasonArea=new JTextArea();
        reasonArea.setFont(new Font("Segoe UI",Font.PLAIN,14));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        JScrollPane reasonScroll=new JScrollPane(reasonArea);
        contentPanel.add(reasonLabel);
        contentPanel.add(reasonScroll);
        mainPanel.add(contentPanel,BorderLayout.CENTER);
        JPanel buttonPanel=new JPanel(new FlowLayout());
        JButton blockBtn=createStyledButton("Chặn",new Color(239,68,68));
        JButton cancelBtn=createStyledButton("Hủy",new Color(107,114,128));
        blockBtn.addActionListener(e->{
            String reason=reasonArea.getText().trim();
            if(!reason.isEmpty()){
                blockClient(client,reason);
                dialog.dispose();
            }else{
                JOptionPane.showMessageDialog(dialog,"Vui lòng nhập lý do chặn!","Lỗi",JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(e->dialog.dispose());
        buttonPanel.add(blockBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel,BorderLayout.SOUTH);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    private static void blockClient(ClientHandler client,String reason){
        try{
            client.sendMessage("BLOCKED|"+reason+"|"+getCurrentTime());
            synchronized(clients){
                clients.remove(client);
            }
            client.close();
            appendLog("Đã chặn client "+client.getSocket().getInetAddress().getHostAddress()+". Lý do: "+reason);
            updateClientTable();
        }catch(IOException e){
            appendLog("Lỗi khi chặn client: "+e.getMessage());
        }
    }
    private static void appendLog(String message){
        SwingUtilities.invokeLater(()->{
            logArea.append("["+getCurrentTime()+"] "+message+"\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    private static void logException(Exception e){
        appendLog("Lỗi: "+e.getMessage());
        e.printStackTrace();
    }
    private static String getCurrentTime(){
        return new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date());
    }
    private static void broadcast(String message){
        synchronized(clients){
            for(ClientHandler client:clients){
                client.sendMessage(message);
            }
        }
    }
    private static void updateClientTable(){
        SwingUtilities.invokeLater(()->{
            synchronized(clients){
                clientTableModel.setRowCount(0);
                for(ClientHandler client:clients){
                    clientTableModel.addRow(new Object[]{client.getSocket().getInetAddress().getHostAddress(),client.getSocket().getPort(),"Đã kết nối",client.getConnectTime(),"Chặn"});
                }
            }
        });
    }
    static class ClientHandler implements Runnable{
        private final Socket socket;
        private final String connectTime;
        private PrintWriter out;
        private volatile boolean running=true;
        public ClientHandler(Socket socket){
            this.socket=socket;
            this.connectTime=getCurrentTime();
            try{
                this.out=new PrintWriter(socket.getOutputStream(),true);
            }catch(IOException e){
                logException(e);
            }
        }
        public Socket getSocket(){
            return socket;
        }
        public String getConnectTime(){
            return connectTime;
        }
        public void sendMessage(String message){
            if(out!=null){
                out.println(message);
            }
        }
        public void close()throws IOException{
            running=false;
            if(out!=null){
                out.close();
            }
            if(socket!=null&&!socket.isClosed()){
                socket.close();
            }
        }
        @Override
        public void run(){
            try(BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()))){
                String input;
                while(running&&(input=in.readLine())!=null){
                    final String finalInput=input;
                    appendLog("Nhận từ client "+socket.getInetAddress().getHostAddress()+": "+finalInput);
                }
            }catch(IOException e){
                if(running){
                    appendLog("Client ngắt kết nối: "+socket.getInetAddress().getHostAddress());
                }
            }finally{
                synchronized(clients){
                    clients.remove(this);
                }
                updateClientTable();
                try{
                    close();
                }catch(IOException e){
                    logException(e);
                }
            }
        }
    }
    static class ButtonRenderer extends JButton implements TableCellRenderer{
        private static final long serialVersionUID=1L;
        public ButtonRenderer(){
            setOpaque(true);
            setText("Chặn");
            setBackground(new Color(239,68,68));
            setForeground(Color.BLACK);
            setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){
            return this;
        }
    }
    static class ButtonEditor extends DefaultCellEditor{
        private static final long serialVersionUID=1L;
        private String label;
        private JButton button;
        public ButtonEditor(JCheckBox checkBox){
            super(checkBox);
            button=new JButton();
            button.setOpaque(true);
            button.setBackground(new Color(239,68,68));
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
            button.addActionListener(e->{
                synchronized(clients){
                    int selectedRow=clientTable.getSelectedRow();
                    if(selectedRow>=0&&selectedRow<clientTableModel.getRowCount()){
                        String ipAddress=(String)clientTableModel.getValueAt(selectedRow,0);
                        ClientHandler client=null;
                        for(ClientHandler c:clients){
                            if(c.getSocket().getInetAddress().getHostAddress().equals(ipAddress)){
                                client=c;
                                break;
                            }
                        }
                        if(client!=null){
                            fireEditingStopped();
                            showBlockClientDialog(client);
                        }
                    }
                }
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table,Object value,boolean isSelected,int row,int column){
            label=(value==null)?"Chặn":value.toString();
            button.setText(label);
            return button;
        }
        @Override
        public Object getCellEditorValue(){
            return label;
        }
        @Override
        public boolean stopCellEditing(){
            return super.stopCellEditing();
        }
    }
}