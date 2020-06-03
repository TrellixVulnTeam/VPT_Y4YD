package common.test;

public class ShortAnswerQuestion extends Question {
    
    public final String question;

    public ShortAnswerQuestion(String question, int associatedPoints) {
        super(associatedPoints, QuestionType.SHORT_ANSWER);
        this.question = question;
    }
    
}