package com.micro.entity;

public record UserImage(byte[] data, String contentType) {
    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }
}
