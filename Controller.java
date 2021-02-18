package advisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

class Controller {

    static void initialInput() throws IOException, InterruptedException {
        View.showMessage("InitialAuth");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        switch (input) {
            case "auth":
                Auth.authorize();
                break;
            case "exit":
                View.showMessage("Exit");
                break;
            default:
                View.showMessage("Access");
                initialInput();
                break;
        }
    }



    static void takeInput(Model prevModel) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        //process input arg for playlists
        String category = null;
        if (input.startsWith("playlists")) {
            category = input.replace("playlists ","");
            input = "playlists";
        }

        switch (input) {
            case "new":
                getNewReleases();
                break;
            case "featured":
                getFeaturedReleases();
                break;
            case "categories":
                getCategories();
                break;
            case "playlists":
                getPlaylists(category);
                break;
            case "prev":
                if (prevModel.currentPage == 1) {
                    View.showMessage("Page");
                    takeInput(prevModel);
                }
                else {
                    prevModel.currentPage -= 1;
                    viewModel(prevModel);
                }
                break;
            case "next":
                if (prevModel.currentPage == prevModel.totalPages) {
                    View.showMessage("Page");
                    takeInput(prevModel);
                }
                else {
                    prevModel.currentPage += 1;
                    viewModel(prevModel);
                }
                break;

            case "help":
                View.showHelp();
                takeInput(prevModel);
                break;
            case "exit":
                View.showMessage("Exit");
                break;
            default:
                View.showMessage("Input");
                takeInput(prevModel);
                break;
        }

    }

    static void getNewReleases() {

        JsonObject responseJson = sendApiRequest("/v1/browse/new-releases");

        try {
            JsonObject albums = responseJson.get("albums").getAsJsonObject();
            JsonArray items = albums.getAsJsonArray("items");
            Model model = new Model(items, "albums");

            viewModel(model);

        } catch (Exception e) {
            View.showMessage("General");
            takeInput(null);

        }

    }


    static void getFeaturedReleases() {
        JsonObject responseJson = sendApiRequest("/v1/browse/featured-playlists");
        try {
            JsonObject playlists = responseJson.get("playlists").getAsJsonObject();
            JsonArray items = playlists.getAsJsonArray("items");
            Model model = new Model(items, "playlists");

            viewModel(model);

        } catch (Exception e) {
            View.showMessage("General");
            takeInput(null);
        }

    }

    static void getCategories() {

        JsonObject responseJson = sendApiRequest("/v1/browse/categories");

        try {
            JsonObject categories = responseJson.get("categories").getAsJsonObject();
            JsonArray items = categories.getAsJsonArray("items");
            Model model = new Model(items, "categories");

            viewModel(model);

        } catch (Exception e) {
            View.showMessage("General");
            takeInput(null);
        }

    }

    static void getPlaylists(String target ) {
        String categoryId  = findCategoryId(target);
        if (categoryId.equals("")) {
            View.showMessage("Category");
        } else {
            try {
                System.out.println(categoryId);
                JsonObject responseJson = sendApiRequest("/v1/browse/categories/" + categoryId + "/playlists");
                if (responseJson.get("error") != null) {
                    System.out.println(responseJson.get("error").getAsJsonObject().get("message").getAsString());
                    takeInput(null);
                } else {
                    JsonObject playlists = responseJson.get("playlists").getAsJsonObject();
                    JsonArray items = playlists.getAsJsonArray("items");

                    Model model = new Model(items, "playlists");

                    viewModel(model);

                }
            } catch (Exception e) {
                View.showMessage("General");
                takeInput(null);
            }
        }

    }


    static void viewModel(Model model) {

        switch (model.type){
            case "albums":
                View.showAlbums(model.getData());
                break;
            case "playlists":
                View.showPlaylists(model.getData());
                break;
            case "categories":
                View.showCategories(model.getData());
                break;
        }

        View.showPages(model.currentPage, model.totalPages);
        takeInput(model);
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
            View.showMessage("General");
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
