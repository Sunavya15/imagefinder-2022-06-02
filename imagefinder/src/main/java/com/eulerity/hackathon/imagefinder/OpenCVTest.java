package com.eulerity.hackathon.imagefinder;



import org.opencv.core.Core;

public class OpenCVTest {
    public static void main(String[] args) {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV Version: " + Core.VERSION);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Error loading OpenCV: " + e.getMessage());
        }
    }
}
