PixelFlow: Distributed Asynchronous Image Processing System
PixelFlow is a high-performance, event-driven microservices architecture built with Spring Boot and RabbitMQ. It solves the common problem of API timeouts during CPU-intensive tasks by offloading image processing to background workers.

🏗️ Architecture Overview
The system consists of two decoupled services communicating via a RabbitMQ Topic Exchange:

Upload Service (Producer): A RESTful API that accepts image uploads, stores them, and publishes a "processing task" message. Returns a 202 Accepted response instantly.

Image Worker (Consumer): A scalable background service that listens for tasks, performs image manipulation (resizing/watermarking), and handles errors gracefully.

Key Technical Patterns
Asynchronous Processing: Decouples heavy I/O and CPU tasks from the user request-response cycle.

Competing Consumers: Multiple instances of the Worker service can be spun up to drain the queue faster during high load.

Dead Letter Exchange (DLX): Automatic routing of corrupted or failed image tasks to a separate "quarantine" queue for inspection.

Backpressure Management: Configured with a prefetch count of 1 to ensure workers aren't overwhelmed by large files.

🛠️ Tech Stack
Framework: Spring Boot 3.x

Message Broker: RabbitMQ

Image Processing: Thumbnailator (Java)

Containerization: Docker & Docker Compose

Testing: JMeter / Locust (Load Testing)

Build Tool: Maven

🚀 Getting Started
Prerequisites
Docker & Docker Compose

JDK 17 or higher

Running the System
Clone the repository:

Bash
git clone https://github.com/your-username/pixelflow.git
cd pixelflow
Start Infrastructure (RabbitMQ & DB):

Bash
docker-compose up -d
Run the Services:

Start the upload-service on port 8080.

Start one or more instances of image-worker.

Access RabbitMQ Dashboard:
Visit http://localhost:15672 (Guest/Guest) to monitor message flow in real-time.

📊 Performance & Load Testing
To demonstrate production readiness, this system was tested under a "spike" load:

Baseline: 50ms average API response time.

Stress Test: 500 concurrent image uploads.

Observation: The upload-service remained responsive while the image-worker processed the backlog at a steady, safe rate without memory spikes.

Note: We implemented a Prefetch Count of 1 to ensure that even with massive 4K images, the JVM never encounters an OutOfMemoryError.
