##### Image Finder Web Scraper - Eulerity Take-Home Challenge
## Overview
This project is a web crawler and image extractor that scans a given URL, extracts images, classifies them, and displays the results. The application utilizes Selenium, OpenCV, and JSoup for image extraction, processing, and face detection.

## Features Implemented
✅ Web Scraping & Image Extraction
Implemented a web crawler using JSoup and Selenium to scrape images from web pages.

## Extracts images from:
<img> tags
## Background images (style="background-image:url(...)")
<source> tags (srcset attribute for responsive images)
✅ Multithreaded Web Crawler
Designed the CrawlerService to crawl multiple pages concurrently for better performance.
Uses an executor service to manage crawling threads efficiently.
✅ Image Resizing
Resized images to a standardized dimension (350x350) using Java’s BufferedImage API.
Ensured images retain quality while resizing.
✅ Image Categorization
Developed a classification system to identify:
Favicons (Very small images, ≤ 32x32 pixels)
Logos (Square-shaped images, typically small)
Images Containing People (Implemented face detection)
✅ Face Detection
Implemented face detection using OpenCV’s Haar Cascade Classifier (haarcascade_frontalface_alt.xml).
Used BufferedImage to Mat conversion for OpenCV image processing.
Detected human faces in images to categorize people-containing images.
✅ Dynamic Web Interface (Frontend)
Improved UI with Bootstrap for a modern, interactive look.

## Added:
Loading spinner while fetching images.
Gallery layout for displaying extracted images.
Popup modal when clicking images for better user interaction.
✅ Deployment on Jetty
Packaged the project as a WAR file.
Deployed on Jetty Server for testing.
Configured Jetty Start.ini to load OpenCV dynamically.
✅ Testing
Implemented JUnit tests in ImageFinderTest.java.
Used Mockito to mock API requests and responses.
Verified that extracted images are correctly fetched and classified.
✅ Git Integration
Set up Git version control for tracking changes.
Added a proper .gitignore to exclude unnecessary files.
Committed the latest code updates.
Technologies Used
Backend: Java, Servlet API, JSoup, Selenium
Frontend: HTML, CSS (Bootstrap), JavaScript (Fetch API)

### Libraries:
OpenCV (for face detection & image processing)
Selenium (for dynamic web page interaction)
JSoup (for parsing HTML & extracting image links)
Testing Frameworks: JUnit, Mockito
Build & Deployment: Maven, Jetty Server
Setup & Deployment Guide
Prerequisites
Install Java 8+
Install Maven
Install Jetty Server
Download OpenCV 3.4.16 and configure the opencv_java3416.dll
How to Run
Clone the Repository:

# command:
Copy
Edit
git clone <repository-url>
cd imagefinder
Build & Package the Project:

# command:
Copy
Edit
mvn clean package
Run on Jetty Server:

# command:
Copy
Edit
mvn jetty:run
Access the Web Interface: Open http://localhost:8080 in your browser.

## Future Improvements
Enhance face detection using DNN-based models (OpenCV DNN).
Improve logo detection using custom ML models.
Optimize the crawling process to prevent duplicate requests.
Store processed images in a database instead of saving locally.
