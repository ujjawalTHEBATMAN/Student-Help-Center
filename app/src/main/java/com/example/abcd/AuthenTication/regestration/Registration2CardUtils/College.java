package com.example.abcd.AuthenTication.regestration.Registration2CardUtils;

import java.util.Random;

public class College {
    private String id;
    private String name;
    private String image;

    public College() { }

    public College(String id, String name, String ignoredImage) {
        this.id = id;
        this.name = name;
        this.image = "images.png";
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
}
