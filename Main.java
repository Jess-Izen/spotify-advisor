package advisor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.URI;
import java.util.Base64;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Main {


    public static void main(String[] args) throws IOException, InterruptedException {

        //process command line arguments
        if (args.length > 0) {
            for (int i = 0; i < args.length - 1; i++) {
                if (args[i].equals("-access")){
                    if (args[i + 1].length() > 0) {
                        Config.AUTH_SERVER_PATH = args[i + 1];
                    }
                } else if (args[i].equals("-resource")) {
                    if (args[i + 1].length() > 0) {
                        Config.API_SERVER_PATH = args[i + 1];
                    }
                }
            }

        }

        initialInput();
    }

     static void initialInput() throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        switch (input) {
            case "auth":
                authorize();
                break;
            case "exit":
                System.out.println("---GOODBYE!---");
                break;
            default:
                System.out.println("Please, provide access for application.");
                initialInput();
                break;
        }
    }

     static void authorize() throws IOException, InterruptedException {
        System.out.println(Config.AUTH_SERVER_PATH + "/authorize?" +
                "client_id=" + Config.CLIENT_ID + "&redirect_uri=" + Config.REDIRECT_URI + "&response_type=code");
        System.out.println("waiting for code...");
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8080), 0);
        server.start();
        server.createContext("/",
                new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
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
            takeInput();
        } catch (Exception e) {
            System.out.println("Failed, please re-authorize.");
            initialInput();
        }

    }

     static void takeInput() {
        Scanner scanner = new Scanner(System.in);

        //using next instead of nextln so we only get first word of command (re: playlists)
        String input = scanner.next();

        switch (input) {
            case "new":
                newReleases();
                break;
            case "featured":
                featuredReleases();
                break;
            case "categories":
                categories();
                break;
            case "playlists":
                //get category name following playlists command
                String target = scanner.nextLine().trim();
                playlists(target);
                break;
            case "exit":
                System.out.println("---GOODBYE!---");
                break;
            default:
                System.out.println("Input not recognized, please try again");
                takeInput();
                break;
        }

    }


     static void newReleases() {

        JsonObject responseJson = sendApiRequest("/v1/browse/new-releases");

        try {
            JsonObject albums = responseJson.get("albums").getAsJsonObject();
            for (JsonElement album : albums.getAsJsonArray("items")) {
                JsonObject albumObject = album.getAsJsonObject();

                //album name
                System.out.println(albumObject.get("name").getAsString());

                //artists
                JsonArray artists = albumObject.getAsJsonArray("artists");
                int size = artists.size();
                if (size == 1) {
                    System.out.println("[" + artists.get(0).getAsJsonObject().get("name").getAsString() + "]");
                } else if (size > 1) {
                    System.out.print("[");
                    for (int i = 0; i < size; i++) {
                        if (i < (size - 1)) {
                            System.out.print(artists.get(i).getAsJsonObject().get("name").getAsString() + ", ");
                        } else {
                            System.out.println(artists.get(i).getAsJsonObject().get("name").getAsString() + "]");
                        }

                    }
                }

                //link
                System.out.println(albumObject.get("external_urls").getAsJsonObject().get("spotify").getAsString() + "\n");


            }
        } catch (Exception e) {
            System.out.println("Error, please try again");
        }

        takeInput();
    }

      static void featuredReleases() {
        JsonObject responseJson = sendApiRequest("/v1/browse/featured-playlists");

          try {
            JsonObject playlists = responseJson.get("playlists").getAsJsonObject();
            for (JsonElement playlist : playlists.getAsJsonArray("items")) {
                JsonObject playlistObject = playlist.getAsJsonObject();

                //playlist name
                System.out.println(playlistObject.get("name").getAsString());

                //link
                System.out.println(playlistObject.get("external_urls").getAsJsonObject().get("spotify").getAsString() + "\n");


            }
        } catch (Exception e) {
            System.out.println("Error, please try again");
        }

        takeInput();


    }

    static void categories() {

        JsonObject responseJson = sendApiRequest("/v1/browse/categories");

        try {
            JsonObject categories = responseJson.get("categories").getAsJsonObject();
            for (JsonElement category : categories.getAsJsonArray("items")) {
                JsonObject categoryObject = category.getAsJsonObject();

                //category name
                System.out.println(categoryObject.get("name").getAsString());



            }
        } catch (Exception e) {
            System.out.println("Error, please try again");
        }

        takeInput();


    }

    static void playlists(String target ) {
        String categoryId  = findCategoryId(target);
        if (categoryId.equals("")) {
            System.out.println("Unknown category name.");
        } else {
            try {
                System.out.println(categoryId);
                JsonObject responseJson = sendApiRequest("/v1/browse/categories/" + categoryId + "/playlists");
                if (responseJson.get("error") != null) {
                    System.out.println(responseJson.get("error").getAsJsonObject().get("message").getAsString());
                } else {
                    JsonObject playlists = responseJson.get("playlists").getAsJsonObject();
                    for (JsonElement playlist : playlists.getAsJsonArray("items")) {
                        JsonObject playlistObject = playlist.getAsJsonObject();

                        //playlist name
                        System.out.println(playlistObject.get("name").getAsString());

                        //link
                        System.out.println(playlistObject.get("external_urls").getAsJsonObject().get("spotify").getAsString() + "\n");
                    }
                }
            } catch (Exception e) {
                System.out.println("Error, please try again");
            }
        }


        takeInput();

    }


    static String findCategoryId(String target) {
        String path = "/v1/browse/categories";
        String categoryId = "";

        try {
            while (categoryId.equals("")) {
                JsonObject responseJson = sendApiRequest(path);
                JsonObject categories = responseJson.get("categories").getAsJsonObject();
                for (JsonElement category : categories.getAsJsonArray("items")) {
                    JsonObject categoryObject = category.getAsJsonObject();
                    if (categoryObject.get("name").getAsString().equals(target)) {
                        categoryId = categoryObject.get("id").getAsString();
                    }
                }
                JsonElement pathElement = responseJson.get("next");
                if (pathElement != null) {
                    path = pathElement.getAsString();
                }
                else {
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error, please try again");
        }
        return categoryId;
    }

    static JsonObject sendApiRequest(String path) {
        var client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + Config.ACCESS_TOKEN)
                .uri(URI.create(Config.API_SERVER_PATH + path))
                .GET()
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return JsonParser.parseString(String.valueOf(response != null ? response.body() : null)).getAsJsonObject();

    }

}

