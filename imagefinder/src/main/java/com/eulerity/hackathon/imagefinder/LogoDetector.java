package com.eulerity.hackathon.imagefinder;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Detects whether an image is a logo using template matching.
 */
public class LogoDetector {
    private static final String TEMPLATE_DIR = "C:\\Users\\030825130\\Downloads\\imagefinder-2022-06-02\\imagefinder\\src\\main\\resources\\templates\\logos";
    private static final double MATCH_THRESHOLD = 0.6; // Adjust for better detection

    /**
     * Checks if the provided image contains a logo using template matching.
     *
     * @param imagePath Path to the image file.
     * @return True if a logo is detected, false otherwise.
     */
    public static boolean containsLogo(String imagePath) {
        System.out.println("🔍 Checking for logos in: " + imagePath);

        // Load image as Mat
        Mat image = Imgcodecs.imread(imagePath, Imgcodecs.IMREAD_UNCHANGED);
        if (image.empty()) {
            System.err.println("❌ Failed to load image: " + imagePath);
            return false;
        }

        // Remove transparency if needed
        image = removeTransparency(image);

        // Load template images
        List<String> templatePaths = loadTemplateImages();
        if (templatePaths.isEmpty()) {
            System.err.println("⚠️ Warning: No template images found in " + TEMPLATE_DIR);
            return false;
        }

        // Check for matching logo
        for (String templatePath : templatePaths) {
            if (isMatchingLogo(image, templatePath)) {
                System.out.println("✅ Logo detected in: " + imagePath);
                return true;
            }
        }

        System.out.println("🚫 No logo detected in: " + imagePath);
        return false;
    }

    /**
     * Determines if the input image matches a given logo template.
     *
     * @param image        The loaded Mat object of the input image.
     * @param templatePath Path to the template image.
     * @return True if the image matches the template, false otherwise.
     */
    public static boolean isMatchingLogo(Mat image, String templatePath) {
        Mat template = Imgcodecs.imread(templatePath, Imgcodecs.IMREAD_UNCHANGED);

        if (template.empty()) {
            System.err.println("❌ Error: Could not load template - " + templatePath);
            return false;
        }

        // Remove transparency if needed
        template = removeTransparency(template);

        // Resize input image to match template size
        Imgproc.resize(image, image, new Size(template.width(), template.height()));

        // Perform template matching
        Mat result = new Mat();
        Imgproc.matchTemplate(image, template, result, Imgproc.TM_CCOEFF_NORMED);

        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        System.out.println("🔍 Match Score: " + mmr.maxVal + " for " + templatePath);

        return mmr.maxVal >= MATCH_THRESHOLD; // Threshold for a match
    }

    /**
     * Loads all template logo images from the templates directory.
     *
     * @return List of template image file paths.
     */
    private static List<String> loadTemplateImages() {
        List<String> templatePaths = new ArrayList<>();
        File directory = new File(TEMPLATE_DIR);

        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("⚠️ Template directory does not exist: " + TEMPLATE_DIR);
            return templatePaths;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png"))) {
                    templatePaths.add(file.getAbsolutePath());
                    System.out.println("✅ Loaded template: " + file.getAbsolutePath());
                }
            }
        }

        if (templatePaths.isEmpty()) {
            System.err.println("❌ No templates loaded! Check your templates folder.");
        }

        return templatePaths;
    }

    /**
     * Removes the alpha channel from an image (transparency) and keeps only the RGB.
     *
     * @param image Input Mat image.
     * @return Mat image with removed transparency.
     */
    public static Mat removeTransparency(Mat image) {
        if (image.channels() == 4) { // Check if the image has an alpha channel
            List<Mat> channels = new ArrayList<>();
            Core.split(image, channels); // Split channels
            
            Mat rgb = new Mat();
            Core.merge(channels.subList(0, 3), rgb); // Merge only RGB channels
            
            return rgb; // Return the image without transparency
        }
        return image; // Return original if no alpha channel
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Example usage
        String testImagePath = "C:\\Users\\030825130\\Downloads\\imagefinder-2022-06-02\\imagefinder\\src\\main\\resources\\templates\\logos\\test_logo1.png";
        boolean isLogo = containsLogo(testImagePath);
        System.out.println("Is the image a logo? " + isLogo);
    }
}
