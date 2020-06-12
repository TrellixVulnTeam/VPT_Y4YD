
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {
        File cppTemplateFile = new File(args[0]);
        File hTemplateFile = new File(args[1]);
        File componentTemplateFile = new File(args[2]);
        File editorDataFile = new File(args[3]);
        File cppOutputFile = new File(args[2]);
        File hOutputFile = new File(args[3]);
        ArrayList<Line> cppTemplateData = parseTemplate(cppTemplateFile);
        ArrayList<Line> hTemplateData = parseTemplate(hTemplateFile);
        HashMap<String, ArrayList<String>> componentData = new HashMap<>();
        String projectName;
        Pattern htcbPattern = Pattern.compile("#+\\[");
        try(BufferedReader br = new BufferedReader(new FileReader(componentTemplateFile))) {
            ArrayList<String> objectData = new ArrayList<>();
            projectName = br.readLine();
            String object = "";
            String line;
            while((line = br.readLine()) != null) {
                if(object.isBlank()) {
                    if(line.endsWith("[") && line.length() > 1) {
                        object = line.substring(0, line.length()-1);
                    }
                    continue;
                }
                if(htcbPattern.matcher(line).matches()) {
                    objectData.add(line.substring(0, line.length()-1));
                } else if(line.equals("]")) {
                    componentData.put(object, new ArrayList<>(objectData));
                    objectData.clear();
                    object = "";
                } else {
                    objectData.add(line);
                }
            }
        }
        ArrayList<String> editorData = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(editorDataFile))) {
            String line;
            while((line = br.readLine()) != null) {
                editorData.add(line);
            }
        }
        createOutputFile(hOutputFile, hTemplateData, componentData, editorData);
        createOutputFile(cppOutputFile, cppTemplateData, componentData, editorData);
    }
    
    public static ArrayList<Line> parseTemplate(File templateFile) throws IOException {
        ArrayList<Line> out = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(templateFile))) {
            String preLine = "";
            String postLine = "";
            boolean isDynamic = false;
            String line;
            while((line = br.readLine()) != null) {
                boolean isEscape = false;
                for(char c: line.toCharArray()) {
                    if(isEscape) {
                        isEscape = false;
                        if(isDynamic) {
                            postLine += c;
                        } else {
                            if(c == '$') {
                                isDynamic = true;
                            } else {
                                preLine += c;
                            }
                        }
                        continue;
                    }
                    if(c == '\\') {
                        isEscape = true;
                    } else {
                        if(isDynamic) {
                            postLine += c;
                        } else {
                            preLine += c;
                        }
                    }
                }
            }
            out.add(new Line(preLine, postLine, isDynamic));
        }
        return out;
    }
    
    public static void createOutputFile(File outputFile, ArrayList<Line> templateData,
            HashMap<String, ArrayList<String>> componentData, ArrayList<String> editorData) throws IOException {
        ArrayList<String> outputLines = new ArrayList<>();
        templateData.forEach((line) -> {
            if(line.isDynamic) {
                outputLines.addAll(parseDynamic(line, componentData, editorData));
            } else {
                outputLines.add(line.preLine);
            }
        });
        outputFile.createNewFile();
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            Iterator<String> lines = outputLines.iterator();
            while(lines.hasNext()) {
                bw.append(lines.next());
                if(lines.hasNext()) {
                    bw.newLine();
                }
            }
        }
    }
    
    public static ArrayList<String> parseDynamic(Line line, HashMap<String, ArrayList<String>> componentData, ArrayList<String> editorData) {
        ArrayList<String> out = new ArrayList<>();
        editorData.forEach((component) -> {
            String[] arguments = escapeSplit(component, ',');
            ArrayList<String> componentLines = componentData.get(arguments[0]);
            boolean ignoreNext = false;
            for(String compLine: componentLines) {
                String outLine = line.preLine;
                String[] lineComponents = escapeSplit(compLine, '$');
                for(String comp: lineComponents) {
                    if(comp.length() == 0) {
                        if(!ignoreNext) {
                            outLine += "$";
                            ignoreNext = true;
                        }
                    }
                    if(ignoreNext) {
                        outLine += comp;
                        continue;
                    }
                    StringInt lineParts = extractInt(comp);
                    if(lineParts.i == -1) {
                        outLine += lineParts.string;
                        continue;
                    }
                    String arg;
                    if(arguments.length <= lineParts.i) {
                        arg = "null";
                    } else {
                        arg = arguments[lineParts.i];
                    }
                    outLine += arg;
                    outLine += lineParts.string;
                }
                outLine += line.postLine;
                out.add(outLine);
            }
        });
        return out;
    }
    
    public static String[] escapeSplit(String str, char escapeChar) {
        int elements = 1;
        boolean isEscape = false;
        for (char c : str.toCharArray()) {
            if (isEscape) {
                isEscape = false;
                if(c == escapeChar) {
                    elements--;
                }
                continue;
            }
            if (c == escapeChar) {
                isEscape = true;
                elements++;
            }
        }
        isEscape = false;
        String[] out = new String[elements];
        int idx = 0;
        String temp = "";
        for(char c : str.toCharArray()) {
            if (isEscape) {
                isEscape = false;
                if(c == escapeChar) {
                    temp += escapeChar;
                } else {
                    out[idx] = temp;
                    temp = "" + c;
                    idx++;
                }
                continue;
            }
            if (c == '\\') {
                isEscape = true;
            } else {
                temp += c;
            }
        }
        out[idx] = temp;
        return out;
    }
    
    public static StringInt extractInt(String str) {
        String iP = "";
        String rest = "";
        int idx = 0;
        for(char c: str.toCharArray()) {
            boolean cancel = false;
            switch(c) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    idx++;
                    break;
                default:
                    cancel = true;
                    break;
            }
            if(cancel) {
                break;
            }
        }
        iP = str.substring(0, idx);
        if(iP.isEmpty()) {
            iP = "-1";
        }
        rest = str.substring(idx);
        return new StringInt(rest, Integer.parseInt(iP));
    }
    
    public static class Line {
        
        public final String preLine;
        public final String postLine;
        public final boolean isDynamic;

        public Line(String preLine, String postLine, boolean isDynamic) {
            this.preLine = preLine;
            this.postLine = postLine;
            this.isDynamic = isDynamic;
        }
        
    }
    
    public static class StringInt {
        
        public final String string;
        public final int i;

        public StringInt(String string, int i) {
            this.string = string;
            this.i = i;
        }
        
    }
    
}
