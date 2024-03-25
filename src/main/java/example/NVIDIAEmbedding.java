package example;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

public class NVIDIAEmbedding {

    @UserFunction
    @Description("example.NVIDIAEmbedding('this is nice', 'api key') - Get the embedding for the string using the API key")
    public List<Float> NVIDIAEmbedding(
            @Name("text") String text,
            @Name("apiKey") String apiKey) {

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        String invokeUrl = "https://ai.api.nvidia.com/v1/retrieval/nvidia/embeddings";
        String requestBody = "{\"input\": \"" + text + "\", \"input_type\": \"query\", \"model\": \"NV-Embed-QA\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(invokeUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        List<Float> embeddingList = new ArrayList<>();

        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            int statusCode = response.statusCode();
            HttpHeaders headers = response.headers();
            String responseBody = response.body();
            JSONObject jsonObject = new JSONObject(responseBody);

            if (jsonObject.has("data")) {
                JSONArray dataArray = jsonObject.getJSONArray("data");
                if (dataArray.length() > 0) {
                    JSONObject dataObject = dataArray.getJSONObject(0);
                    if (dataObject.has("embedding")) {
                        JSONArray embeddingArray = dataObject.getJSONArray("embedding");
                        for (int i = 0; i < embeddingArray.length(); i++) {
                            embeddingList.add((float) embeddingArray.getDouble(i));
                        }
                    }
                }
            }
            System.out.println("Embedding List: " + embeddingList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return embeddingList;
    }
}