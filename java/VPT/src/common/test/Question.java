package common.test;

public class Question {
    
    public final int associatedPoints;
    public final QuestionType questionType;

    public Question(int associatedPoints, QuestionType questionType) {
        this.associatedPoints = associatedPoints;
        this.questionType = questionType;
    }
    
}