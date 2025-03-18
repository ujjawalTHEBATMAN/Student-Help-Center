package com.example.abcd.ExamQuizes;

import android.os.Parcel;
import android.os.Parcelable;

public class QuizResult implements Parcelable {
    private String userEmail;
    private float obtainedMarks;
    private float totalMarks;
    private long timestamp;

    // Required no-argument constructor for Firebase
    public QuizResult() {}

    public QuizResult(String userEmail, float obtainedMarks, float totalMarks, long timestamp) {
        this.userEmail = userEmail;
        this.obtainedMarks = obtainedMarks;
        this.totalMarks = totalMarks;
        this.timestamp = timestamp;
    }

    protected QuizResult(Parcel in) {
        userEmail = in.readString();
        obtainedMarks = in.readFloat();
        totalMarks = in.readFloat();
        timestamp = in.readLong();
    }

    public static final Creator<QuizResult> CREATOR = new Creator<QuizResult>() {
        @Override
        public QuizResult createFromParcel(Parcel in) {
            return new QuizResult(in);
        }

        @Override
        public QuizResult[] newArray(int size) {
            return new QuizResult[size];
        }
    };

    // Getters and setters
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public float getObtainedMarks() { return obtainedMarks; }
    public void setObtainedMarks(float obtainedMarks) { this.obtainedMarks = obtainedMarks; }

    public float getTotalMarks() { return totalMarks; }
    public void setTotalMarks(float totalMarks) { this.totalMarks = totalMarks; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userEmail);
        dest.writeFloat(obtainedMarks);
        dest.writeFloat(totalMarks);
        dest.writeLong(timestamp);
    }
}