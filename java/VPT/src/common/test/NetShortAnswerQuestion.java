package common.test;

public class NetShortAnswerQuestion extends NetQuestion {
    
    private static final long serialVersionUID = -6211743374941526405L;
    
    public final String question;

    public NetShortAnswerQuestion(String question, int associatedPoints) {
        super(associatedPoints, QuestionType.SHORT_ANSWER);
        this.question = question;
    }
    
}