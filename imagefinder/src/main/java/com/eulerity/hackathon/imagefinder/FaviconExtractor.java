package com.eulerity.hackathon.imagefinder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FaviconExtractor {
    private static final String FAVICON_DIR = "C:\\Users\\030825130\\Downloads\\imagefinder-2022-06-02\\imagefinder\\src\\main\\resources\\templates\\favicons";

    public static String extractFaviconUrl(String pageUrl) {
        try {
            // Fetch HTML content
            Document doc = Jsoup.connect(pageUrl).get();

            // Look for <link rel="icon"> or <link rel="shortcut icon">
            Element faviconElement = doc.select("link[rel=icon], link[rel='shortcut icon']").first();
            if (faviconElement != null) {
                String faviconUrl = faviconElement.attr("href");

                // Convert relative URLs to absolute
                if (!faviconUrl.startsWith("http")) {
                    URL baseUrl = new URL(pageUrl);
                    faviconUrl = baseUrl.getProtocol() + "://" + baseUrl.getHost() + "/" + faviconUrl;
                }

                System.out.println("✅ Favicon Found in HTML: " + faviconUrl);
                return faviconUrl;
            }

            // If no favicon is found in HTML, fallback to /favicon.ico
            URL baseUrl = new URL(pageUrl);
            String defaultFaviconUrl = baseUrl.getProtocol() + "://" + baseUrl.getHost() + "/favicon.ico";
            if (isValidFavicon(defaultFaviconUrl)) {
                System.out.println("✅ Using fallback favicon: " + defaultFaviconUrl);
                return defaultFaviconUrl;
            }

        } catch (Exception e) {
            System.out.println("🚫 No favicon found for: " + pageUrl);
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

            // **Generate a unique filename based on the website URL**
            String domainHash = hashDomain(url.getHost());
            String extension = faviconUrl.endsWith(".png") ? "png" : "ico";
            String fileName = "favicon_" + domainHash + "." + extension;
            File destinationFile = new File(FAVICON_DIR, fileName);

            // **Check if the file already exists**
            if (destinationFile.exists()) {
                System.out.println("⚠️ Favicon already exists, skipping download: " + destinationFile.getAbsolutePath());
                return destinationFile;
            }

            // **Download the favicon**
            try (InputStream in = url.openStream()) {
                Files.copy(in, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("✅ Favicon downloaded: " + destinationFile.getAbsolutePath());

            // **Convert .ico to .png if needed**
            if (extension.equals("ico")) {
                File pngFile = new File(FAVICON_DIR, fileName.replace(".ico", ".png"));
                if (convertICOtoPNG(destinationFile, pngFile)) {
                    return pngFile;
                }
            }

            return destinationFile;
        } catch (Exception e) {
            System.err.println("❌ Error downloading favicon: " + e.getMessage());
            return null;
        }
    }

    // **Check if favicon URL is valid**
    private static boolean isValidFavicon(String faviconUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(faviconUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400);
        } catch (IOException e) {
            return false;
        }
    }

    // **Convert ICO to PNG**
    private static boolean convertICOtoPNG(File icoFile, File pngFile) {
        try {
            Image image = ImageIO.read(icoFile);
            if (image == null) {
                return false;
            }

            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bufferedImage.createGraphics();
            g2.drawImage(image, 0, 0, null);
            g2.dispose();

            return ImageIO.write(bufferedImage, "png", pngFile);
        } catch (IOException e) {
            System.err.println("❌ Error converting ICO to PNG: " + e.getMessage());
            return false;
        }
    }

    // **Generate a hash from the domain name (avoids duplicate favicons)**
    private static String hashDomain(String domain) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(domain.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString().substring(0, 10); // Shorten for filename
        } catch (NoSuchAlgorithmException e) {
            return domain.replace(".", "_"); // Fallback
        }
    }
}
