package com.valhora.backend.common.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.valhora.backend.common.exception.ImageUploadException;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryService {

    private static final String PRODUCTS_FOLDER = "valhora/products";

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String upload(MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", PRODUCTS_FOLDER));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new ImageUploadException("No se pudo subir la imagen a Cloudinary", e);
        }
    }
}
