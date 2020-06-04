package common.test;

public class ShortAnswerQuestion extends Question {
    
    private static final long serialVersionUID = -8825764437962294476L;
    
    public final String question;
    public final String answer;

    public ShortAnswerQuestion(String question, String answer, int associatedPoints) {
        super(associatedPoints, QuestionType.SHORT_ANSWER);
        this.question = question;
        this.answer = answer;
    }
    
    public NetShortAnswerQuestion toNetShortAnswerQuestion() {
        return new NetShortAnswerQuestion(question, associatedPoints);
    }
    
}