package common.test;

public class LongAnswerQuestion extends Question {
    
    public final String question;

    public LongAnswerQuestion(String question, int associatedPoints) {
        super(associatedPoints, QuestionType.LONG_ANSWER);
        this.question = question;
    }
    
}