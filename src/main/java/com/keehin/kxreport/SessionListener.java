package com.keehin.kxreport;

import java.nio.file.*;
import java.io.IOException;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SessionListener implements HttpSessionListener {

    private static final Logger logger = LoggerFactory.getLogger(SessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        String code = createHashCode(se.getSession());
        String dir = Database.ROOT + se.getSession().getServletContext().getContextPath() + "/" + code;
        logger.info("Sess.Created {}", dir);
        Path dirPath = Paths.get(dir);
        if (!Files.exists(dirPath))
            try {
                Files.createDirectories(dirPath); // creates all non-existent parent directories
            } catch (IOException e) {
                logger.error("Failed to create directory: {}", dirPath, e);
            }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String code = createHashCode(se.getSession());
        deleteDirectory(Database.ROOT + se.getSession().getServletContext().getContextPath() + "/" + code);
    }

    public static String createHashCode(HttpSession session) {
        return Integer.toHexString(session.getId().hashCode()).toUpperCase();
    }

    public static boolean deleteDirectory(String dirName) {
        logger.info("Sess.Deleted {}", dirName);
        if (dirName == null || dirName.isBlank())
            return false;

        Path dirPath = Paths.get(dirName);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath))
            return false;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path filePath : stream)
                Files.deleteIfExists(filePath); // delete file
            Files.deleteIfExists(dirPath);
        } catch (IOException e) {
            logger.error("Error listing directory: {}", dirName, e);
            return false;
        }

        return !Files.exists(dirPath);
    }
}
