package advisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class View {

    static void showAlbums(JsonArray items) {
        for (JsonElement album : items) {
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
    }

    static void showPlaylists(JsonArray items) {
        for (JsonElement playlist : items) {
            JsonObject playlistObject = playlist.getAsJsonObject();

            //playlist name
            System.out.println(playlistObject.get("name").getAsString());

            //link
            System.out.println(playlistObject.get("external_urls").getAsJsonObject().get("spotify").getAsString() + "\n");
        }
    }

    static void showCategories(JsonArray items) {
        for (JsonElement category : items) {
            JsonObject categoryObject = category.getAsJsonObject();

            //category name
            System.out.println(categoryObject.get("name").getAsString());

        }
    }

    static void showMessage(String type) {

        switch (type) {
            case "General":
                System.out.println("Error, please try again");
                break;
            case "Category":
                System.out.println("Unknown category name.");
                break;
            case "Exit":
                System.out.println("---GOODBYE!---");
                break;
            case "Input":
                System.out.println("Input not recognized, please try again");
                break;
            case "Access":
                System.out.println("Please, provide access for application.");
                break;
            case "Page":
                System.out.println("No more pages.");
                break;
            case "InitialAuth":
                System.out.println("Please input 'auth' to authorize application");
        }
    }

    static void showPages(int current, int total) {
        System.out.println("---PAGE "+ current +" OF " + total + "---");
    }

    static void showHelp(){
        System.out.println("Available Commands:");
        System.out.println("new - view new releases");
        System.out.println("featured - view featured playlists");
        System.out.println("categories - view available categories");
        System.out.println("playlists <category> - view playlists for provided category (case sensitive");
        System.out.println("prev, next - change pages for loaded option");
        System.out.println("help - view available commands");
        System.out.println("exit - quit");
    }


}
