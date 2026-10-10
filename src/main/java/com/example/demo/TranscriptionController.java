package com.example.demo;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/transcribe")
@CrossOrigin(origins = "http://localhost:5173")
public class TranscriptionController {
	



	    private final TranscriptionService service;

	    public TranscriptionController(TranscriptionService service) {
	        this.service = service;
	    }

	    @PostMapping(
	        value = "/transcribe",
	        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	    )
	    public String transcribe(
	            @RequestParam("file") MultipartFile file)
	            throws IOException, InterruptedException {

	        return service.transcribe(file);
	    }
	}

