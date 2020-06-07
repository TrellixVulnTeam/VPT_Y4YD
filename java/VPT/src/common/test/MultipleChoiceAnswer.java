package common.test;

import java.io.Serializable;

/**
 * Represents an answer to a {@link MultipleChoiceQuestion}
 */
public class MultipleChoiceAnswer implements Serializable {
    
    private static final long serialVersionUID = -7083064546445004713L;
    
    /**
     * The answer which will be displayed to the user
     */
    public final String answer;
    /**
     * Whether this answer is correct
     */
    public final boolean isCorrect;

    public MultipleChoiceAnswer(String answer, boolean isCorrect) {
        this.answer = answer;
        this.isCorrect = isCorrect;
    }
    
}