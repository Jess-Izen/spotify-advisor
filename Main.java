package advisor;
import java.io.IOException;

public class Main {


    public static void main(String[] args) throws IOException, InterruptedException {

        //process command line arguments
        if (args.length > 0) {
            for (int i = 0; i < args.length - 1; i++) {
                switch (args[i]) {
                    case "-access":
                        if (args[i + 1].length() > 0) {
                            Config.AUTH_SERVER_PATH = args[i + 1];
                        }
                        break;
                    case "-resource":
                        if (args[i + 1].length() > 0) {
                            Config.API_SERVER_PATH = args[i + 1];
                        }
                        break;
                    case "-page":
                        if (args[i + 1].length() > 0) {
                            try {
                                Config.PAGINATION = Integer.parseInt(args[i + 1]);
                            } catch (NumberFormatException nfe) {
                            }
                        }
                        break;
                }
            }

        }

        Controller.initialInput();
    }


}

