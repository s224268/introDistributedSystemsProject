package org.example.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import com.google.gson.Gson;

public class API {

    private static final String BASE_URL = "https://unofficialurbandictionaryapi.com/api/random?";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static API instance;

    // Private constructor to prevent instantiation
    private API() {}

    // Public method to provide access to the instance
    public static API getInstance() {
        if (instance == null) {
            instance = new API();
        }
        return instance;
    }

    public List<WordDefinition> callUrbanDictionaryAPI(Params params) {
        String url = BASE_URL + params.toQueryString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), ApiResponse.class).getData();
            } else {
                System.err.println("Error: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error when calling API");
            e.printStackTrace();
        }
        return List.of();
    }
}