package common.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MultipleChoiceQuestion extends Question {

    private static final long serialVersionUID = 6118308379082449650L;
    
    private final String question;
    private final ArrayList<MultipleChoiceAnswer> answers;
    
    public MultipleChoiceQuestion(String question, int associatedPoints, MultipleChoiceAnswer... answers) {
        this(question, associatedPoints, Arrays.asList(answers));
    }
    
    public MultipleChoiceQuestion(String question, int associatedPoints, Collection<MultipleChoiceAnswer> answers) {
        super(associatedPoints, QuestionType.MULTIPLE_CHOICE);
        this.question = question;
        this.answers = new ArrayList<>(answers);
    }
    
    public static final Function<MultipleChoiceAnswer, String> answerConverter = answer -> answer.answer;
    public NetMultipleChoiceQuestion toNetMultipleChoiceQuestion() {
        return new NetMultipleChoiceQuestion(question, associatedPoints, answers.stream().map(answerConverter).collect(Collectors.toList()));
    }
    
}