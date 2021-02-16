package advisor;
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
        if (args.length > 0) {
            if (args[0] == "-access") {
                if (args[1].length() > 0) {
                    Config.SERVER_PATH = args[1];
                }
            }
        }

        initialInput();
    }

    public static void initialInput() throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        switch(input) {
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

    public static void authorize() throws IOException, InterruptedException {
        System.out.println(Config.SERVER_PATH + "/authorize?" +
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

    public static void getToken() {
        System.out.println("code received");
        System.out.println("making http request for access_token...");
        String encodedKey = Base64.getEncoder().encodeToString((Config.CLIENT_ID + ":" + Config.CLIENT_SECRET).getBytes());
        var client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type","application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + encodedKey)
                .uri(URI.create(Config.SERVER_PATH + "/api/token"))
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

        System.out.println("response:");
        System.out.println(response.body());
        System.out.println("---SUCCESS---");
        takeInput();
    }

    public static void takeInput(){
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        switch(input) {
            case "new":
                System.out.println("---NEW RELEASES---\n" +
                        "Mountains [Sia, Diplo, Labrinth]\n" +
                        "Runaway [Lil Peep]\n" +
                        "The Greatest Show [Panic! At The Disco]\n" +
                        "All Out Life [Slipknot]");
                takeInput();
                break;
            case "featured":
                System.out.println("---FEATURED---\n" +
                        "Mellow Morning\n" +
                        "Wake Up and Smell the Coffee\n" +
                        "Monday Motivation\n" +
                        "Songs to Sing in the Shower");
                takeInput();
                break;
            case "categories":
                System.out.println("---CATEGORIES---\n" +
                        "Top Lists\n" +
                        "Pop\n" +
                        "Mood\n" +
                        "Latin");
                takeInput();
                break;
            case "playlists Mood":
                System.out.println("---MOOD PLAYLISTS---\n" +
                        "Walk Like A Badass  \n" +
                        "Rage Beats  \n" +
                        "Arab Mood Booster  \n" +
                        "Sunday Stroll");
                takeInput();
                break;
            case "exit":
                System.out.println("---GOODBYE!---");
                break;
            default:
                takeInput();
                break;
        }

    }
}
