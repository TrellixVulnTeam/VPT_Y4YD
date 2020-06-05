package common.test;

import java.io.Serializable;

public class MultipleChoiceAnswer implements Serializable {
    
    private static final long serialVersionUID = -7083064546445004713L;
    
    public final String answer;
    public final boolean isCorrect;

    public MultipleChoiceAnswer(String answer, boolean isCorrect) {
        this.answer = answer;
        this.isCorrect = isCorrect;
    }
    
}