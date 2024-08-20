package com.hackathon3.grummang_hack.service.google;


import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.hackathon3.grummang_hack.model.entity.WorkspaceConfig;
import com.hackathon3.grummang_hack.repository.WorkSpaceConfigRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleUtilService {

    private static final String APPLICATION_NAME = "grummang-google-dirve-func";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final WorkSpaceConfigRepo workspaceConfigRepo;

    @Value("${aes.key}")
    private String key;

    public Credential selectToken(int workspace_id) {
        try {
            // workspace_id를 통해 해당 workspace의 token을 가져옴
            WorkspaceConfig workspaceConfig = workspaceConfigRepo.findById(workspace_id).orElse(null);
            if (workspaceConfig == null) { // 수정: 잘못된 workspace ID 처리
                throw new IllegalArgumentException("Invalid workspace ID");
            }
            return new Credential(BearerToken.authorizationHeaderAccessMethod())
                    .setAccessToken(workspaceConfig.getToken()); // 토큰을 반환
        } catch (Exception e) {
            log.error("An error occurred while selecting the token: {}", e.getMessage(), e);
            return null;
        }
    }

    public Drive getDriveService(int workspace_id) throws Exception {
        try {
            return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, selectToken(workspace_id))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            log.error("An error occurred while creating the Drive service: {}", e.getMessage(), e);
            throw e;
        }
    }


//    private void TokenUpdate(String Token, int workdpace_id){
//        try {
//            String freshToken = Token;
//            workspaceConfigRepo.updateToken(workdpace_id, freshToken);
//        } catch (Exception e) {
//            log.error("An error occurred while updating the token: {}", e.getMessage(), e);
//        }
//    }
}
