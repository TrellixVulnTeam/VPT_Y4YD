package common.test;

public class LongAnswerQuestion extends NetQuestion {
    
    private static final long serialVersionUID = 2216411656854561767L;
    
    public final String question;

    public LongAnswerQuestion(String question, int associatedPoints) {
        super(associatedPoints, QuestionType.LONG_ANSWER);
        this.question = question;
    }
    
}