package common.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a {@link Test} which does not contain any correct answer information
 */
public class NetTest implements Serializable {
    
    private static final long serialVersionUID = -3630264916910226377L;
    
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
    private final ArrayList<NetQuestion> questions;
    
    /**
     * Creates a new NetTest
     * @param name the name of this test
     * @param language the Language this test is written for
     * @param difficulty the difficulty of this test
     * @param questions the questions contained in this test
     */
    public NetTest(String name, Language language, TestDifficulty difficulty, Collection<NetQuestion> questions) {
        this.name = name;
        this.language = language;
        this.difficulty = difficulty;
        this.questions = new ArrayList(questions);
    }
    
    /**
     * Retrieves a copy of the internal array of questions
     * @return a copy of the internal array of questions
     */
    public ArrayList<Question> getQuestions() {
        return new ArrayList<>(questions);
    }
}