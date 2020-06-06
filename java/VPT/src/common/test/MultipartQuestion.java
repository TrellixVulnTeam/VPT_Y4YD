package common.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Represents a question made up of multiple sub-questions
 */
public class MultipartQuestion extends NetQuestion {
    
    private static final long serialVersionUID = 4335037840892290983L;
    
    /**
     * An ArrayList of the sub-questions making up this multipart question
     */
    private ArrayList<Question> subQuestions;

    /**
     * Creates a new MultipartQuestion
     * @param question the question (if any) which will be prompted to the user
     * @param subQuestions the sub-questions which are part of this MultipartQuestion
     */
    public MultipartQuestion(String question, Question... subQuestions) {
        this(question, Arrays.asList(subQuestions));
    }
    
    /**
     * Creates a new MultipartQuestion
     * @param question the question (if any) which will be prompted to the user
     * @param subQuestions the sub-questions which are part of this MultipartQuestion
     */
    public MultipartQuestion(String question, Collection<Question> subQuestions) {
        super(question, 0, QuestionType.MULTIPART);
        this.subQuestions = new ArrayList<>(subQuestions);
    }
    
    /**
     * Retrieves a copy of the internal array of sub-questions
     * @return a copy of the internal array of sub-questions
     */
    public ArrayList<Question> getSubQuestions() {
        return new ArrayList<>(subQuestions);
    }
    
}