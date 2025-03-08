package com.eulerity.hackathon.imagefinder;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * A utility class for resizing images in Java.
 */
public class ImageResizer {

    static {
        try {
            // Load the OpenCV native library using its full absolute path.
            System.load("C:\\Users\\030825130\\Downloads\\opencv\\build\\java\\x64\\opencv_java3416.dll");
            System.out.println("Successfully loaded opencv_java3416.dll");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Error loading opencv_java3416.dll: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Resizes an image to the specified width and height.
     *
     * @param inputImagePath  Path to the original image file.
     * @param outputImagePath Path where the resized image will be saved.
     * @param scaledWidth     Desired width (in pixels).
     * @param scaledHeight    Desired height (in pixels).
     * @throws Exception if an error occurs during reading or writing.
     */
    public static void resize(String inputImagePath,
                              String outputImagePath,
                              int scaledWidth,
                              int scaledHeight) throws Exception {

        // 1) Read the original image from disk.
        BufferedImage originalImage = ImageIO.read(new File(inputImagePath));
        if (originalImage == null) {
            throw new IllegalArgumentException("Could not read input file: " + inputImagePath);
        }

        // 2) Create a new BufferedImage to draw the scaled image.
        BufferedImage resizedImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);

        // 3) Draw the original image, scaled, onto the new BufferedImage.
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH),
                      0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        // 4) Write the resized image to the output path (using "jpg" as an example).
        ImageIO.write(resizedImage, "jpg", new File(outputImagePath));
    }
}
