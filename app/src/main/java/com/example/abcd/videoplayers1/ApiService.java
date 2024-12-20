package com.example.abcd.videoplayers1;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("https://raw.githubusercontent.com/ujjawalTHEBATMAN/jsonFileForCAStoreCourseURL/refs/heads/main/bcavideos")
    Call<VideoData> fetchVideoData();
}