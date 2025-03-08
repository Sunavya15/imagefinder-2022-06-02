package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
// import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet {
    private static final long serialVersionUID = 1L;
    protected static final Gson GSON = new Gson();

    /**
     * Factory method to create a new CrawlerService instance.
     * In production, this returns a fresh instance per request.
     * In tests, you can override this method to return a mock.
     */
    protected CrawlerService createCrawlerService() {
        return new CrawlerService(2);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        String url = req.getParameter("url");

        System.out.println("Received request for: " + url);

        // Use the crawler to get image URLs and remove duplicates.
        Set<String> uniqueImageUrls = new LinkedHashSet<>(createCrawlerService().crawl(url));


        // Return the unique URLs as JSON.
        resp.getWriter().print(GSON.toJson(uniqueImageUrls));
    }
}
