package jeju.bear.global.service;

import jeju.bear.global.common.CustomException;
import jeju.bear.global.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${file.path}")
    private String path;

    public String uploadProfileImage(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            return null;
        }
        String dir = Paths.get(path, "profile").toString();
        return saveImage(file, dir);
    }

    public String uploadPostImage(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            return null;
        }

        String dir = Paths.get(path, "post").toString();
        return saveImage(file, dir);
    }

    private String saveImage(MultipartFile image, String dir) {
        String filename = UUID.randomUUID().toString().replace("-", "") + "_" + image.getOriginalFilename();
        String filePath = Paths.get(dir, filename).toString();

        Path path = Paths.get(filePath);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, image.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 생성 오류");
        }
        return filePath;
    }

    public void deleteImage(String filePath) {
        if(filePath != null && !filePath.isEmpty()) {
            Path path = Paths.get(filePath);

            try {
                Files.deleteIfExists(path); // 파일이 존재하면 삭제, 없으면 무시
            } catch (IOException e) {
                e.printStackTrace();
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 삭제 오류");
            }
        }
    }

}
