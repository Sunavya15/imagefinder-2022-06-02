package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.google.gson.Gson;

public class ImageFinderTest {

    public HttpServletRequest request;
    public HttpServletResponse response;
    public StringWriter sw;
    public HttpSession session;

    @Before
    public void setUp() throws Exception {
        // Create mocks for request and response.
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Mockito.when(response.getWriter()).thenReturn(pw);

        // Mock HttpSession.
        session = Mockito.mock(HttpSession.class);
        Mockito.when(request.getSession()).thenReturn(session);

        // Set context path and URI details.
        Mockito.when(request.getContextPath()).thenReturn("/yourApp");
        Mockito.when(request.getRequestURI()).thenReturn("/yourApp/main");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/yourApp/main"));
    }

    @Test
    public void testDoPostWithoutResizing() throws IOException, ServletException {
        // Set servlet path and URL parameter.
        Mockito.when(request.getServletPath()).thenReturn("/main");
        Mockito.when(request.getParameter("url")).thenReturn("https://www.mommysheartfoundation.com/");

        // Create a mock CrawlerService.
        CrawlerService mockCrawlerService = Mockito.mock(CrawlerService.class);

        // Define a list of image URLs, including a duplicate.
        List<String> mockImageUrls = Arrays.asList(
            "https://images.squarespace-cdn.com/content/v1/633529fd9e46d97a3897b132/2b367532-3481-4cd0-99b7-c5b9548f0c9d/heart-hands.jpg?format=2500w",
            "https://images.squarespace-cdn.com/content/v1/633529fd9e46d97a3897b132/1675477709442-3K4OIXL4L1XJ3N6ZEIXZ/image-asset.jpeg?format=500w",
            "https://images.squarespace-cdn.com/content/v1/633529fd9e46d97a3897b132/1675477709442-3K4OIXL4L1XJ3N6ZEIXZ/image-asset.jpeg?format=500w"  // duplicate
        );
        Mockito.when(mockCrawlerService.crawl(Mockito.anyString())).thenReturn(mockImageUrls);

        // Create an instance of ImageFinder that returns our mock CrawlerService.
        ImageFinder imageFinder = new ImageFinder() {
            @Override
            protected CrawlerService createCrawlerService() {
                return mockCrawlerService;
            }
        };

        // Invoke the doPost method.
        imageFinder.doPost(request, response);

        // Build the expected JSON response (a set of unique URLs).
        Set<String> expectedResponseSet = new HashSet<>();
        expectedResponseSet.add("https://images.squarespace-cdn.com/content/v1/633529fd9e46d97a3897b132/2b367532-3481-4cd0-99b7-c5b9548f0c9d/heart-hands.jpg?format=2500w");
        expectedResponseSet.add("https://images.squarespace-cdn.com/content/v1/633529fd9e46d97a3897b132/1675477709442-3K4OIXL4L1XJ3N6ZEIXZ/image-asset.jpeg?format=500w");

        // Deserialize the actual JSON response.
        String actualJson = sw.toString().trim();
        @SuppressWarnings("unchecked")
        Set<String> actualResponseSet = new Gson().fromJson(actualJson, HashSet.class);

        // Assert that the actual response matches the expected unique URLs.
        Assert.assertEquals(expectedResponseSet, actualResponseSet);
    }
}
