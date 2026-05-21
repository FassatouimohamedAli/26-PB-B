package com.example.pfebtk.image.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${file.image.dir}")
    private String imageDir;

    @PostConstruct
    public void init() {
        new File(imageDir).mkdirs();
    }

    // ✅ Sauvegarder image
    public String saveImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Image vide !");
        }

        if (!isImage(file)) {
            throw new RuntimeException("Seuls les fichiers image sont autorisés !");
        }


        String fileName = System.currentTimeMillis() +"-" + file.getOriginalFilename();

        try {
            Path path = Paths.get(imageDir).resolve(fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur sauvegarde image", e);
        }
    }

    // ✅ Charger image
    public Resource loadImage(String filename) {
        try {
            Path path = Paths.get(imageDir).resolve(filename).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }

            throw new RuntimeException("Image introuvable !");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur chargement image", e);
        }
    }

    // ✅ Supprimer image
    public void deleteImage(String filename) {
        try {
            Files.deleteIfExists(Paths.get(imageDir).resolve(filename));
        } catch (IOException e) {
            throw new RuntimeException("Erreur suppression image", e);
        }
    }

    // ✅ Vérifier type image
    public boolean isImage(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name != null &&
                (name.toLowerCase().endsWith(".png") ||
                        name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg"));
    }

    // helper extension
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new RuntimeException("Extension invalide !");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
