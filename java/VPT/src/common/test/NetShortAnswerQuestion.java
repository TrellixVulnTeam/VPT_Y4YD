package common.test;

/**
 * Represents a {@link ShortAnswerQuestion} which is stripped of all correct answer information
 */
public class NetShortAnswerQuestion extends NetQuestion {
    
    private static final long serialVersionUID = -6211743374941526405L;

    /**
     * Creates a new NetShortAnswerQuestion
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     */
    public NetShortAnswerQuestion(String question, int associatedPoints) {
        super(question, associatedPoints, QuestionType.SHORT_ANSWER);
    }
    
}