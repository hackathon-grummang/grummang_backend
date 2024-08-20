package com.hackathon3.grummang_hack.service.vt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon3.grummang_hack.model.dto.Result;
import com.hackathon3.grummang_hack.model.entity.StoredFile;
import com.hackathon3.grummang_hack.repository.StoredFileRepo;
import com.hackathon3.grummang_hack.service.file.S3FileDownloadService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class VtUploadService {

    private static final Logger logger = LoggerFactory.getLogger(VtUploadService.class);

    @Value("${virustotal.api.key}")
    private String apikey;

    private static final String BASE_URL = "https://www.virustotal.com/api/v3/files";
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final StoredFileRepo storedFileRepo;
    private final S3FileDownloadService s3FileDownloadService;
    private static final int SIZE_32MB = 32 * 1024 * 1024;
    private static final int SIZE_650MB = 650 * 1024 * 1024;

    private final FileStatusService fileStatusService;

    public VtUploadService(FileStatusService fileStatusService, StoredFileRepo storedFileRepo, S3FileDownloadService s3FileDownloadService) {
        this.objectMapper = new ObjectMapper();
        this.client = new OkHttpClient();
        this.fileStatusService = fileStatusService;
        this.storedFileRepo = storedFileRepo;
        this.s3FileDownloadService = s3FileDownloadService;
    }

    public Result uploadFileFromS3(long fileId) {
        Optional<StoredFile> optionalStoredFile = storedFileRepo.findById(fileId);
        if (optionalStoredFile.isEmpty()) {
            return new Result(false, "File not found in database");
        }
        StoredFile storedFile = optionalStoredFile.get();

        int fileSizeOfByte = storedFile.getSize();
        String savePath = storedFile.getSavePath();
        String[] parts = savePath.split("/", 2);

        if (parts.length != 2) {
            return new Result(false, "Invalid savePath format");
        }

        String bucketName = parts[0];
        String key = parts[1];
        try (InputStream inputStream = s3FileDownloadService.downloadFile(bucketName, key)) {
            String uploadUrl = getUploadUrl(fileSizeOfByte);
            String analysisId = performUpload(uploadUrl, inputStream, key, fileId);
            return new Result(true, analysisId); // 성공 시 분석 ID 반환
        } catch (IOException e) {
            logger.error("Error during file upload: {}", e.getMessage());
            return new Result(false, "Error during file upload: " + e.getMessage());
        }
    }

    public String getUploadUrl(int fileSize) throws IOException {
        if (fileSize < SIZE_32MB) {
            logger.debug("size 32MB 미만: {}",fileSize);
            return BASE_URL;
        } else if (fileSize < SIZE_650MB) {
            logger.debug("size 32MB 이상 650MB 미만: {}",fileSize);
            String url = BASE_URL + "/upload_url";
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("x-apikey", apikey)
                    .addHeader("accept", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to get upload URL: " + response);
                }

                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new IOException("Response body is null for URL: " + url);
                }

                String bodyString = responseBody.string();
                return extractUploadUrl(bodyString);
            }
        } else {
            throw new IOException("Error: File size exceeds 650MB limit.");
        }
    }

    private String extractUploadUrl(String responseBody) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.path("data").asText();
    }

    @Transactional
    public String performUpload(String url, InputStream inputStream, String fileName, long fileId) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(inputStream.readAllBytes(), MediaType.parse("application/octet-stream")))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("x-apikey", apikey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to upload file: " + response);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("Response body is null for URL: " + url);
            }

            String bodyString = responseBody.string();
            fileStatusService.updateVtStatus(fileId, 0);
            String analysisId = extractAnalysisId(bodyString);
            if (analysisId == null) {
                throw new IOException("Failed to extract analysis ID from response");
            }
            return analysisId;
        }
    }

    private String extractAnalysisId(String responseBody) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.path("data").path("id").asText();
    }
}
