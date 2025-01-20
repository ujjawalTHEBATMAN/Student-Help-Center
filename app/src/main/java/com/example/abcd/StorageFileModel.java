package com.example.abcd;

import com.google.firebase.database.DataSnapshot;
import java.util.Objects;

public class StorageFileModel {
    private String id;
    private String name;
    private String url;
    private long timestamp;

    public StorageFileModel() {}

    public StorageFileModel(DataSnapshot snapshot) {
        id = Objects.requireNonNull(snapshot.child("id").getValue(String.class), "File ID cannot be null");
        name = snapshot.child("name").getValue(String.class);
        url = Objects.requireNonNull(snapshot.child("url").getValue(String.class), "File URL cannot be null");
        timestamp = snapshot.child("timestamp").getValue(Long.class) != null ?
                snapshot.child("timestamp").getValue(Long.class) : 0L;

        if (name == null || name.isEmpty()) {
            name = "Unnamed File";
        }
    }

    public String getName() {
        return (name != null && !name.isEmpty()) ? name : "Unnamed File";
    }

    public String getUrl() {
        return url != null ? url : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = (name != null && !name.isEmpty()) ? name : "Unnamed File";
    }

    public void setUrl(String url) {
        this.url = (url != null && !url.isEmpty()) ? url : "";
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }
}