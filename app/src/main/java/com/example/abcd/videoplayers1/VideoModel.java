package com.example.abcd.videoplayers1;

public class VideoModel {
    private String id;
    private String title;
    private String videoUrl;
    private String thumbnailUrl;
    private String duration;
    private int watchCount;

    public VideoModel(String id, String title, String videoUrl, String thumbnailUrl, String duration, int watchCount) {
        this.id = id;
        this.title = title;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.duration = duration;
        this.watchCount = watchCount;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getDuration() {
        return duration;
    }

    public int getWatchCount() {
        return watchCount;
    }
}
