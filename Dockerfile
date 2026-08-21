# ProPlacement Compiler Server - Multi-Language Sandbox
FROM ubuntu:22.04

# Prevent interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Update package lists and install Java 21, GCC, G++, Python 3, Node.js
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jdk-headless \
    build-essential \
    gcc \
    g++ \
    python3 \
    nodejs \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy backend files into container
COPY backend/JavaCompilerServer.java ./

# Create temporary execution directory for compiler sandboxes
RUN mkdir -p temp_exec

# Pre-compile the Java compiler server
RUN javac -encoding UTF-8 JavaCompilerServer.java

# Dynamic port binding (Render/Railway/Fly.io inject PORT)
ENV PORT=8080
EXPOSE 8080

# Run the server
CMD ["java", "-Dfile.encoding=UTF-8", "JavaCompilerServer"]
