package common.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a test and all its associated data
 */
public class Test implements Serializable {
    
    private static final long serialVersionUID = -4989225315930207547L;
    
    /**
     * The name of this test
     */
    public final String name;
    /**
     * The Language this test is written for
     */
    public final Language language;
    /**
     * The difficulty of this test
     */
    public final TestDifficulty difficulty;
    /**
     * The questions contained in this test
     */
    private final ArrayList<Question> questions;
    
    /**
     * Creates a new Test
     * @param name the name of this test
     * @param language the Language this test is written for
     * @param difficulty the difficulty of this test
     * @param questions the questions contained in this test
     */
    public Test(String name, Language language, TestDifficulty difficulty, Collection<Question> questions) {
        this.name = name;
        this.language = language;
        this.difficulty = difficulty;
        this.questions = new ArrayList(questions);
    }
    
    /**
     * A Function converting Questions to their corresponding NetQuestion equivalents
     */
    public static final Function<Question, NetQuestion> questionConverter = question -> {
        if(question == null) {
            return null;
        }
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
        System.err.println("No valid conversion for: " + question.getClass() + " Test.java:questionConverter");
        return null;
    };
    /**
     * Converts this Test to a NetTest
     * @return a NetTest containing the same information as this Test
     */
    public NetTest toNetTest() {
        return new NetTest(name, language, difficulty, getQuestions().stream().map(questionConverter).collect(Collectors.toList()));
    }
    
    /**
     * Retrieves a copy of the internal array of questions
     * @return a copy of the internal array of questions
     */
    public ArrayList<Question> getQuestions() {
        return new ArrayList<>(questions);
    }
    
}