import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * A class used for building the main project and compiling certain enums to c++ header files defining macros
 */
public class Main {
    
    public static void main(String[] args) throws Exception {
        String srcDir = args[0];
        String enumFile = args[1];
        String headerDir = args[2];
        try(BufferedReader br = new BufferedReader(new FileReader(enumFile))) {
            String enumLine;
            while((enumLine = br.readLine()) != null) {
                String[] enumLineParts = enumLine.split(" ");
                String srcPath = enumLineParts[0];
                String destPath = enumLineParts[1];
                String destName = enumLineParts[2];
                File outputFile = new File(headerDir + destPath + ".h");
                outputFile.createNewFile();
                try(BufferedReader fr = new BufferedReader(new FileReader(srcDir + srcPath + ".java"));
                        BufferedWriter fw = new BufferedWriter(new FileWriter(outputFile))) {
                    fw.append("#ifndef " + destName);
                    fw.newLine();
                    fw.append("#define " + destName);
                    fw.newLine();
                    String line;
                    while((line = fr.readLine()) != null) {
                        if(line.matches(".*\\(-?[0-9]+\\)[,;]")) {
                            String[] parts = line.trim().split("\\(");
                            String name = parts[0];
                            String idString = parts[1];
                            int id = Integer.parseInt(idString.substring(0, idString.length()-2));
                            fw.append("#define " + destName + "_" + name + " " + id);
                            fw.newLine();
                        }
                    }
                    fw.append("#endif");
                }
            }
        }
    }
}