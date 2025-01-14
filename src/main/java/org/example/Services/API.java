package org.example.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import com.google.gson.Gson;

public class API {

    private static final String API_URL = "https://unofficialurbandictionaryapi.com/api/random?strict=false&matchCase=false&limit=3&page=1&multiPage=false&";

    public List<WordDefinition> callUrbanDictionaryAPI() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            } else {
                System.err.println("Error: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    private List<WordDefinition> parseResponse(String responseBody) {
        Gson gson = new Gson();
        ApiResponse apiResponse = gson.fromJson(responseBody, ApiResponse.class);
        return apiResponse.getData();
    }
}