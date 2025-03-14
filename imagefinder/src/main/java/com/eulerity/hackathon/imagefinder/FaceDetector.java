package com.eulerity.hackathon.imagefinder;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class FaceDetector {
    private static final CascadeClassifier faceDetector;

    static {
        try {
            System.load("C:\\Users\\030825130\\Downloads\\opencv\\build\\java\\x64\\opencv_java3416.dll");
            System.out.println("✅ Successfully loaded OpenCV.");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("❌ Error loading OpenCV: " + e.getMessage());
            throw e;
        }

        String classifierPath = "C:\\Users\\030825130\\Downloads\\imagefinder-2022-06-02\\imagefinder\\src\\main\\resources\\haarcascade_frontalface_alt (1).xml";
        faceDetector = new CascadeClassifier(classifierPath);
        if (faceDetector.empty()) {
            System.err.println("❌ Failed to load Haar Cascade classifier from: " + classifierPath);
        } else {
            System.out.println("✅ Successfully loaded Haar Cascade classifier.");
        }
    }

    public static boolean containsFace(BufferedImage image) {
        Mat matImage = bufferedImageToMat(image);
        if (matImage.empty()) {
            System.err.println("❌ Error: Mat is empty after conversion!");
            return false;
        }

        Mat grayImage = new Mat();
        Imgproc.cvtColor(matImage, grayImage, Imgproc.COLOR_BGR2GRAY);

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(grayImage, faceDetections, 1.1, 3, 0, new Size(30, 30), new Size());

        return faceDetections.toArray().length > 0;
    }

    public static void detectAndSaveFaces(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                System.err.println("❌ Error: Could not load image.");
                return;
            }

            Mat matImage = bufferedImageToMat(image);
            if (matImage.empty()) {
                System.err.println("❌ Error: Mat is empty after conversion!");
                return;
            }

            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(matImage, faceDetections);
            Rect[] facesArray = faceDetections.toArray();

            if (facesArray.length > 0) {
                // **Mark entire image with a red border** if people are present.
                Imgproc.rectangle(matImage, new Point(5, 5), 
                        new Point(matImage.cols() - 5, matImage.rows() - 5), 
                        new Scalar(0, 255, 0), // 🟢 Green box
                        5);; //

                // Save the marked image.
                String outputPath = imagePath.replace(".jpg", "_marked.jpg");
                ImageIO.write(matToBufferedImage(matImage), "jpg", new File(outputPath));
                System.out.println("✅ Saved marked image: " + outputPath);
            } else {
                System.out.println("🚫 No people detected in: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("❌ Error processing image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        Mat matRgb = new Mat();
        Imgproc.cvtColor(mat, matRgb, Imgproc.COLOR_BGR2RGB);
        BufferedImage image = new BufferedImage(matRgb.width(), matRgb.height(), BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = new byte[matRgb.width() * matRgb.height() * matRgb.channels()];
        matRgb.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, matRgb.width(), matRgb.height(), data);
        return image;
    }
    public static void main(String[] args) {
    if (args.length == 0) {
        System.err.println("❌ Please provide an image path as an argument.");
        return;
    }

    String imagePath = args[0];
    detectAndSaveFaces(imagePath);
}

}
