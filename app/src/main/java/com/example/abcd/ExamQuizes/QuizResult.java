package com.example.abcd.ExamQuizes;

import android.os.Parcel;
import android.os.Parcelable;

public class QuizResult implements Parcelable {
    private String userEmail;
    private String subject;
    private float obtainedMarks;
    private float totalMarks;
    private long timestamp;

    public QuizResult() {}

    public QuizResult(String userEmail, String subject, float obtainedMarks, float totalMarks, long timestamp) {
        this.userEmail = userEmail;
        this.subject = subject;
        this.obtainedMarks = obtainedMarks;
        this.totalMarks = totalMarks;
        this.timestamp = timestamp;
    }

    protected QuizResult(Parcel in) {
        userEmail = in.readString();
        subject = in.readString();
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

    public String getUserEmail() { return userEmail; }
    public String getSubject() { return subject; }
    public float getObtainedMarks() { return obtainedMarks; }
    public float getTotalMarks() { return totalMarks; }
    public long getTimestamp() { return timestamp; }

    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setObtainedMarks(float obtainedMarks) { this.obtainedMarks = obtainedMarks; }
    public void setTotalMarks(float totalMarks) { this.totalMarks = totalMarks; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userEmail);
        dest.writeString(subject);
        dest.writeFloat(obtainedMarks);
        dest.writeFloat(totalMarks);
        dest.writeLong(timestamp);
    }
}