package advisor;

import com.google.gson.JsonArray;


class Model {
    private JsonArray data;
    private int totalItems;
    int currentPage;
    int totalPages;
    String type;


    Model(JsonArray data, String type) {
        this.data = data;
        this.totalPages = (int) Math.ceil(data.size() / Config.PAGINATION);
        this.totalItems = data.size();
        this.type = type;
        this.currentPage = 1;
    }


     JsonArray getData() {
        JsonArray returnData = new JsonArray();
        for (int i = (currentPage - 1) * Config.PAGINATION; i < (currentPage -1) * Config.PAGINATION + Config.PAGINATION; i++) {

            returnData.add(data.get(i));

            if (i == totalItems - 1) {
                break;
            }
        }

        return returnData;
    }

}
