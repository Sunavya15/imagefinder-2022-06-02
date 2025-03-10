# Image Finder Web Scraper - Eulerity Take-Home Challenge
## Overview
This project is a web crawler and image extractor that scans a given URL, extracts images, classifies them, and displays the results. The application utilizes Selenium, OpenCV, and JSoup for image extraction, processing, and face detection.

## Features Implemented

* Web Scraping & Image Extraction
Implemented a web crawler using JSoup and Selenium to scrape images from web pages.

## Extracts images from img tags 
## Background images (style="background-image:url(...)") <source> tags (srcset attribute for responsive images)

* Multithreaded Web Crawler
Designed the CrawlerService to crawl multiple pages concurrently for better performance.
Uses an executor service to manage crawling threads efficiently.
* Image Resizing
Resized images to a standardized dimension (350x350) using Java’s BufferedImage API.
Ensured images retain quality while resizing.

### Image Categorization

Developed a classification system to identify:

#### Favicons (Very small images, ≤ 32x32 pixels)
#### Logos (Square-shaped images, typically small)
#### Images Containing People (Implemented face detection)

### Face Detection

* Implemented face detection using OpenCV’s Haar Cascade Classifier (haarcascade_frontalface_alt.xml).

* Used BufferedImage to Mat conversion for OpenCV image processing.

* Detected human faces in images to categorize people-containing images.

* Dynamic Web Interface (Frontend)

* Improved UI with Bootstrap for a modern, interactive look.

* Gallery layout for displaying extracted images.

### Deployment on Jetty

Packaged the project as a WAR file.
Deployed on Jetty Server for testing.
Configured Jetty Start.ini to load OpenCV dynamically.

### Testing

Implemented JUnit tests in ImageFinderTest.java.
Used Mockito to mock API requests and responses.
Verified that extracted images are correctly fetched and classified.

### Git Integration

Set up Git version control for tracking changes.
Added a proper .gitignore to exclude unnecessary files.
Committed the latest code updates.
Technologies Used
Backend: Java, Servlet API, JSoup, Selenium
Frontend: HTML, CSS, JavaScript

### Libraries:

* OpenCV (for face detection & image processing)
* Selenium (for dynamic web page interaction)
* JSoup (for parsing HTML & extracting image links)
* Testing Frameworks: JUnit, Mockito
* Build & Deployment: Maven, Jetty Server
* Setup & Deployment Guide

### Prerequisites:
#### Install Java 8+ 
* link: https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html#license-lightbox
#### Install Maven
* link: https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip
#### Install Jetty Server 
* link: https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.4.56.v20240826/jetty-distribution-9.4.56.v20240826.zip
#### Download OpenCV 3.4.16 and configure the opencv_java3416.dll 
* link: https://sourceforge.net/projects/opencvlibrary/files/3.4.16/opencv-3.4.16-vc14_vc15.exe/download

## Steps to Run the Image Finder Application

* This project is a web-based tool to extract **favicons, logos, and images** containing people from a given URL.

## 🚀 How to Run

### **1️⃣ Clone the Repository**

git clone <repository-url>

### 2️⃣ Navigate to the Project Directory

cd imagefinder

### 3️⃣ Clean Any Previous Builds

mvn clean

### 4️⃣ Install Dependencies and Build the Project

mvn install

### 5️⃣ Run the Application Using Jetty

mvn jetty:run

### Once the application is running, open:

http://localhost:8080/

## Testing the API in Postman

* Use Postman or cURL to test the API.

### POST Request:

#### 1. URL: http://localhost:8080/main

#### 2. Headers:

* Content-Type: application/x-www-form-urlencoded

* Body (form-data or x-www-form-urlencoded):

* Key: url
* Value: https://www.example.com

#### Expected Response
* json

* [
    * {"url": "https://www.example.com/logo.png", "type": "logo"},
    * {"url": "https://www.example.com/favicon.ico", "type": "favicon"},
    * {"url": "https://www.example.com/image1.jpg", "type": "people"}
* ]

## Build & Package the Project:

## Running the Project
Here we will detail how to setup and run this project so you may get started, as well as the requirements needed to do so.

### Requirements

Before beginning, make sure you have the following installed and ready to use

Maven 3.5 or higher
Java 8
Exact version, NOT Java 9+ - the build will fail with a newer version of Java
Setup
To start, open a terminal window and navigate to wherever you unzipped to the root directory imagefinder. To build the project, run the command:

### mvn package

If all goes well you should see some lines that end with "BUILD SUCCESS". When you build your project, maven should build it in the target directory. To clear this, you may run the command:

### mvn clean

To run the project, use the following command to start the server:

### mvn clean package jetty:run

You should see a line at the bottom that says "Started Jetty Server". Now, if you enter localhost:8080 into your browser, you should see the index.html welcome page! If all has gone well to this point, you're ready to begin!

## Future Improvements
Enhance face detection using DNN-based models (OpenCV DNN)(haarcascade_frontalface_alt).
Improve logo detection using custom ML models.
Optimize the crawling process to prevent duplicate requests.
Store processed images in a database instead of saving locally.
