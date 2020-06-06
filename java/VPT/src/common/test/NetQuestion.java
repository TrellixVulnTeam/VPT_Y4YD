package common.test;

/**
 * Represents a {@link Question} which does not contain any correct answer information
 */
public class NetQuestion extends Question {

    private static final long serialVersionUID = 6376868606442397866L;
    
    /**
     * Creates a new NetQuestion
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     * @param questionType the type of question which will be prompted to the user
     */
    public NetQuestion(String question, int associatedPoints, QuestionType questionType) {
        super(question, associatedPoints, questionType);
    }
    
}