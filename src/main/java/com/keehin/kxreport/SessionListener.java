package com.keehin.kxreport;

import java.io.File;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

@Component
public class SessionListener implements HttpSessionListener {
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        String code = createHashCode(se.getSession());
        System.out.println("Sess.Created " + code);
        File dir = new File(Database.getOUTPUT_PATH() + code);
		if (!dir.exists())
			dir.mkdirs();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String code = createHashCode(se.getSession());
        System.out.println("Sess.Deleted " + code);
        deleteDirectory(new File(Database.getOUTPUT_PATH() + code));
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
                    file.delete();
            }
        }
        return directory.delete();
    }
}
