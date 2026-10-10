package com.example.demo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


@Service
public class TranscriptionService {



    @Value("${groq.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String transcribe(MultipartFile file)
            throws IOException, InterruptedException {

        String boundary = "Boundary-" + UUID.randomUUID();
        String filename = file.getOriginalFilename() == null
                ? "audio.wav"
                : file.getOriginalFilename().replace("\"", "");

        String contentType = file.getContentType() == null
                ? "application/octet-stream"
                : file.getContentType();

        byte[] audio = file.getBytes();

        byte[] prefix = (
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"model\"\r\n\r\n" +
                "whisper-large-v3-turbo\r\n" +
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" +
                filename + "\"\r\n" +
                "Content-Type: " + contentType + "\r\n\r\n"
        ).getBytes(StandardCharsets.UTF_8);

        byte[] suffix = (
                "\r\n--" + boundary + "--\r\n"
        ).getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[prefix.length + audio.length + suffix.length];

        System.arraycopy(prefix, 0, body, 0, prefix.length);
        System.arraycopy(audio, 0, body, prefix.length, audio.length);
        System.arraycopy(suffix, 0, body,
                prefix.length + audio.length, suffix.length);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.groq.com/openai/v1/audio/transcriptions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type",
                        "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response =
                HttpClient.newHttpClient().send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Groq API error (" + response.statusCode() + ")");
        }

        JsonNode json = objectMapper.readTree(response.body());
        return json.path("text").asText();
    }
}


