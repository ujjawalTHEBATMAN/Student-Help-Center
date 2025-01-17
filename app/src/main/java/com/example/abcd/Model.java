package com.example.abcd;

public class Model {
    private String name;
    private String apiUrl;

    public Model(String name, String apiUrl) {
        this.name = name;
        this.apiUrl = apiUrl;
    }

    public String getName() {
        return name;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}
