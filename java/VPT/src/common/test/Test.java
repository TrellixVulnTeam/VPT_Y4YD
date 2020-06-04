package common.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Test implements Serializable {
    
    private static final long serialVersionUID = -4989225315930207547L;
    
    public final String name;
    public final Language language;
    public final TestDifficulty difficulty;
    private final ArrayList<Question> questions;
    
    public Test(String name, Language language, TestDifficulty difficulty, Collection<Question> questions) {
        this.name = name;
        this.language = language;
        this.difficulty = difficulty;
        this.questions = new ArrayList(questions);
    }
    
    public static final Function<Question, NetQuestion> questionConverter = question -> {
        if(question instanceof NetQuestion) {
            return (NetQuestion)question;
        }
        if(question instanceof MultipleChoiceQuestion) {
            return ((MultipleChoiceQuestion)question).toNetMultipleChoiceQuestion();
        }
        if(question instanceof ShortAnswerQuestion) {
            return ((ShortAnswerQuestion)question).toNetShortAnswerQuestion();
        }
        //This should never be reached
        return null;
    };
    public NetTest toNetTest() {
        return new NetTest(name, language, difficulty, getQuestions().stream().map(questionConverter).collect(Collectors.toList()));
    }
    
    public ArrayList<Question> getQuestions() {
        return new ArrayList<>(questions);
    }
    
}