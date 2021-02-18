package advisor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class Auth {

    static void authorize() throws IOException, InterruptedException {
        System.out.println(Config.AUTH_SERVER_PATH + "/authorize?" +
                "client_id=" + Config.CLIENT_ID + "&redirect_uri=" + Config.REDIRECT_URI + "&response_type=code");
        System.out.println("waiting for code...");
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8080), 0);
        server.start();
        server.createContext("/",
                exchange -> {
                    String str;
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null) {
                        if (query.startsWith("code")) {
                            str = "Got the code. Return back to your program.";
                            Config.AUTH_CODE = query;
                        } else {
                            str = "Authorization code not found. Try again.";
                        }
                    } else {
                        str = "Authorization code not found. Try again.";
                    }

                    exchange.sendResponseHeaders(200, str.length());
                    exchange.getResponseBody().write(str.getBytes());
                    exchange.getResponseBody().close();
                }
        );
        while (Config.AUTH_CODE.isBlank()) {
            Thread.sleep(10);
        }
        server.stop(10);
        getToken();

    }

    static void getToken() throws IOException, InterruptedException {
        System.out.println("code received");
        System.out.println("making http request for access_token...");
        String encodedKey = Base64.getEncoder().encodeToString((Config.CLIENT_ID + ":" + Config.CLIENT_SECRET).getBytes());
        var client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + encodedKey)
                .uri(URI.create(Config.AUTH_SERVER_PATH + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=authorization_code&"
                        + Config.AUTH_CODE +
                        "&redirect_uri=" + Config.REDIRECT_URI))
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        JsonObject responseJson = JsonParser.parseString(String.valueOf(response != null ? response.body() : null)).getAsJsonObject();
        try {
            Config.ACCESS_TOKEN = responseJson.get("access_token").getAsString();
            System.out.println("---SUCCESS---");
        } catch (Exception e) {
            System.out.println("Failed, please re-authorize.");
            Controller.initialInput();
            return;
        }

        Controller.takeInput(null);

    }
}
