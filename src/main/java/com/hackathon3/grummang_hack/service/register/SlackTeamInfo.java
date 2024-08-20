package com.hackathon3.grummang_hack.service.register;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class SlackTeamInfo {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> getTeamInfo(String token) throws IOException, InterruptedException {
        String url = "https://slack.com/api/team.info";

        // HttpClient 생성
        HttpClient client = HttpClient.newHttpClient();

        // 요청 준비
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        // 요청 보내기 및 응답 받기
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 응답 출력 및 필요한 값 추출
        if (response.statusCode() == 200) {
            String responseBody = response.body();

            // JSON 응답을 JsonNode로 변환
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // JSON 응답에서 필요한 값 추출
            if (jsonNode.path("ok").asBoolean()) {
                JsonNode teamNode = jsonNode.path("team");

                // asText() 메서드로 변경, 기본값은 null
                String teamName = teamNode.path("name").asText(); // 기본값은 빈 문자열
                String teamId = teamNode.path("id").asText(); // 기본값은 빈 문자열

                List<String> teamInfo = new ArrayList<>();
                teamInfo.add(teamName);
                teamInfo.add(teamId);

                System.out.println(teamInfo);
                // 특정 값 반환
                return teamInfo;
            } else {
                String error = jsonNode.path("error").asText(); // 기본값은 빈 문자열
                throw new IOException("Error: " + error);
            }
        } else {
            throw new IOException("Failed to fetch team info. Status code: " + response.statusCode());
        }
    }
}
