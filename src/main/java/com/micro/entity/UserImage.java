package com.micro.entity;

public class UserImage {
    private byte[] data;
    private String contentType;

    public UserImage(byte[] data, String contentType) {
        this.data = data;
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }
}
