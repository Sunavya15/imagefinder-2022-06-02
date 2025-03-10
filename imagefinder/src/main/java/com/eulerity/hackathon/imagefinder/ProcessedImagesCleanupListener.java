package com.eulerity.hackathon.imagefinder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;

@WebListener
public class ProcessedImagesCleanupListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String path = sce.getServletContext().getRealPath("/processedImages");
        if (path != null) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                for (File file : dir.listFiles()) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
                System.out.println("Processed images folder cleaned on startup.");
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
