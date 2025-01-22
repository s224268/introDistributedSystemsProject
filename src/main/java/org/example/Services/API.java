package org.example.Services;

import java.util.List;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class API {

    private static final String TEST_URL = "https://api.ipify.org";
    private static final String BASE_URL = "https://unofficialurbandictionaryapi.com/api/random?";
    private static final Gson gson = new Gson();
    private static API instance;

    private API() {}

    public static API getInstance() {
        if (instance == null) {
            instance = new API();
        }
        return instance;
    }

    public void testInternetConnection() {
        try {
            URL url = new URL(TEST_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = in.readLine();
                System.out.println("Internet is accessible: " + response);
                in.close();
            } else {
                System.err.println("Failed to access the internet. Response code: " + responseCode);
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<WordDefinition> callUrbanDictionaryAPI(Params params) {
        testInternetConnection();

        String urlString = BASE_URL + params.toQueryString();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                ApiResponse apiResponse = gson.fromJson(response.toString(), ApiResponse.class);
                return apiResponse.getData();
            } else {
                System.err.println("Error: Received HTTP code " + responseCode);
            }
            connection.disconnect();
        } catch (Exception e) {
            System.err.println("Error when calling API");
            e.printStackTrace();
        }
        return List.of();
    }
}