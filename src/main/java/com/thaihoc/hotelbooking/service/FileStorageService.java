package com.thaihoc.hotelbooking.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final String uploadDir = "C:/hotelbooking/uploads/";

    public String store(MultipartFile file, String folder) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destination = Paths.get(uploadDir + folder + fileName);
            Files.createDirectories(destination.getParent());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return folder + fileName; // URL trả về FE
        } catch (Exception e) {
            throw new RuntimeException("FILE_UPLOAD_FAILED", e);
        }
    }

    public String update(MultipartFile file, String oldPath, String folder) {
        try {

            String oldFileName = Paths.get(oldPath).getFileName().toString();
            Path oldFilePath = Paths.get(uploadDir + folder + oldFileName);

            if (Files.exists(oldFilePath)) {
                Files.delete(oldFilePath);
            }

            // Gọi lại store để lưu file mới
            return store(file, folder);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // 🆕 Hàm xóa file theo path
    public boolean delete(String path) {
        try {
            Path filePath = Paths.get(uploadDir + path);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true; // Xóa thành công
            }
            return false; // File không tồn tại
        } catch (IOException e) {
            return false; // Xóa thất bại
        }
    }

}


