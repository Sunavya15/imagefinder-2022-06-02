package com.eulerity.hackathon.imagefinder;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FaviconExtractor {
    private static final String FAVICON_DIR = "C:\\Users\\030825130\\Downloads\\imagefinder-2022-06-02\\imagefinder\\src\\main\\resources\\templates\\favicons";

    public static String extractFaviconUrl(String pageUrl) {
        WebDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--disable-gpu");
            driver = new ChromeDriver(options);
            driver.get(pageUrl);

            WebElement faviconElement = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//link[@rel='icon'] | //link[@rel='shortcut icon']")
                ));

            String faviconUrl = faviconElement.getAttribute("href");
            if (faviconUrl != null && !faviconUrl.isEmpty()) {
                System.out.println("✅ Favicon Found: " + faviconUrl);
                return faviconUrl;
            }
        } catch (Exception e) {
            System.out.println("🚫 No favicon found on: " + pageUrl);
        } finally {
            if (driver != null) driver.quit();
        }
        return null;
    }

    public static File downloadFavicon(String faviconUrl) {
        try {
            URL url = new URL(faviconUrl);
            File faviconDir = new File(FAVICON_DIR);
            if (!faviconDir.exists()) {
                faviconDir.mkdirs();
            }

            // Save the favicon as .ico
            String fileName = "favicon_" + System.currentTimeMillis() + ".ico";
            File destinationFile = new File(FAVICON_DIR, fileName);

            try (InputStream in = url.openStream()) {
                Files.copy(in, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("✅ Favicon downloaded: " + destinationFile.getAbsolutePath());
            return destinationFile;
        } catch (Exception e) {
            System.err.println("❌ Error downloading favicon: " + e.getMessage());
            return null;
        }
    }
}
