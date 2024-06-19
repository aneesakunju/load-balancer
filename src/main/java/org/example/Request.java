package org.example;

public class Request {

    private String id;
    private String requestType;

    public Request(String id, String requestType) {
        this.id = id;
        this.requestType = requestType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    @Override
    public String toString() {
        return "Request [id=" + id + ", requestType=" + requestType + "]";
    }

}
