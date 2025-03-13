package com.eulerity.hackathon.imagefinder;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawlerService {

    private final ImageExtractorService imageExtractorService = new ImageExtractorService();
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final List<Future<List<String>>> futures = new ArrayList<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private String domain;
    private int maxDepth;
    private static final String LOGO_DIR = "C:\\Users\\030825130\\Downloads\\imagefinder-2022-06-02\\imagefinder\\src\\main\\resources\\templates\\detected_logos";

    public CrawlerService(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public List<String> crawl(String startUrl) {
        Set<String> allImagesSet = ConcurrentHashMap.newKeySet();

        try {
            URL urlObj = new URL(startUrl);
            domain = urlObj.getHost();
        } catch (MalformedURLException e) {
            System.err.println("❌ Invalid start URL: " + e.getMessage());
            return new ArrayList<>();
        }

        activeTasks.incrementAndGet();
        submitCrawlTask(startUrl, maxDepth);

        while (activeTasks.get() > 0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("⚠️ Executor termination interrupted: " + e.getMessage());
        }

        for (Future<List<String>> future : futures) {
            try {
                List<String> images = future.get();
                if (images != null) {
                    allImagesSet.addAll(images);
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("❌ Error retrieving images: " + e.getMessage());
            }
        }

        return new ArrayList<>(allImagesSet);
    }

    private void submitCrawlTask(String url, int depth) {
        if (depth <= 0 || visited.contains(url)) {
            activeTasks.decrementAndGet();
            return;
        }
        visited.add(url);
        Future<List<String>> future = executor.submit(() -> {
            List<String> pageImages = new ArrayList<>();
            System.out.println("🌍 Crawling URL: " + url);

            try {
                Connection connection = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                                "(KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36")
                        .timeout(10000)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);

                Connection.Response response = connection.execute();
                String contentType = response.contentType();
                System.out.println("📄 Content Type: " + contentType);

                if (contentType != null && (contentType.startsWith("text/") ||
                        contentType.startsWith("application/xml") ||
                        (contentType.startsWith("application/") && contentType.endsWith("+xml")))) {

                    Document doc = response.parse();
                    Elements links = doc.select("a[href]");

                    for (Element link : links) {
                        String absHref = link.absUrl("href");
                        if (absHref != null && !absHref.isEmpty()) {
                            try {
                                URL linkUrl = new URL(absHref);
                                String linkHost = linkUrl.getHost();
                                if (linkHost.equalsIgnoreCase(domain) || linkHost.endsWith("." + domain)) {
                                    activeTasks.incrementAndGet();
                                    submitCrawlTask(absHref, depth - 1);
                                }
                            } catch (MalformedURLException e) {
                                System.err.println("⚠️ Invalid link URL: " + e.getMessage());
                            }
                        }
                    }

                    // Extract & Download Favicon
                    String faviconUrl = FaviconExtractor.extractFaviconUrl(url);
                    if (faviconUrl != null) {
                        FaviconExtractor.downloadFavicon(faviconUrl);
                    }

                    // Extract & Detect Logos
                    Elements images = doc.select("img");
                    for (Element img : images) {
                        String imageUrl = img.absUrl("src");
                        if (isLikelyLogo(imageUrl)) {
                            System.out.println("✅ Logo Detected: " + imageUrl);
                            saveUniqueLogo(imageUrl);
                        }
                    }

                    pageImages.addAll(imageExtractorService.extractImages(url));
                } else {
                    System.err.println("⚠️ Skipping unsupported content type: " + contentType + " for URL: " + url);
                }
            } catch (IOException e) {
                System.err.println("❌ Failed to crawl " + url + ": " + e.getMessage());
            } finally {
                activeTasks.decrementAndGet();
            }
            return pageImages;
        });
        futures.add(future);
    }

    /**
     * ✅ Identifies logos based on the image URL.
     */
    private boolean isLikelyLogo(String imageUrl) {
        String lowerUrl = imageUrl.toLowerCase();
        return lowerUrl.contains("logo") || lowerUrl.contains("brandmark") || lowerUrl.contains("company") ||
               lowerUrl.contains("corporate") || lowerUrl.contains("branding");
    }

    /**
     * ✅ Saves unique logos, avoiding duplicates.
     */
    private void saveUniqueLogo(String logoUrl) {
        try {
            URL url = new URL(logoUrl);
            File logoDir = new File(LOGO_DIR);
            if (!logoDir.exists()) logoDir.mkdirs();

            String fileName = "logo_" + hashUrl(logoUrl) + ".png";
            File destinationFile = new File(LOGO_DIR, fileName);

            if (destinationFile.exists()) {
                System.out.println("⚠️ Logo already exists, skipping: " + destinationFile.getAbsolutePath());
                return;
            }

            InputStream in = url.openStream();
            Files.copy(in, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Logo saved: " + destinationFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ Failed to save logo: " + e.getMessage());
        }
    }

    private String hashUrl(String url) {
        return Integer.toHexString(url.hashCode());
    }

    public static void main(String[] args) {
        CrawlerService crawler = new CrawlerService(3);
        List<String> logos = crawler.crawl("http://www.shubhsolutions.com/index.htm#");
        System.out.println("🎯 Logos Collected: " + logos.size());
    }
}
