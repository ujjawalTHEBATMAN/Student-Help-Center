package com.example.abcd.videoplayers1;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/ujjawalTHEBATMAN/jsonFileForCAStoreCourseURL/refs/heads/main/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }
}