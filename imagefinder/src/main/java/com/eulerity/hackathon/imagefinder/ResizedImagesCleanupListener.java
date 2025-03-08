package com.eulerity.hackathon.imagefinder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;

@WebListener
public class ResizedImagesCleanupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Get the real path to the "resizedImages" folder.
        String path = sce.getServletContext().getRealPath("/resizedImages");
        if (path != null) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            file.delete();
                        }
                    }
                }
                System.out.println("Resized images folder cleaned on startup.");
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Optional: add any shutdown cleanup code if needed.
    }
}
