package com.eulerity.hackathon.imagefinder;


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class OpenCVUtils {

    /**
     * Converts a BufferedImage to an OpenCV Mat.
     *
     * @param bi the BufferedImage to convert.
     * @return a Mat representing the image.
     * @throws Exception if an error occurs during conversion.
     */
    public static Mat bufferedImageToMat(BufferedImage bi) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Write the BufferedImage to a byte array (using JPEG format here).
        ImageIO.write(bi, "jpg", baos);
        baos.flush();
        byte[] imageBytes = baos.toByteArray();
        baos.close();

        // Convert the byte array into an OpenCV Mat.
        MatOfByte mob = new MatOfByte(imageBytes);
        Mat mat = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_UNCHANGED);
        return mat;
    }
}
