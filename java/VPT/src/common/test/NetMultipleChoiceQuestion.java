package common.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Represents a {@link MultipleChoiceQuestion} which is stripped of all correct answer information
 */
public class NetMultipleChoiceQuestion extends NetQuestion {

    private static final long serialVersionUID = 2794200576845738406L;
    
    /**
     * Should the question allow multiple answers even if this question only contains one correct answer
     */
    public final boolean overrideMultipleAnswer;
    /**
     * The possible answers to this question
     */
    private final ArrayList<String> answers;
    
    
    /**
     * Creates a new NetMultipleChoiceQuestion
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     * @param answers the possible answers to the question
     */
    public NetMultipleChoiceQuestion(String question, int associatedPoints, String... answers) {
        this(question, associatedPoints, Arrays.asList(answers));
    }
    
    /**
     * Creates a new NetMultipleChoiceQuestion
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     * @param answers the possible answers to the question
     */
    public NetMultipleChoiceQuestion(String question, int associatedPoints, Collection<String> answers) {
        this(question, associatedPoints, false, answers);
    }
    
    /**
     * Creates a new NetMultipleChoiceQuestion
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     * @param overrideMultipleAnswer should this question allow multiple answers even if this question only contains one correct answer
     * @param answers the possible answers to the question
     */
    public NetMultipleChoiceQuestion(String question, int associatedPoints, boolean overrideMultipleAnswer, String... answers) {
        this(question, associatedPoints, overrideMultipleAnswer, Arrays.asList(answers));
    }
    
    /**
     * Creates a new NetMultipleChoiceQuestion
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     * @param overrideMultipleAnswer should this question allow multiple answers even if this question only contains one correct answer
     * @param answers the possible answers to the question
     */
    public NetMultipleChoiceQuestion(String question, int associatedPoints, boolean overrideMultipleAnswer, Collection<String> answers) {
        super(question, associatedPoints, QuestionType.MULTIPLE_CHOICE);
        this.overrideMultipleAnswer = overrideMultipleAnswer;
        this.answers = new ArrayList(answers);
    }
    
    /**
     * Retrieves a copy of the internal array of answers
     * @return a copy of the internal array of answers
     */
    public ArrayList<MultipleChoiceAnswer> getAnswers() {
        return new ArrayList(answers);
    }
}