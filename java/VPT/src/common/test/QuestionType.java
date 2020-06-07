package common.test;

/**
 * Represents the type of a question
 */
public enum QuestionType {
    
    /**
     * Represents a question composed of many additional questions
     */
    MULTIPART,
    /**
     * Represents a question for which the user can select one (or more) options from a list
     */
    MULTIPLE_CHOICE,
    /**
     * Represents a question which is intended to have a short answer and is optionally graded automatically
     */
    SHORT_ANSWER,
    /**
     * Represents a question which is responded to with a long form answer and graded manually
     */
    LONG_ANSWER;
    
}