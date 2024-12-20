package com.example.abcd.videoplayers1;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoViewModel extends AndroidViewModel {
    private final MutableLiveData<List<VideoModel>> videoList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final ApiService apiService;

    public VideoViewModel(@NonNull Application application) {
        super(application);
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<List<VideoModel>> getVideoList() {
        return videoList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void fetchVideoData(String semester) {
        isLoading.setValue(true);
        error.setValue(null);

        Call<VideoData> call = apiService.fetchVideoData();
        call.enqueue(new Callback<VideoData>() {
            @Override
            public void onResponse(@NonNull Call<VideoData> call, @NonNull Response<VideoData> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<VideoModel> videos = null;
                    switch (semester) {
                        case "sem1":
                            videos = response.body().getSem1();
                            break;
                        case "sem2":
                            videos = response.body().getSem2();
                            break;
                        case "sem3":
                            videos = response.body().getSem3();
                            break;
                        case "sem4":
                            videos = response.body().getSem4();
                            break;
                        case "sem5":
                            videos = response.body().getSem5();
                            break;
                        case "sem6":
                            videos = response.body().getSem6();
                            break;
                    }
                    
                    if (videos != null) {
                        videoList.setValue(videos);
                    } else {
                        videoList.setValue(new ArrayList<>());
                        error.setValue("No videos available for " + semester);
                    }
                } else {
                    error.setValue("Failed to load videos. Server returned: " + 
                        response.code());
                    videoList.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VideoData> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                error.setValue("Network error: " + t.getMessage());
                videoList.setValue(new ArrayList<>());
            }
        });
    }
}