package com.example.abcd.ExamQuizes;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class QuizQuestion implements Parcelable {
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private int correctAnswerIndex;

    // Required empty constructor for Firebase
    public QuizQuestion() {}

    public QuizQuestion(String question, String option1, String option2,
                        String option3, String option4, int correctAnswerIndex) {
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    // Keep existing Parcelable implementation
    protected QuizQuestion(Parcel in) {
        question = in.readString();
        option1 = in.readString();
        option2 = in.readString();
        option3 = in.readString();
        option4 = in.readString();
        correctAnswerIndex = in.readInt();
    }

    public static final Creator<QuizQuestion> CREATOR = new Creator<QuizQuestion>() {
        @Override
        public QuizQuestion createFromParcel(Parcel in) {
            return new QuizQuestion(in);
        }

        @Override
        public QuizQuestion[] newArray(int size) {
            return new QuizQuestion[size];
        }
    };

    // Rest of your existing getters
    public String getQuestion() { return question; }
    public String getOption1() { return option1; }
    public String getOption2() { return option2; }
    public String getOption3() { return option3; }
    public String getOption4() { return option4; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeString(option1);
        dest.writeString(option2);
        dest.writeString(option3);
        dest.writeString(option4);
        dest.writeInt(correctAnswerIndex);
    }
}