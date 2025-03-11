package com.eulerity.hackathon.imagefinder;

import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LogoDetector {
    private static final String TEMPLATE_DIR = "C:\\Users\\030825130\\Downloads\\imagefinder-2022-06-02\\imagefinder\\src\\main\\resources\\templates\\logos";
    private static final double MATCH_THRESHOLD = 300; // Adjust based on testing

    public static boolean containsLogo(String imagePath) {
        System.out.println("🔍 Checking for logos in: " + imagePath);

        Mat image = Imgcodecs.imread(imagePath, Imgcodecs.IMREAD_GRAYSCALE);
        if (image.empty()) {
            System.err.println("❌ Failed to load image: " + imagePath);
            return false;
        }

        List<String> templatePaths = loadTemplateImages();
        if (templatePaths.isEmpty()) {
            System.err.println("⚠️ Warning: No template images found in " + TEMPLATE_DIR);
            return false;
        }

        for (String templatePath : templatePaths) {
            if (isMatchingLogo(image, templatePath)) {
                System.out.println("✅ Logo detected in: " + imagePath);
                return true;
            }
        }

        System.out.println("🚫 No logo detected in: " + imagePath);
        return false;
    }

    private static boolean isMatchingLogo(Mat image, String templatePath) {
        Mat template = Imgcodecs.imread(templatePath, Imgcodecs.IMREAD_GRAYSCALE);

        if (template.empty()) {
            System.err.println("❌ Error: Could not load template - " + templatePath);
            return false;
        }

        // **SIFT Feature Detector** (More powerful than ORB)
        SIFT sift = SIFT.create();
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint(), keypoints2 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat(), descriptors2 = new Mat();

        // Detect keypoints and compute descriptors
        sift.detectAndCompute(image, new Mat(), keypoints1, descriptors1);
        sift.detectAndCompute(template, new Mat(), keypoints2, descriptors2);

        if (descriptors1.empty() || descriptors2.empty()) {
            System.err.println("⚠️ No keypoints found in image or template!");
            return false;
        }

        // Use FLANN-based Matcher for SIFT (Better for large dataset matching)
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        List<MatOfDMatch> knnMatches = new ArrayList<>();
        matcher.knnMatch(descriptors1, descriptors2, knnMatches, 2); // Find 2 best matches

        // **Ratio Test to filter good matches**
        double sumDistance = 0;
        int goodMatches = 0;
        for (MatOfDMatch matOfDMatch : knnMatches) {
            if (matOfDMatch.toArray().length < 2) continue;

            DMatch[] matches = matOfDMatch.toArray();
            if (matches[0].distance < 0.75 * matches[1].distance) { // Lowe's ratio test
                sumDistance += matches[0].distance;
                goodMatches++;
            }
        }

        double avgDistance = goodMatches > 0 ? sumDistance / goodMatches : Double.MAX_VALUE;
        System.out.println("🔍 Match Score (Lower is better): " + avgDistance + " for " + templatePath);

        return avgDistance < MATCH_THRESHOLD;
    }

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

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String testImagePath = "C:\\Users\\030825130\\Downloads\\imagefinder-2022-06-02\\imagefinder\\src\\main\\resources\\templates\\logos\\test_logo1.png";
        boolean isLogo = containsLogo(testImagePath);
        System.out.println("Is the image a logo? " + isLogo);
    }
}
