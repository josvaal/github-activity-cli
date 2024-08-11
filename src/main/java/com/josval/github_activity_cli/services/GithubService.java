package com.josval.github_activity_cli.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class GithubService {
    public static final String API_URL = "https://api.github.com/users/";

    public List<Map<String, Object>> ListEvents(String username) {
        try {
            URL url = new URL(API_URL.concat(username).concat("/events"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                return gson.fromJson(response.toString(), listType);
            } else {
                System.out.println("API Call Failed. Response Code: " + responseCode);
            }
        }catch (Exception e){
            throw new RuntimeException("Error processing request", e);
        }
        return null;
    }
}
