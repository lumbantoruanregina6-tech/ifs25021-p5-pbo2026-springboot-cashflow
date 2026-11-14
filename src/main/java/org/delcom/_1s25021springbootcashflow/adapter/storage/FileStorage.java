package org.delcom._1s25021springbootcashflow.adapter.storage;

import org.delcom._1s25021springbootcashflow.domain.exception.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Menyimpan upload ke folder UPLOAD_DIR dengan nama UUID + ekstensi. */
public class FileStorage {
    private static final Set<String> ALLOWED = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Tidak bisa membuat folder upload: " + root, e);
        }
    }

    public Path getRoot() {
        return root;
    }

    /** Mengembalikan path relatif (nama file) yang tersimpan, mis. 3f2a....png */
    public String store(String originalName, byte[] data) throws ValidationException, IOException {
        String ext = extensionOf(originalName);
        if (!ALLOWED.contains(ext)) {
            throw new ValidationException("Format gambar harus png, jpg, jpeg, gif, atau webp");
        }
        String name = UUID.randomUUID() + "." + ext;
        Files.write(root.resolve(name), data);
        return name;
    }

    public void delete(String relativePath) throws IOException {
        if (relativePath != null && !relativePath.isBlank()) {
            Files.deleteIfExists(root.resolve(relativePath).normalize());
        }
    }

    /** URL publik untuk path relatif, dilayani oleh handler /uploads/**. */
    public static String url(String relativePath) {
        return "/uploads/" + relativePath;
    }

    private static String extensionOf(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
