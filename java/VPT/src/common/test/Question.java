package common.test;

import java.io.Serializable;

/**
 * Represents a question which will appear on a test
 */
public class Question implements Serializable {
    
    private static final long serialVersionUID = -5195578031577152123L;
    
    /**
     * The question which will be prompted to the user
     */
    public final String question;
    /**
     * The number of points associated with this question
     */
    public final int associatedPoints;
    /**
     * The type of question which will be prompted to the user
     */
    public final QuestionType questionType;

    /**
     * Creates a new Question
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     * @param questionType the type of question which will be prompted to the user
     */
    public Question(String question, int associatedPoints, QuestionType questionType) {
        this.question = question == null ? "" : question;
        this.associatedPoints = associatedPoints;
        this.questionType = questionType;
    }
    
}