package com.example.pfebtk.signatureDetection.controller;

import com.example.pfebtk.signatureDetection.service.SignatureDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/emp/signature")
public class SignatureDetectionController {

    @Autowired
    private SignatureDetectionService signatureDetectionService;

    @PostMapping(value ="/detect" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> detect( @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        boolean signed = signatureDetectionService.detectSignature(file);

        if (signed) {
            return ResponseEntity.ok("Document signé");
        } else {
            return ResponseEntity.ok("Document non signé");
        }
    }

}
