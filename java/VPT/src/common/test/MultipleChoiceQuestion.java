package common.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class MultipleChoiceQuestion extends Question {

    private final String question;
    private final ArrayList<MultipleChoiceAnswer> answers;
    
    public MultipleChoiceQuestion(String question, int associatedPoints, MultipleChoiceAnswer... answers) {
        this(question, associatedPoints, Arrays.asList(answers));
    }
    
    public MultipleChoiceQuestion(String question, int associatedPoints, Collection<MultipleChoiceAnswer> answers) {
        super(associatedPoints, QuestionType.MULTIPLE_CHOICE);
        this.question = question;
        this.answers = new ArrayList(answers);
    }
    
}