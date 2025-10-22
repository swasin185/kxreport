package com.keehin.kxreport;

import java.io.File;

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
        logger.info("Sess.Created {}", code);
        File dir = new File(Database.getOutputPath() + code);
		if (!dir.exists())
			dir.mkdirs();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String code = createHashCode(se.getSession());
        logger.info("Sess.Deleted {}", code);
        deleteDirectory(new File(Database.getOutputPath() + code));
    }

    public static String createHashCode(HttpSession session) {
        return Integer.toHexString(session.getId().hashCode()).toUpperCase();
    }

    public static boolean deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return false;
        }
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) 
                    if (!file.delete()) 
                        logger.error("Session auto delete {}", file.getName());
            }
        }
        return directory.delete();
    }
}
