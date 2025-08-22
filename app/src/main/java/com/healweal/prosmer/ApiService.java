package com.healweal.prosmer;

import com.healweal.prosmer.network.GeminiRequest;
import com.healweal.prosmer.network.GeminiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("v1beta/models/gemini-pro:generateContent")
    Call<GeminiResponse> generateContent(@Body GeminiRequest request, @Query("key") String apiKey);
}
