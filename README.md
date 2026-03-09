# PixelFlow | Distributed Image Processing Pipeline

PixelFlow is a high-performance, event-driven microservices system built with **Spring Boot** and **RabbitMQ**. It demonstrates how to handle CPU-intensive image processing tasks (resizing/watermarking) asynchronously to keep APIs fast and responsive.



---

## 🚀 The Problem & Solution

**The Problem:** Standard REST APIs often time out or lag when processing large image files directly within the request-response cycle. This leads to a poor user experience and server crashes under load.

**The Solution:** PixelFlow offloads the heavy lifting. The API accepts the file and returns a `202 Accepted` status immediately. The actual processing happens in the background via a decoupled worker service, managed by a RabbitMQ message broker.

---

## 🛠️ Tech Stack

* **Backend:** Java 17+, Spring Boot 3.x
* **Messaging:** RabbitMQ (Topic Exchange)
* **Image Lib:** Thumbnailator
* **Database:** PostgreSQL (for job status tracking)
* **DevOps:** Docker, Docker Compose
* **Testing:** JMeter / Locust

---

## 🏗️ Architecture

The system is split into two specialized services:

### 1. Upload Service (The Producer)
* Exposes a `POST /api/v1/images/upload` endpoint.
* Validates image headers and saves the file to a volume.
* Publishes a JSON payload to the `image.processing.exchange`.

### 2. Image Worker (The Consumer)
* Listens to the `image.tasks` queue.
* **Prefetch Count:** Set to `1` to prevent memory exhaustion during high-concurrency tasks.
* Processes images (Resizing, Grayscale, or Watermarking).
* **DLX (Dead Letter Exchange):** Automatically moves failed tasks to a `failed-tasks-queue` for manual retry.

---

## 📊 Reliability & Scaling Features

| Feature | Implementation | Benefit |
| :--- | :--- | :--- |
| **Backpressure Control** | `prefetch=1` | Prevents the worker from crashing when receiving 100+ large images at once. |
| **Fault Tolerance** | Dead Letter Exchange | Ensures no message is lost if processing fails; allows for easy debugging. |
| **Horizontal Scaling** | Competing Consumers | Spin up 5 worker containers to process the queue 5x faster without changing code. |
| **Idempotency** | Job ID Tracking | Prevents processing the same image twice if a network glitch occurs. |

---

## 🚦 Getting Started

### Prerequisites
* Docker Desktop
* Maven 3.8+

### Quick Start
1.  **Clone the repo:**
    ```bash
    git clone [https://github.com/yourusername/pixelflow.git](https://github.com/yourusername/pixelflow.git)
    ```
2.  **Launch Infrastructure:**
    ```bash
    docker-compose up -d
    ```
3.  **Monitor the Broker:**
    Access the RabbitMQ Management UI at `http://localhost:15672` (Username: `guest`, Password: `guest`).



---

## 📈 Load Test Results
Under a stress test of **500 concurrent uploads**, the system maintained:
* **API Response Time:** < 40ms (95th percentile)
* **Worker Stability:** 100% success rate with 0 `OutOfMemory` errors.
* **Queue Recovery:** Fully drained 1GB of image data in 3.5 minutes using 2 worker instances.

---

## 💼 For Upwork Clients
This project serves as a technical demonstration of:
1.  **Microservices Communication:** Using RabbitMQ Topic Exchanges.
2.  **System Resilience:** Handling failures with Dead Letter Queues.
3.  **Resource Optimization:** Efficient JVM memory management during heavy I/O.




**How to use it**

Launch the service: Run docker-compose up -d in the directory where you saved the file.
Access the Dashboard: Open your browser and go to http://localhost:15672.
Log in: Use the credentials defined in the environment section (default: pixelflow / pixelflow).



# This creates 10 parallel "workers" on your machine sending requests as fast as possible
seq 1000 | xargs -I % -P 10 curl --location 'localhost:8080/image-processing/upload' \
--form 'images=@"/Users/ng/Documents/3840_compress.jpeg"' \
--form 'images=@"/Users/ng/Documents/4978202. Blue (Arizona Blue).jpg"' \
--form 'images=@"/Users/ng/Documents/AnikaAadhar.jpg"' \
--form 'images=@"/Users/ng/Documents/AnikaPanCard.jpg"' \
--form 'images=@"/Users/ng/Documents/Gemini_Generated_Image_n7cludn7cludn7cl.jpg"' \
--form 'images=@"/Users/ng/Documents/banner-wmc.png"'
