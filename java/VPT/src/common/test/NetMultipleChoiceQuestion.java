package common.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class NetMultipleChoiceQuestion extends NetQuestion {

    private static final long serialVersionUID = 2794200576845738406L;
    
    private final String question;
    private final ArrayList<String> answers;
    
    public NetMultipleChoiceQuestion(String question, int associatedPoints, String... answers) {
        this(question, associatedPoints, Arrays.asList(answers));
    }
    
    public NetMultipleChoiceQuestion(String question, int associatedPoints, Collection<String> answers) {
        super(associatedPoints, QuestionType.MULTIPLE_CHOICE);
        this.question = question;
        this.answers = new ArrayList(answers);
    }
    
}