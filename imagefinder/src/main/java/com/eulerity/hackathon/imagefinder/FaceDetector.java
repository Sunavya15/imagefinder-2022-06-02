package com.eulerity.hackathon.imagefinder;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

public class FaceDetector {
    private static final CascadeClassifier faceDetector;

    static {
        try {
            // Explicitly load the OpenCV native library using its full absolute path.
            System.load("C:\\Users\\030825130\\Downloads\\opencv\\build\\java\\x64\\opencv_java3416.dll");
            System.out.println("Successfully loaded opencv_java3416.dll in FaceDetector.");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Error loading OpenCV DLL in FaceDetector: " + e.getMessage());
            throw e;
        }
        
        // Load the Haar Cascade classifier XML file from resources.
        // Make sure that haarcascade_frontalface_alt.xml is in src/main/resources/
        String classifierPath = FaceDetector.class.getResource("/haarcascade_frontalface_alt.xml").getPath();
        faceDetector = new CascadeClassifier(classifierPath);
        if (faceDetector.empty()) {
            System.err.println("Failed to load Haar Cascade classifier from: " + classifierPath);
        } else {
            System.out.println("Successfully loaded Haar Cascade classifier from: " + classifierPath);
        }
    }

    /**
     * Detects whether the provided BufferedImage contains at least one face.
     *
     * @param image the BufferedImage to check.
     * @return true if one or more faces are detected, false otherwise.
     */
    public static boolean containsFace(java.awt.image.BufferedImage image) {
        try {
            // Convert the BufferedImage to an OpenCV Mat using our utility method.
            Mat matImage = OpenCVUtils.bufferedImageToMat(image);
            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(matImage, faceDetections);
            Rect[] facesArray = faceDetections.toArray();
            System.out.println("Detected " + facesArray.length + " face(s).");
            return facesArray.length > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
