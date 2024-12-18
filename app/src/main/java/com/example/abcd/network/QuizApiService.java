package com.example.abcd.network;

import com.example.abcd.models.QuizResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface QuizApiService {
    @GET("quiz")
    Call<QuizResponse> getQuizQuestions();
}
