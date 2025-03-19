package com.example.abcd.ExamQuizes;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Quiz implements Parcelable {
    private String quizId;
    private String subject;
    private long startingTime;
    private int durationMinutes;
    private float marksPerQuestion;
    private float negativeMarking;
    private ArrayList<Question> questions = new ArrayList<>(); // Change from List<Question> to List<QuizQuestion>

    private String resultViewType;
    private long endingTime;

    // Empty constructor required for Firebase
    public Quiz() {questions = new ArrayList<>();}

    public Quiz(String subject, long startingTime, int durationMinutes,
                float marksPerQuestion, float negativeMarking,
                ArrayList<Question> questions, String resultViewType) {
        this.subject = subject;
        this.startingTime = startingTime;
        this.durationMinutes = durationMinutes;
        this.marksPerQuestion = marksPerQuestion;
        this.negativeMarking = negativeMarking;
        this.questions = questions;
        this.resultViewType = resultViewType;
        this.endingTime = startingTime + (durationMinutes * 60 * 1000L);
    }

    protected Quiz(Parcel in) {
        quizId = in.readString();
        subject = in.readString();
        startingTime = in.readLong();
        durationMinutes = in.readInt();
        marksPerQuestion = in.readFloat();
        negativeMarking = in.readFloat();
        questions = in.createTypedArrayList(Question.CREATOR);
        resultViewType = in.readString();
        endingTime = in.readLong();
    }

    public static final Creator<Quiz> CREATOR = new Creator<Quiz>() {
        @Override
        public Quiz createFromParcel(Parcel in) {
            return new Quiz(in);
        }

        @Override
        public Quiz[] newArray(int size) {
            return new Quiz[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(quizId);
        dest.writeString(subject);
        dest.writeLong(startingTime);
        dest.writeInt(durationMinutes);
        dest.writeFloat(marksPerQuestion);
        dest.writeFloat(negativeMarking);
        dest.writeTypedList(questions);
        dest.writeString(resultViewType);
        dest.writeLong(endingTime);
    }

    // Updated toMap method with an additional JSON output for questions
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("quizId", quizId);
        result.put("subject", subject);
        result.put("startingTime", startingTime);
        result.put("durationMinutes", durationMinutes);
        result.put("marksPerQuestion", marksPerQuestion);
        result.put("negativeMarking", negativeMarking);
        result.put("resultViewType", resultViewType);
        result.put("endingTime", endingTime);
        result.put("questions", questions);

        // Extra JSON representation for questions
        List<Map<String, Object>> questionsJson = new ArrayList<>();
        for (Question q : questions) {
            Map<String, Object> qMap = new HashMap<>();
            qMap.put("text", q.getText());
            qMap.put("options", q.getOptions());
            qMap.put("correctAnswerIndex", q.getCorrectAnswerIndex());
            questionsJson.add(qMap);
        }
        result.put("questionsJson", questionsJson);
        return result;
    }

    // Getters and setters
    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public long getStartingTime() { return startingTime; }
    public void setStartingTime(long startingTime) { this.startingTime = startingTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public float getMarksPerQuestion() { return marksPerQuestion; }
    public void setMarksPerQuestion(float marksPerQuestion) { this.marksPerQuestion = marksPerQuestion; }
    public float getNegativeMarking() { return negativeMarking; }
    public void setNegativeMarking(float negativeMarking) { this.negativeMarking = negativeMarking; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(ArrayList<Question> questions) { this.questions = questions; }
    public String getResultViewType() { return resultViewType; }
    public void setResultViewType(String resultViewType) { this.resultViewType = resultViewType; }
    public long getEndingTime() { return endingTime; }
    public void setEndingTime(long endingTime) { this.endingTime = endingTime; }
}
