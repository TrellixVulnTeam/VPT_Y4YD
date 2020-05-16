package common;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public final class DesktopConsole {
    
    public static void format​(String fmt, Object... args) {
        JOptionPane.showMessageDialog(null, String.format(fmt, args), "VPT", JOptionPane.PLAIN_MESSAGE);
    }
    
    public static void printf​(String format, Object... args) {
        format(format, args);
    }
    
    public static String readLine() {
        return JOptionPane.showInputDialog(null, "", "VPT", JOptionPane.PLAIN_MESSAGE);
    }
    
    public static String readLine​(String fmt, Object... args) {
        return JOptionPane.showInputDialog(null, String.format(fmt, args), "VPT", JOptionPane.PLAIN_MESSAGE);
        
    }
    
    public static char[] readPassword() {
        JPasswordField pwField = new JPasswordField(50);
        JOptionPane.showInputDialog(null, pwField, "VPT", JOptionPane.PLAIN_MESSAGE);
        return pwField.getPassword();
    }
    
    public static char[] readPassword​(String fmt, Object... args) {
        JPanel display = new JPanel();
        JPasswordField pwField = new JPasswordField(50);
        display.add(new JLabel(String.format(fmt, args)));
        display.add(pwField);
        JOptionPane.showMessageDialog(null, display, "VPT", JOptionPane.PLAIN_MESSAGE);
        return pwField.getPassword();
    }
    
    private DesktopConsole() {}
    
}