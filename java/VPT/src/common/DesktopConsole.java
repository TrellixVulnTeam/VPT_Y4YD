package common;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * Provides similar functionality to {@link java.io.Console} but utilizing a desktop interface.
 */
public final class DesktopConsole {
    
    /**
     * Displays a formatted string to the user
     * @param fmt A format string as described in <a
    *         href="../util/Formatter.html#syntax">Format string syntax</a>
     * @param args Arguments referenced by the format specifiers in the format
    *         string.  If there are more arguments than format specifiers, the
    *         extra arguments are ignored.  The number of arguments is
    *         variable and may be zero.  The maximum number of arguments is
    *         limited by the maximum dimension of a Java array as defined by
    *         <cite>The Java&trade; Virtual Machine Specification</cite>.
    *         The behaviour on a
    *         {@code null} argument depends on the <a
    *         href="../util/Formatter.html#syntax">conversion</a>.
    */
    public static void format​(String fmt, Object... args) {
        JOptionPane.showMessageDialog(null, String.format(fmt, args), "VPT", JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     * A convenience method to display a formatted string to the user
     * 
     * <p> An invocation of this method of the form
    * {@code con.printf(format, args)} behaves in exactly the same way
    * as the invocation of
    * <pre>con.format(format, args)</pre>.
     * @param format A format string as described in <a
    *         href="../util/Formatter.html#syntax">Format string syntax</a>
     * @param args Arguments referenced by the format specifiers in the format
    *         string.  If there are more arguments than format specifiers, the
    *         extra arguments are ignored.  The number of arguments is
    *         variable and may be zero.  The maximum number of arguments is
    *         limited by the maximum dimension of a Java array as defined by
    *         <cite>The Java&trade; Virtual Machine Specification</cite>.
    *         The behavior on a
    *         {@code null} argument depends on the <a
    *         href="../util/Formatter.html#syntax">conversion</a>.
     */
    public static void printf​(String format, Object... args) {
        format​(format, args);
    }
    
    /**
     * Reads a single line of text from the user
     * @return A string containing the line read from the user
     */
    public static String readLine() {
        return JOptionPane.showInputDialog(null, "", "VPT", JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     * Provides a formatted prompt, then reads a single line of text from the user
     * @param fmt A format string as described in <a
    *         href="../util/Formatter.html#syntax">Format string syntax</a>
     * @param args Arguments referenced by the format specifiers in the format
    *         string.  If there are more arguments than format specifiers, the
    *         extra arguments are ignored.  The number of arguments is
    *         variable and may be zero.  The maximum number of arguments is
    *         limited by the maximum dimension of a Java array as defined by
    *         <cite>The Java&trade; Virtual Machine Specification</cite>.
    *         The behavior on a
    *         {@code null} argument depends on the <a
    *         href="../util/Formatter.html#syntax">conversion</a>.r
     * @return A string containing the line read from the user
     */
    public static String readLine​(String fmt, Object... args) {
        return JOptionPane.showInputDialog(null, String.format(fmt, args), "VPT", JOptionPane.PLAIN_MESSAGE);
        
    }
    
    /**
     * Reads a password or passphrase from the user
     * @return A character array containing the password or passphrase read from the user
     * @see java.io.Console#readPassword() 
     * @see DesktopConsole#readPassword() 
     */
    public static char[] readPassword() {
        JPasswordField pwField = new JPasswordField(50);
        JOptionPane.showInputDialog(null, pwField, "VPT", JOptionPane.PLAIN_MESSAGE);
        return pwField.getPassword();
    }
    
    /**
     * Provides a formatted prompt, then reads a password or passphrase from the user
     * @param fmt A format string as described in <a
    *         href="../util/Formatter.html#syntax">Format string syntax</a>
     * @param args Arguments referenced by the format specifiers in the format
    *         string.  If there are more arguments than format specifiers, the
    *         extra arguments are ignored.  The number of arguments is
    *         variable and may be zero.  The maximum number of arguments is
    *         limited by the maximum dimension of a Java array as defined by
    *         <cite>The Java&trade; Virtual Machine Specification</cite>.
    *         The behavior on a
    *         {@code null} argument depends on the <a
    *         href="../util/Formatter.html#syntax">conversion</a>.r
     * @return A character array containing the password or passphrase read from the user
     * @see java.io.Console#readPassword​(java.lang.String, java.lang.Object...) 
     * @see DesktopConsole#readPassword​(java.lang.String, java.lang.Object...) 
     */
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