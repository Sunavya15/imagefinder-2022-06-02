package com.eulerity.hackathon.imagefinder;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import com.eulerity.hackathon.imagefinder.FaceDetector;

public class ImageExtractorService {

    /**
     * Extracts image URLs from the given page, downloads each image,
     * classifies it based on the original image dimensions, resizes it,
     * and returns the public URLs for the processed images.
     *
     * @param url The URL of the page to extract images from.
     * @return A list of public URLs for the processed images.
     */
    private static final HashSet<String> processedHashes = new HashSet<>();

public List<String> extractImages(String url) {
    Set<String> imageUrls = new LinkedHashSet<>();
    System.out.println("Extracting images from URL: " + url);

    // Set up WebDriver
    System.setProperty("webdriver.chrome.driver",
            "C:\\Users\\030825130\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--disable-gpu");
    WebDriver driver = new ChromeDriver(options);

    try {
        driver.get(url);
        dismissCookieBannerIfPresent(driver);
        scrollVerticallyUntilNoNewContent(driver);
        clickCarouselArrows(driver, "button[aria-label='Next']");
        horizontalScrollContainer(driver, "div.horizontal-scroll-container");

        WebDriverWait wait = new WebDriverWait(driver, 60);
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("img")));

        extractFromImgTags(driver, imageUrls);
        extractFromBackgroundImages(driver, imageUrls);
        extractFromSourceTags(driver, imageUrls);
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        driver.quit();
    }

    // Process images (Check Hash Uniqueness)
    Set<String> uniqueProcessedImages = new LinkedHashSet<>();

    for (String imageUrl : imageUrls) {
        String processedPath = downloadClassifyAndResizeImage(imageUrl, 350, 350);
        if (processedPath != null) {
            try {
                String imageHash = computeFileHash(new File("src/main/webapp" + processedPath));
                if (!processedHashes.contains(imageHash)) {
                    processedHashes.add(imageHash);
                    uniqueProcessedImages.add(processedPath);
                } else {
                    System.out.println("🚫 Duplicate detected, skipping: " + imageUrl);
                }
            } catch (Exception e) {
                System.err.println("⚠ Error computing hash for " + processedPath + ": " + e.getMessage());
            }
        }
    }

    return new ArrayList<>(uniqueProcessedImages);
}

    /**
     * Normalizes a URL by trimming whitespace and removing a trailing slash.
     *
     * @param urlStr the URL to normalize.
     * @return the normalized URL.
     */
    private String normalizeUrl(String urlStr) {
        if (urlStr == null) return null;
        String trimmed = urlStr.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private void dismissCookieBannerIfPresent(WebDriver driver) {
        try {
            WebElement acceptBtn = driver.findElement(By.cssSelector("button.accept-cookies"));
            if (acceptBtn.isDisplayed()) {
                acceptBtn.click();
            }
        } catch (NoSuchElementException e) {
            // No cookie banner found.
        }
    }

    private void scrollVerticallyUntilNoNewContent(WebDriver driver) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long lastHeight = (long) js.executeScript("return document.body.scrollHeight");
        while (true) {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(3000); // Wait for lazy loading.
            long newHeight = (long) js.executeScript("return document.body.scrollHeight");
            if (newHeight == lastHeight) {
                break;
            }
            lastHeight = newHeight;
        }
    }

    private void clickCarouselArrows(WebDriver driver, String arrowButtonSelector) throws InterruptedException {
        int maxClicks = 15;       // Maximum number of clicks to avoid endless looping.
        int stableLimit = 3;      // Stop if the first visible image doesn't change for several clicks.
        int stableCount = 0;
        int clicks = 0;

        List<WebElement> imgs = driver.findElements(By.tagName("img"));
        String previousFirstImg = (imgs.size() > 0) ? imgs.get(0).getAttribute("src") : "";

        long startTime = System.currentTimeMillis();
        long maxDuration = 2 * 60 * 1000; // 2 minutes in milliseconds.

        while (clicks < maxClicks && stableCount < stableLimit &&
                (System.currentTimeMillis() - startTime) < maxDuration) {
            try {
                WebElement arrowButton = driver.findElement(By.cssSelector(arrowButtonSelector));
                if (!arrowButton.isDisplayed() || !arrowButton.isEnabled()) {
                    break;
                }
                try {
                    arrowButton.click();
                } catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", arrowButton);
                }
                Thread.sleep(2000); // Wait for carousel to load images.
                List<WebElement> newImgs = driver.findElements(By.tagName("img"));
                String currentFirstImg = (newImgs.size() > 0) ? newImgs.get(0).getAttribute("src") : "";
                if (currentFirstImg.equals(previousFirstImg)) {
                    stableCount++;
                } else {
                    stableCount = 0;
                    previousFirstImg = currentFirstImg;
                }
                clicks++;
            } catch (NoSuchElementException e) {
                break;
            }
        }
    }

    private void horizontalScrollContainer(WebDriver driver, String containerSelector) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        try {
            WebElement container = driver.findElement(By.cssSelector(containerSelector));
            long lastScrollLeft = (long) js.executeScript("return arguments[0].scrollLeft;", container);
            while (true) {
                js.executeScript("arguments[0].scrollBy(500, 0);", container);
                Thread.sleep(2000);
                long newScrollLeft = (long) js.executeScript("return arguments[0].scrollLeft;", container);
                if (newScrollLeft == lastScrollLeft) {
                    break;
                }
                lastScrollLeft = newScrollLeft;
            }
        } catch (NoSuchElementException e) {
            // No horizontally scrollable container found.
        }
    }

    private void extractFromImgTags(WebDriver driver, Set<String> imageUrls) {
        List<WebElement> imgElements = driver.findElements(By.tagName("img"));
        for (WebElement img : imgElements) {
            String imgUrl = img.getAttribute("src");
            if (imgUrl != null && !imgUrl.isEmpty()) {
                imageUrls.add(normalizeUrl(imgUrl));
            }
        }
    }

    private void extractFromBackgroundImages(WebDriver driver, Set<String> imageUrls) {
        List<WebElement> bgElements = driver.findElements(By.cssSelector("[style*='background-image']"));
        Pattern pattern = Pattern.compile("url\\([\"']?(.*?)[\"']?\\)");
        for (WebElement elem : bgElements) {
            String style = elem.getAttribute("style");
            Matcher matcher = pattern.matcher(style);
            if (matcher.find()) {
                String bgUrl = matcher.group(1);
                if (bgUrl != null && !bgUrl.isEmpty()) {
                    imageUrls.add(normalizeUrl(bgUrl));
                }
            }
        }
    }

    private void extractFromSourceTags(WebDriver driver, Set<String> imageUrls) {
        List<WebElement> sourceElements = driver.findElements(By.tagName("source"));
        for (WebElement source : sourceElements) {
            String srcSet = source.getAttribute("srcset");
            if (srcSet != null && !srcSet.isEmpty()) {
                String[] urls = srcSet.split(",");
                if (urls.length > 0) {
                    String firstUrl = urls[0].trim().split(" ")[0];
                    imageUrls.add(normalizeUrl(firstUrl));
                }
            }
        }
    }

    /**
     * Downloads an image from the given URL, classifies it using its original dimensions,
     * then resizes it, and saves it to "src/main/webapp/resizedImages" using a deterministic filename
     * based on the image's MD5 hash.
     *
     * Returns the public URL (e.g., "/resizedImages/resized_<hash>.jpg")
     * so the frontend can display it.
     *
     * @param imageUrl The URL of the image to process.
     * @param width The desired width for resizing.
     * @param height The desired height for resizing.
     * @return The public URL of the processed image, or null if processing failed.
     */
    private String downloadClassifyAndResizeImage(String imageUrl, int width, int height) {
        try {
            File processedDir = new File("src/main/webapp/processedImages");
            if (!processedDir.exists()) {
                processedDir.mkdirs();
            }
    
            File resizedDir = new File("src/main/webapp/resizedImages");
            if (!resizedDir.exists()) {
                resizedDir.mkdirs();
            }
    
            // **Download image to `processedImages/`**
            URL url = new URL(imageUrl);
            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
            File tempFile = new File(processedDir, fileName);
            try (InputStream in = url.openStream()) {
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
    
            BufferedImage img = ImageIO.read(tempFile);
            if (img == null) return null;
    
            File processedFile = tempFile;
            boolean containsPeople = FaceDetector.containsFace(img);
            boolean containsLogo = LogoDetector.containsLogo(tempFile.getAbsolutePath());
    
            // **Mark image if it contains people**
            if (containsPeople) {
                System.out.println("✅ People detected in: " + imageUrl);
                FaceDetector.detectAndSaveFaces(tempFile.getAbsolutePath());
                processedFile = new File(tempFile.getAbsolutePath().replace(".jpg", "_marked.jpg"));
            }
    
            // **Detect logos without modifying image**
            if (containsLogo) {
                System.out.println("✅ Logo detected in: " + imageUrl);
            }
    
            // **Extract favicon from the website's root domain**
            String pageDomain = new URL(imageUrl).getProtocol() + "://" + new URL(imageUrl).getHost();
            String faviconUrl = FaviconExtractor.extractFaviconUrl(pageDomain);
    
            if (faviconUrl != null) {
                File faviconFile = FaviconExtractor.downloadFavicon(faviconUrl);
                if (faviconFile != null && LogoDetector.containsLogo(faviconFile.getAbsolutePath())) {
                    System.out.println("✅ Favicon is a logo: " + faviconUrl);
                }
            }
    
            // **Compute MD5 hash for duplicate detection**
            String fileHash = computeFileHash(processedFile);
            String resizedFileName = "resized_" + fileHash + ".jpg";
            File resizedFile = new File(resizedDir, resizedFileName);
    
            // **Skip re-processing if already exists**
            if (resizedFile.exists()) {
                processedFile.delete();
                return "/resizedImages/" + resizedFileName;
            }
    
            // **Resize image while preserving color**
            ImageResizer.resize(processedFile.getAbsolutePath(), resizedFile.getAbsolutePath(), width, height);
    
            // **Delete temporary processed image**
            processedFile.delete();
    
            return "/resizedImages/" + resizedFileName;
        } catch (Exception e) {
            System.err.println("❌ Error processing image: " + imageUrl + " - " + e.getMessage());
            return null;
        }
    }
    
    

    /**
     * Computes the MD5 hash of a file.
     *
     * @param file the file to hash.
     * @return the MD5 hash as a 32-character hexadecimal string.
     * @throws Exception if an error occurs during reading or digest computation.
     */
    private String computeFileHash(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        byte[] digest = md.digest(fileBytes);
        BigInteger no = new BigInteger(1, digest);
        String hashText = no.toString(16);
        while (hashText.length() < 32) {
            hashText = "0" + hashText;
        }
        return hashText;
    }

    /**
     * Classifies an image based on its dimensions and (stubbed) face detection.
     * Returns a category if the image is a favicon, logo, or contains people.
     * If the image does not match any special criteria, returns null.
     *
     * @param imageFile the image file to classify.
     * @return the ImageCategory of the image, or null if no special classification.
     */
    private ImageCategory classifyImage(File imageFile) {
        try {
            BufferedImage img = ImageIO.read(imageFile);
            if (img == null) {
                return null;
            }
            int width = img.getWidth();
            int height = img.getHeight();

            // Favicon: very small image (typically <= 32x32).
            if (width <= 32 && height <= 32) {
                return ImageCategory.FAVICON;
            }

            // Logo: roughly square and small (e.g., less than 200px on the long side).
            if (Math.abs(width - height) < 20 && width < 200 && height < 200) {
                return ImageCategory.LOGO;
            }

            // Contains People: stubbed face detection.
            if (FaceDetector.containsFace(img)) {
                return ImageCategory.CONTAINS_PEOPLE;
            }

            // No special category.
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Stub for face detection.
     * Replace with actual logic using OpenCV or another library as needed.
     *
     * @param img the BufferedImage to check.
     * @return false (stub implementation).
     */
    // private boolean containsFace(BufferedImage img) {
    //     // For real face detection, integrate OpenCV's CascadeClassifier or similar.
    //     return false;
    // }

    public void clearProcessedImages() {
        deleteDirectoryContents(new File("src/main/webapp/processedImages"));
        deleteDirectoryContents(new File("src/main/webapp/resizedImages"));
        System.out.println("✅ Cleared old images from processedImages/ and resizedImages/");
    }
    
    private void deleteDirectoryContents(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        }
    }
    
}

