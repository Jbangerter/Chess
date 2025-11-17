package serverfacade;


import com.google.gson.Gson;
import exceptions.ErrorResponse;
import exceptions.HttpResponseException;
import model.*;

import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData registerUser(UserData user) throws HttpResponseException {
        var request = buildRequest("POST", "/user", user, null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public AuthData loginUser(UserData user) throws HttpResponseException {
        var request = buildRequest("POST", "/session", user, null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }


    public void logoutUser(String authToken) throws HttpResponseException {
        var request = buildRequest("DELETE", "/session", null, authToken);
        var response = sendRequest(request);
        handleResponse(response, null); // Expects an empty body
    }


    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));

        if (body != null) {
            builder.setHeader("Content-Type", "application/json");
        }

        if (authToken != null && !authToken.isEmpty()) {
            builder.setHeader("Authorization", authToken);
        }

        return builder.build();
    }


    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws HttpResponseException {
        HttpResponse<String> response;

        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException ex) {

            throw new HttpResponseException(999, ex.getMessage());
        }

        int statusCode = response.statusCode();
        if (statusCode >= 400) {
            String errorMessage = "Error: " + statusCode;
            if (response.body() != null && !response.body().isEmpty()) {
                try {
                    Gson gson = new Gson();
                    ErrorResponse error = gson.fromJson(response.body(), ErrorResponse.class);
                    errorMessage = error.message();
                } catch (Exception e) {
                    errorMessage = response.body();
                }
            }

            throw new HttpResponseException(statusCode, errorMessage);
        }

        return response;
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws HttpResponseException {
        var status = response.statusCode();

        if (!((int) status / 100 == 2)) {
            var body = response.body();
            if (body != null && !body.isEmpty()) {
                throw new HttpResponseException(status, body);
            }
            throw new HttpResponseException(status, "Unknown Exception: " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }


}
