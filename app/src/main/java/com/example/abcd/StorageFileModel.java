package com.example.abcd;

public class StorageFileModel {
    private String id;
    private String name;
    private String url;
    private long timestamp;

    // Required empty constructor for Firebase
    public StorageFileModel() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}