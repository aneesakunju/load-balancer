package org.example;

interface LoadBalancer {
    String serveRequest(Request request);
    String getStatus();
    void incrementRequestCount(String name);
    void decrementRequestCount(String name);
}

