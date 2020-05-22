package common;

import java.awt.GraphicsEnvironment;

/**
 * Provides similar functionality to {@link java.io.Console}, allowing access to the system console or a desktop interface.
 */
public final class Console {
    
    private static final java.io.Console systemConsole = System.console();

    /**
     * Should this console try to use implementations from {@link java.io.Console}.
     * Not to be confused with {@link #preferSystem}
     */
    public final boolean allowSystem;
    
    /**
     * Should this console try to use implementations from {@link DesktopConsole}
     */
    public final boolean allowDesktop;
    
    /**
     * If this console can use implementations from {@link java.io.Console} and
     * {@link DesktopConsole} should it prefer {@link java.io.Console}
     * @see #allowSystem
     * @see #allowDesktop
     */
    public final boolean preferSystem;
    
    /**
     * Initializes a new Console
     * @param allowSystem should this console try to use implementations from {@link java.io.Console}
     * @param allowDesktop should this console try to use implementations from {@link DesktopConsole}
     * @param preferSystem if this console can use implementations from {@link java.io.Console} and
     * {@link DesktopConsole} should it prefer {@link java.io.Console}
     * @throws IllegalArgumentException If no valid console implementation could be found
     */
    public Console(boolean allowSystem, boolean allowDesktop, boolean preferSystem) throws IllegalArgumentException {
        allowSystem = allowSystem && systemConsole != null;
        allowDesktop = allowDesktop && !GraphicsEnvironment.isHeadless();
        this.allowSystem = allowSystem;
        this.allowDesktop = allowDesktop;
        this.preferSystem = preferSystem;
        if(!(allowDesktop || allowSystem)) {
            throw new IllegalArgumentException("Could Not Find Valid Console for inputs: " + allowSystem + " " + allowDesktop + " " + preferSystem);
        }
    }
    
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
     * @throws IllegalStateException If no valid console implementation could be found
     * @see java.io.Console#format(java.lang.String, java.lang.Object...) 
     * @see DesktopConsole#format(java.lang.String, java.lang.Object...) 
     */
    public void format​(String fmt, Object... args) throws IllegalStateException {
        checkState();
        if(allowSystem && (preferSystem || !allowDesktop)) {
            systemConsole.format(fmt, args);
        } else {
            DesktopConsole.format(fmt, args);
        }
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
     * @throws IllegalStateException If no valid console implementation could be found
     * @see java.io.Console#printf​(java.lang.String, java.lang.Object...) 
     * @see DesktopConsole#printf​(java.lang.String, java.lang.Object...) 
     */
    public void printf​(String format, Object... args) throws IllegalStateException {
        checkState();
        if(allowSystem && (preferSystem || !allowDesktop)) {
            systemConsole.printf​(format, args);
        } else {
            DesktopConsole.printf​(format, args);
        }
    }
    
    /**
     * Reads a single line of text from the user
     * @return A string containing the line read from the user
     * @throws IllegalStateException IllegalStateException If no valid console implementation could be found
     * @see java.io.Console#readLine() 
     * @see DesktopConsole#readLine() 
     */
    public String readLine() throws IllegalStateException {
        checkState();
        if(allowSystem && (preferSystem || !allowDesktop)) {
            return systemConsole.readLine();
        } else {
            return DesktopConsole.readLine();
        }
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
     * @throws IllegalStateException IllegalStateException If no valid console implementation could be found
     * @see java.io.Console#readLine​(java.lang.String, java.lang.Object...) 
     * @see DesktopConsole#readLine(java.lang.String, java.lang.Object...) 
     */
    public String readLine​(String fmt, Object... args) throws IllegalStateException {
        checkState();
        if(allowSystem && (preferSystem || !allowDesktop)) {
            return systemConsole.readLine(fmt, args);
        } else {
            return DesktopConsole.readLine(fmt, args);
        }
    }
    
    /**
     * Reads a password or passphrase from the user
     * @return A character array containing the password or passphrase read from the user
     * @throws IllegalStateException IllegalStateException If no valid console implementation could be found
     * @see java.io.Console#readPassword() 
     * @see DesktopConsole#readPassword() 
     */
    public char[] readPassword() throws IllegalStateException {
        checkState();
        if(allowSystem && (preferSystem || !allowDesktop)) {
            return systemConsole.readPassword();
        } else {
            return DesktopConsole.readPassword();
        }
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
     * @throws IllegalStateException IllegalStateException If no valid console implementation could be found
     * @see java.io.Console#readPassword​(java.lang.String, java.lang.Object...) 
     * @see DesktopConsole#readPassword​(java.lang.String, java.lang.Object...) 
     */
    public char[] readPassword​(String fmt, Object... args) throws IllegalStateException {
        checkState();
        if(allowSystem && (preferSystem || !allowDesktop)) {
            return systemConsole.readPassword​(fmt, args);
        } else {
            return DesktopConsole.readPassword​(fmt, args);
        }
    }
    
    /**
     * Checks if this Console supports a valid console implementation
     * @throws IllegalStateException If no valid console implementation could be found
     */
    private void checkState() throws IllegalStateException {
        if(!(allowDesktop || allowSystem)) {
            throw new IllegalStateException("Console is Invalid");
        }
    }
    
}