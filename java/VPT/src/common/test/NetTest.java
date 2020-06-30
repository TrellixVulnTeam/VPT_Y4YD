package common.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class NetTest implements Serializable {
    
    private static final long serialVersionUID = -3630264916910226377L;
    
    public final String name;
    public final Language language;
    public final TestDifficulty difficulty;
    private final ArrayList<NetQuestion> questions;
    
    public NetTest(String name, Language language, TestDifficulty difficulty, Collection<NetQuestion> questions) {
        this.name = name;
        this.language = language;
        this.difficulty = difficulty;
        this.questions = new ArrayList<>(questions);
    }
    
}