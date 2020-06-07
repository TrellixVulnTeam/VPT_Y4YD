package common.test;

/**
 * Represents a question which is responded to with a long form answer and graded manually
 */
public class LongAnswerQuestion extends NetQuestion {
    
    private static final long serialVersionUID = 2216411656854561767L;

    /**
     * Creates a new LongAnswerQuestion
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     */
    public LongAnswerQuestion(String question, int associatedPoints) {
        super(question, associatedPoints, QuestionType.LONG_ANSWER);
    }
    
}