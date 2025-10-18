package notification;

import javax.swing.*;
import java.awt.*;

public class IconManager {

    public static ImageIcon loadIcon(String name, int w, int h) {
        try {
            ImageIcon raw = new ImageIcon(IconManager.class.getResource("/icons/" + name + ".png"));
            Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            System.err.println("⚠️ Không tìm thấy icon: " + name);
            return null;
        }
    }

    // Các icon thường dùng
    public static final ImageIcon ICON_WEATHER   = loadIcon("cloudy", 32, 32);
    public static final ImageIcon ICON_NEWS      = loadIcon("newspaper-folded", 32, 32);
    public static final ImageIcon ICON_GENERAL   = loadIcon("rocket", 32, 32);
    public static final ImageIcon ICON_CONNECTED = loadIcon("check", 20, 20);
    public static final ImageIcon ICON_ERROR     = loadIcon("error", 20, 20);
    public static final ImageIcon ICON_SERVER    = loadIcon("desktop", 24, 24);
    public static final ImageIcon ICON_NO_CONN   = loadIcon("no-connection", 20, 20);
}
