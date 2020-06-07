package common.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a question for which the user can select one (or more) answers from a list
 */
public class MultipleChoiceQuestion extends Question {

    private static final long serialVersionUID = 6118308379082449650L;
    
    /**
     * Should the question allow multiple answers even if this question only contains one correct answer
     */
    public final boolean overrideMultipleAnswer;
    /**
     * The possible answers to this question
     */
    private final ArrayList<MultipleChoiceAnswer> answers;
    
    /**
     * Creates a new MultipleChoiceQuestion
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     * @param answers the possible answers to the question
     */
    public MultipleChoiceQuestion(String question, int associatedPoints, MultipleChoiceAnswer... answers) {
        this(question, associatedPoints, Arrays.asList(answers));
    }
    
    /**
     * Creates a new MultipleChoiceQuestion
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     * @param answers the possible answers to the question
     */
    public MultipleChoiceQuestion(String question, int associatedPoints, Collection<MultipleChoiceAnswer> answers) {
        this(question, associatedPoints, false, answers);
    }
    
    /**
     * Creates a new MultipleChoiceQuestion
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     * @param overrideMultipleAnswer should this question allow multiple answers even if this question only contains one correct answer
     * @param answers the possible answers to the question
     */
    public MultipleChoiceQuestion(String question, int associatedPoints, boolean overrideMultipleAnswer, MultipleChoiceAnswer... answers) {
        this(question, associatedPoints, overrideMultipleAnswer, Arrays.asList(answers));
    }
    
    /**
     * Creates a new MultipleChoiceQuestion
     * @param question the question which will be prompted to the user
     * @param associatedPoints the number of points associated with this question
     * @param overrideMultipleAnswer should this question allow multiple answers even if this question only contains one correct answer
     * @param answers the possible answers to the question
     */
    public MultipleChoiceQuestion(String question, int associatedPoints, boolean overrideMultipleAnswer, Collection<MultipleChoiceAnswer> answers) {
        super(question, associatedPoints, QuestionType.MULTIPLE_CHOICE);
        this.overrideMultipleAnswer = overrideMultipleAnswer;
        this.answers = new ArrayList<>(answers);
    }
    
    /**
     * A function mapping MultipleChoiceAnswers to their answer prompt components
     */
    public static final Function<MultipleChoiceAnswer, String> answerConverter = answer -> answer.answer;
    /**
     * Converts this MultipleChoiceQuestion to a NetMultipleChoiceQuestion
     * @return a NetMultipleChoiceQuestion mirroring the data contained in this MultipleChoiceQuestion
     */
    public NetMultipleChoiceQuestion toNetMultipleChoiceQuestion() {
        return new NetMultipleChoiceQuestion(question, associatedPoints, overrideMultipleAnswer, answers.stream().map(answerConverter).collect(Collectors.toList()));
    }
    
    /**
     * Retrieves a copy of the internal array of MultipleChoiceAnswers
     * @return a copy of the internal array of MultipleChoiceAnswers
     */
    public ArrayList<MultipleChoiceAnswer> getAnswers() {
        return new ArrayList<>(answers);
    }
}