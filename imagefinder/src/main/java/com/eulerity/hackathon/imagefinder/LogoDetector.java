package com.eulerity.hackathon.imagefinder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Logo detection from website URLs or direct image URLs.
 */
public class LogoDetector {
    private static final String LOGO_DIR = "src/main/resources/detected_logos";
    private static final double MATCH_THRESHOLD = 250.0; // Adjust this for accuracy

    static {
        System.load("C:\\Users\\030825130\\Downloads\\opencv\\build\\java\\x64\\opencv_java3416.dll");
    }

    /**
     * Determines whether the given URL is a direct image or a webpage.
     * @param url The input URL.
     * @return True if it's an image, false if it's a webpage.
     */
    private static boolean isImageURL(String url) {
        return url.matches(".*\\.(jpg|jpeg|png|svg|gif|bmp|webp)$");
    }

    /**
     * Detects a logo from a given URL (website or direct image).
     * @param inputUrl The URL to check.
     * @return True if a logo is detected.
     */
    public static boolean detectLogo(String inputUrl) {
        try {
            if (isImageURL(inputUrl)) {
                // ✅ URL is a direct image, download and analyze
                System.out.println("⬇ Direct image detected, downloading: " + inputUrl);
                File imageFile = downloadImage(inputUrl);
                if (imageFile != null && containsLogo(imageFile.getAbsolutePath())) {
                    System.out.println("✅ Logo detected in: " + inputUrl);
                    return true;
                }
            } else {
                // ✅ URL is a webpage, extract and analyze images
                System.out.println("🔍 Extracting potential logos from webpage: " + inputUrl);
                List<String> logoUrls = extractLogoUrls(inputUrl);
                if (logoUrls.isEmpty()) {
                    System.out.println("🚫 No potential logos found on the page.");
                    return false;
                }

                for (String logoUrl : logoUrls) {
                    System.out.println("⬇ Downloading logo candidate: " + logoUrl);
                    File logoFile = downloadImage(logoUrl);
                    if (logoFile != null && containsLogo(logoFile.getAbsolutePath())) {
                        System.out.println("✅ Logo detected in: " + logoUrl);
                        return true;
                    }
                }
            }
            System.out.println("🚫 No valid logos detected.");
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error processing URL: " + inputUrl + " | " + e.getMessage());
            return false;
        }
    }

    /**
     * Extracts image URLs from a webpage that might be logos.
     * @param websiteUrl The webpage URL.
     * @return A list of possible logo image URLs.
     */
    private static List<String> extractLogoUrls(String websiteUrl) {
        List<String> logoUrls = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(websiteUrl).get();
            for (Element img : doc.select("img")) {
                String imgUrl = img.absUrl("src");
                if (imgUrl.contains("logo") || imgUrl.contains("brand") || imgUrl.contains("icon")) {
                    logoUrls.add(imgUrl);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to extract image URLs: " + e.getMessage());
        }
        return logoUrls;
    }

    /**
     * Downloads an image from a URL and saves it locally.
     * @param imageUrl The image URL.
     * @return The downloaded File object.
     */
    private static File downloadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            File logoDir = new File(LOGO_DIR);
            if (!logoDir.exists()) logoDir.mkdirs();

            String fileName = "logo_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(logoDir, fileName);

            try (InputStream in = url.openStream()) {
                Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            return outputFile;
        } catch (IOException e) {
            System.err.println("❌ Failed to download image: " + imageUrl + " | " + e.getMessage());
            return null;
        }
    }

    /**
     * Detects if an image is a logo using OpenCV's SIFT feature detector.
     * @param imagePath Path to the image file.
     * @return True if the image is a logo.
     */
    public static boolean containsLogo(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath, Imgcodecs.IMREAD_GRAYSCALE);
        if (image.empty()) {
            System.err.println("❌ Failed to load image: " + imagePath);
            return false;
        }

        // **Feature Detection**
        SIFT sift = SIFT.create();
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();
        sift.detectAndCompute(image, new Mat(), keypoints, descriptors);

        return keypoints.size().height > 50;  // Arbitrary threshold for logo-like features
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java LogoDetector <image-url or website-url>");
            return;
        }

        String inputUrl = args[0];
        boolean isLogoDetected = detectLogo(inputUrl);
        System.out.println("🔎 Is a logo detected? " + isLogoDetected);
    }
}
