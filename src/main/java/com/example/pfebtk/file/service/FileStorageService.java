package com.example.pfebtk.file.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    @Value("${file.convention.dir}")
    private String conventionDir;

    @Value("${file.convention.signed.dir}")
    private String signedDir;

    @PostConstruct
    public void init() {
        new File(conventionDir).mkdirs();
        new File(signedDir).mkdirs();
    }

    // resp upload convention
    public String saveConvention(MultipartFile file) {
        return saveToDir(file, conventionDir);
    }

    // emp re-upload convention signée
    public String saveConventionSigned(MultipartFile file) {
        return saveToDir(file, signedDir);
    }

    //  charger un fichier
    public Resource loadConvention(String filename) {
        return loadFromDir(filename, conventionDir);
    }

    public Resource loadConventionSigned(String filename) {
        return loadFromDir(filename, signedDir);
    }

    //  supprimer
    public void deleteConvention(String filename) {
        deleteFromDir(filename, conventionDir);
    }

    public void deleteConventionSigned(String filename) {
        deleteFromDir(filename, signedDir);
    }

    //  vérifier PDF
    public boolean isPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide ou null !");
        }
        String name = file.getOriginalFilename();
        return name != null && name.toLowerCase().endsWith(".pdf");
    }

    //  helpers privés
    private String saveToDir(MultipartFile file, String dir) {
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(dir, filename);
        try {
            Files.write(path, file.getBytes());
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Impossible de sauvegarder : " + filename, e);
        }
    }

    private Resource loadFromDir(String filename, String dir) {
        try {
            Path path = Paths.get(dir).resolve(filename).normalize();
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) return resource;
            throw new RuntimeException("Fichier introuvable : " + filename);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Fichier introuvable : " + filename, e);
        }
    }

    private void deleteFromDir(String filename, String dir) {
        try {
            Files.deleteIfExists(Paths.get(dir, filename));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de supprimer : " + filename, e);
        }
    }
}
