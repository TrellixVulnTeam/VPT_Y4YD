package common.test;

import java.util.ArrayList;
import java.util.Collection;

public class Test {
    
    private static final long serialVersionUID = -4989225315930207547L;
    
    private final String name;
    private final Language language;
    private final TestDifficulty difficulty;
    private final ArrayList<Question> questions;
    
    public Test(String name, Language language, TestDifficulty difficulty, Collection<Question> questions) {
        this.name = name;
        this.language = language;
        this.difficulty = difficulty;
        this.questions = new ArrayList(questions);
    }
    
}