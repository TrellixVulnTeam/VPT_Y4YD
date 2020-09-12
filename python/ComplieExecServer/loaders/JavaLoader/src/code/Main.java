package code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Main {
    
    private static final String DIR = formatFilename("C:\\Users\\ryand\\Desktop\\Folders\\Visual Studio Projects\\Projects\\VPT\\python\\ComplieExecServer\\loaders\\JavaLoader");

    private static boolean hasRun = false;
    public static final Function<String, String> stringParser = s -> s;
    public static final Function<String, Integer> intParser = Integer::parseInt;
    public static final Function<String, Boolean> boolParser = Boolean::parseBoolean;
    private static final ArrayList<String> libs = new ArrayList<>();
    
    public static void main(String[] args) {
        if(hasRun)
            return;
        hasRun = true;
        try {
            //Setup Vars
            String testId = args[0];
            String programLoc = args[1];
            File testsFile = new File(formatFilename(DIR + "/tests/" + testId + "_tests.tcfg"));
            File permissionsFile = new File(formatFilename(DIR + "/tests/" + testId + "_perms.tcfg"));
            File entrypointFile = new File(formatFilename(DIR + "/tests/" + testId + "_entrypoint.tcfg"));
            ArrayList<Class<?>> argTypes = new ArrayList<>();
            ArrayList<Object[]> tests = new ArrayList<>();
            Permissions permissions = new Permissions();
            
            //Load Tests and Args
            try(BufferedReader tReader = new BufferedReader(new FileReader(testsFile))) {
                ArrayList<Function<String, ?>> parsers = new ArrayList<>();
                String format = tReader.readLine();
                for(char c: format.toCharArray()) {
                    Function<String, ?> parser;
                    switch(c) {
                        case 's':
                            parser = stringParser;
                            argTypes.add(String.class);
                            break;
                        case 'i':
                            parser = intParser;
                            argTypes.add(Integer.TYPE);
                            break;
                        case 'b':
                            parser = boolParser;
                            argTypes.add(Boolean.TYPE);
                            break;
                        default:
                            throw new IllegalArgumentException("Illegal Format Char: " + c);
                    }
                    parsers.add(parser);
                }
                int numStaticArgs = Integer.parseInt(tReader.readLine());
                ArrayList<Object> staticArgs = new ArrayList<>();
                for(int i = 0; i < numStaticArgs; i++) {
                    staticArgs.add(parsers.get(i).apply(tReader.readLine()));
                }
                int numDynamicArgs = parsers.size() - numStaticArgs;
                while(tReader.readLine() != null) {
                    ArrayList<Object> dynamicArgs = new ArrayList<>();
                    boolean eof = false;
                    for(int i = 0; i < numDynamicArgs; i++) {
                        String line = tReader.readLine();
                        if(line == null) {
                            eof = true;
                            break;
                        }
                        line = line.substring(0, line.length()-1);
                        dynamicArgs.add(parsers.get(i+numStaticArgs).apply(line));
                    }
                    if(eof) {
                        break;
                    }
                    ArrayList<Object> testArgs = new ArrayList<>(staticArgs);
                    testArgs.addAll(dynamicArgs);
                    tests.add(testArgs.toArray());
                }
            } catch(IllegalArgumentException | IOException e) {
                System.out.println("Error");
                logError(e);
                System.exit(1);
            }
            
            //Load Permissions
            try(BufferedReader pReader = new BufferedReader(new FileReader(permissionsFile))) {
                HashMap<String, Class<? extends Permission>> imports = new HashMap<>();
                String line;
                while((line = pReader.readLine()) != null) {
                    try {
                        if(line.matches("import [a-zA-Z]+[a-zA-Z\\.]+[a-zA-Z]+ as [a-zA-Z]+")) {
                            String[] parts = line.split(" ");
                            imports.put(parts[1], (Class<? extends Permission>)Class.forName(parts[3]));
                        } else if(line.matches("grantna? [a-zA-Z]+[a-zA-Z\\.]+[a-zA-Z]+")) {
                            String clazzName = line.split(" ")[1];
                            Class<? extends Permission> clazz = imports.containsKey(clazzName) ? imports.get(clazzName) : (Class<? extends Permission>)Class.forName(clazzName);
                            Permission perm;
                            String permName = pReader.readLine();
                            if(line.startsWith("grantna")) {
                                String permActions = pReader.readLine();
                                perm = clazz.getConstructor(String.class, String.class).newInstance(permName, permActions);
                            } else {
                                perm = clazz.getConstructor(String.class).newInstance(permName);
                            }
                            permissions.add(perm);
                        }
                    } catch(Exception e) {}
                }
                permissions.setReadOnly();
                Policy.setPolicy(new CCEPolicy(permissions));
            } catch(IOException e) {
                System.out.println("Error");
                logError(e);
                System.exit(1);
            }
            
            //Load Jar
            String mainClass = null, mainMethod = null;
            try(BufferedReader eReader = new BufferedReader(new FileReader(entrypointFile))) {
                mainClass = eReader.readLine();
                mainMethod = eReader.readLine();
            } catch(IOException e) {
                System.out.println("Error");
                logError(e);
                System.exit(1);
            }
            ClassLoader cl = null;
            try {
                cl = recursiveImportJar(programLoc, formatFilename(DIR + "/libs"), true);
            } catch(IOException e) {
                System.out.println("Error");
                logError(e);
                System.exit(1);
            }
            
            //Run Tests
            System.out.println("Success");
            try {
                Method entrypoint = Class.forName(mainClass, false, cl).getMethod(mainMethod, argTypes.toArray(Class[]::new));
                System.setSecurityManager(new SecurityManager());
                for(Object[] test: tests) {
                    Object result = entrypoint.invoke(null, test);
                    System.out.println("Success");
                    if(result == null) {
                        System.out.println("0");
                        continue;
                    }
                    if(result.getClass().isArray()) {
                        System.out.println("1" + Arrays.deepToString(test));
                        continue;
                    }
                    System.out.println("1" + result.toString());
                }
                System.out.println("Done");
            } catch(ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                System.out.println("Error");
                System.exit(1);
            }
        //TODO: LOG ERROR
        } catch(Exception e) {
            System.out.println("Error");
            logError(e);
            System.exit(1);
        }
    }
    
    private static ClassLoader recursiveImportJar(String jarLoc, String libsDir, boolean doLoad) throws IOException {
        try (JarFile jar = new JarFile(jarLoc)) {
            Manifest manifest = jar.getManifest();
            if(manifest != null) {
                String classpath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
                if(classpath != null) {
                    String[] libJars = classpath.split(" ");
                    for(int i = 1; i < libJars.length; i++) {
                        String libJar = libJars[i];
                        if(!libs.contains(libJar)) {
                            libs.add(libJar);
                            recursiveImportJar(formatFilename(libsDir + "/" + libJar), libsDir, false);
                        }
                    }
                }
            }
        }
        if(!doLoad) {
            return null;
        }
        URL[] libUrls = libs.stream().map(Main::createJarUrl).toArray(URL[]::new);
        URL[] urls = new URL[libUrls.length+1];
        urls[0] = createJarUrl(jarLoc);
        System.arraycopy(libUrls, 0, urls, 1, libUrls.length);
        URLClassLoader cl = new URLClassLoader(urls, Main.class.getClassLoader());
        libs.add(jarLoc);
        for(String jarPath: libs) {
            try(JarFile jar = new JarFile(jarPath)) {
                Enumeration<JarEntry> entries = jar.entries();
                while(entries.hasMoreElements()) {
                    try {
                        JarEntry je = entries.nextElement();
                        if(!je.isDirectory() && je.getName().endsWith(".class")) {
                            //-6 bc of .class
                            String className = je.getName().substring(0, je.getName().length()-6);
                            className = className.replace('/', '.');
                            cl.loadClass(className);
                        }
                    } catch(Exception e) {}
                }
            } catch(Exception e) {}
        }
        return cl;
    }
    
    private static URL createJarUrl(String jarLoc) {
        try {
            return new URL("jar:file:" + jarLoc + "!/");
        } catch(MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String formatFilename(String filename) {
        return filename.replace("/", File.separator);
    }

    private static void logError(Exception e) {e.printStackTrace();
        try {
            File errorFile;
            int lognum = 0;
            do {
                errorFile = new File(formatFilename(DIR + "error_" + lognum + ".log"));
                lognum++;
            } while(errorFile.exists());
            errorFile.createNewFile();
            try(PrintStream ps = new PrintStream(errorFile)) {
                e.printStackTrace(ps);
            }
        } catch(Exception exc) {}
    }
    
    private static class CCEPolicy extends Policy {

        private final Permissions permissions;

        public CCEPolicy(Permissions permissions) {
            this.permissions = new Permissions();
            permissions.elementsAsStream().forEach((permission) -> this.permissions.add(permission));
            this.permissions.setReadOnly();
        }
        
        @Override
        public PermissionCollection getPermissions(CodeSource codesource) {
            return permissions;
        }

        @Override
        public PermissionCollection getPermissions(ProtectionDomain domain) {
            return getPermissions(domain.getCodeSource());
        }

        @Override
        public boolean implies(ProtectionDomain domain, Permission permission) {
            return getPermissions(domain).implies(permission);
        }
        
        
    }
    
}
