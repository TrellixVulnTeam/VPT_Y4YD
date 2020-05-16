package common;

import java.awt.GraphicsEnvironment;

public final class Console {
    
    private static java.io.Console systemConsole = System.console();

    public final boolean allowSystem, allowDesktop, preferSystem;
    
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
    
    public void format​(String fmt, Object... args) throws IllegalStateException {
        checkState();
        if(allowSystem && (preferSystem || !allowDesktop)) {
            systemConsole.format(fmt, args);
        } else {
            DesktopConsole.format(fmt, args);
        }
    }
    
    public void printf​(String format, Object... args) throws IllegalStateException {
        checkState();
        if(allowSystem && (preferSystem || !allowDesktop)) {
            systemConsole.printf​(format, args);
        } else {
            DesktopConsole.printf​(format, args);
        }
    }
    
    public String readLine() throws IllegalStateException {
        checkState();
        if(allowSystem && (preferSystem || !allowDesktop)) {
            return systemConsole.readLine();
        } else {
            return DesktopConsole.readLine();
        }
    }
    
    public String readLine​(String fmt, Object... args) throws IllegalStateException {
        checkState();
        if(allowSystem && (preferSystem || !allowDesktop)) {
            return systemConsole.readLine(fmt, args);
        } else {
            return DesktopConsole.readLine(fmt, args);
        }
    }
    
    public char[] readPassword() throws IllegalStateException {
        checkState();
        if(allowSystem && (preferSystem || !allowDesktop)) {
            return systemConsole.readPassword();
        } else {
            return DesktopConsole.readPassword();
        }
    }
    
    public char[] readPassword​(String fmt, Object... args) throws IllegalStateException {
        checkState();
        if(allowSystem && (preferSystem || !allowDesktop)) {
            return systemConsole.readPassword​(fmt, args);
        } else {
            return DesktopConsole.readPassword​(fmt, args);
        }
    }
    
    private void checkState() throws IllegalStateException {
        if(!(allowDesktop || allowSystem)) {
            throw new IllegalStateException("Console is Invalid");
        }
    }
    
}