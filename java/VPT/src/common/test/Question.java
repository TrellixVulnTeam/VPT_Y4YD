package common.test;

import java.io.Serializable;

public class Question implements Serializable {
    
    private static final long serialVersionUID = -5195578031577152123L;
    
    public final int associatedPoints;
    public final QuestionType questionType;

    public Question(int associatedPoints, QuestionType questionType) {
        this.associatedPoints = associatedPoints;
        this.questionType = questionType;
    }
    
}