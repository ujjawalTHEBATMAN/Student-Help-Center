package com.example.abcd.MathFeature;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NewtonApiService {
    @GET("{operation}/{expression}")
    Call<NewtonResponse> solveEquation(
            @Path("operation") String operation,
            @Path("expression") String expression
    );
}
