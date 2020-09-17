package common.test;

/**
 * Represents a question which is intended to have a short answer and is optionally graded automatically
 */
public class ShortAnswerQuestion extends Question {
    
    private static final long serialVersionUID = -8825764437962294476L;
    
    /**
     * The correct answer to this question or <code>null</code> if it should be graded manually
     */
    public final String answer;

    /**
     * Creates a new ShortAnswerQuestion
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     * @param answer the correct answer to this question or <code>null</code> if it should be graded manually
     */
    public ShortAnswerQuestion(String question, int associatedPoints, String answer) {
        super(question, associatedPoints, QuestionType.SHORT_ANSWER);
        this.answer = answer;
    }
    
    /**
     * @return A NetShortAnswerQuestion representing the information from this ShortAnswerQuestion
     */
    public NetShortAnswerQuestion toNetShortAnswerQuestion() {
        return new NetShortAnswerQuestion(question, associatedPoints);
    }
    
}