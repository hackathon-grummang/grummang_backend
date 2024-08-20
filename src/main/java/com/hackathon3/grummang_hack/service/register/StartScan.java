package com.hackathon3.grummang_hack.service.register;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class StartScan {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode postToScan(Integer configId, String saasName) throws IOException, InterruptedException {
        String url = "http://localhost:8080/api/v1/connect/"+saasName+"/all";

        // HttpClient 생성
        HttpClient client = HttpClient.newHttpClient();

        // 요청 본문 준비
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("workspace_config_id", configId);
        String requestBody = objectMapper.writeValueAsString(requestBodyMap);

        // 요청 준비
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();


        // 요청 보내기 및 응답 받기
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 응답 출력 및 필요한 값 추출
        if (response.statusCode() == 200) {
            String responseBody = response.body();

            // JSON 응답을 JsonNode로 변환
            return objectMapper.readTree(responseBody);
        } else {
            throw new IOException("Fail" + response.statusCode());
        }
    }
}
