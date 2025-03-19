package com.example.abcd.ExamQuizes;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.Map;

public class Question implements Parcelable {
    private String text;
    private Map<String, String> options;
    private int correctAnswerIndex;

    public Question() {}

    public Question(String text, Map<String, String> options, int correctAnswerIndex) {
        this.text = text;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    // Getters and setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Map<String, String> getOptions() { return options; }
    public void setOptions(Map<String, String> options) { this.options = options; }

    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public void setCorrectAnswerIndex(int correctAnswerIndex) { this.correctAnswerIndex = correctAnswerIndex; }

    // Parcelable implementation
    protected Question(Parcel in) {
        text = in.readString();
        correctAnswerIndex = in.readInt();
        // Read the map (keys and values are Strings)
        options = new HashMap<>();
        in.readMap(options, String.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeInt(correctAnswerIndex);
        dest.writeMap(options);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };
}
