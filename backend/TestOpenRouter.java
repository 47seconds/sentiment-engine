import java.io.*;
import java.net.http.*;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class TestOpenRouter {
    public static void main(String[] args) throws Exception {
        String apiKey = "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";
        String model = "mistralai/mistral-7b-instruct:free";
        
        System.out.println("Testing OpenRouter API...");
        
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Analyze sentiment: 'very good driver'. Return only: POSITIVE or NEGATIVE");
        request.put("messages", Arrays.asList(message));
        
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(request);
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
            
        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
}
