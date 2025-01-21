package org.example.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import com.google.gson.Gson;
import okhttp3.*;

public class API {

    private static final String BASE_URL = "https://unofficialurbandictionaryapi.com/api/random?";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static API instance;

    private API() {}

    // Public method to provide access to the instance
    public static API getInstance() {
        if (instance == null) {
            instance = new API();
        }
        return instance;
    }

    public void testInternetConnection() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.google.com")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Internet is accessible: " + response.body().string());
            } else {
                System.err.println("Failed to access the internet: " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<WordDefinition> callUrbanDictionaryAPI(Params params) {

        testInternetConnection();

        String url = BASE_URL + params.toQueryString();

        // Build the HTTP request
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                ApiResponse apiResponse = gson.fromJson(responseBody, ApiResponse.class);
                return apiResponse.getData();
            } else {
                System.err.println("Error: " + (response.body() != null ? response.body().string() : "No response body"));
            }
        } catch (IOException e) {
            System.out.println("Error when calling API");
            e.printStackTrace();
        }
        return List.of();
    }
}