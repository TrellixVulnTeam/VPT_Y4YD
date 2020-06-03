package common.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class MultipartQuestion extends Question {
    
    private ArrayList<Question> subQuestions;

    public MultipartQuestion(Question... subQuestions) {
        this(Arrays.asList(subQuestions));
    }
    
    public MultipartQuestion(Collection<Question> subQuestions) {
        super(0, QuestionType.MULTIPART);
        this.subQuestions = new ArrayList<>(subQuestions);
    }
    
}