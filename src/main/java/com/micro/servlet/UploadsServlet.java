package com.micro.servlet;

import com.micro.listener.AppContextListener;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Serves uploaded files from the configured storage directory.
 * Maps to /static/uploads/* to intercept requests for user-uploaded content.
 */
@WebServlet(urlPatterns = "/static/uploads/*")
public class UploadsServlet extends HttpServlet {

    private String storageRoot;

    @Override
    public void init() throws ServletException {
        this.storageRoot = AppContextListener.getFileStoragePath(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Remove leading slash
        String relativePath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        // Decode URL (e.g. spaces, special chars)
        relativePath = URLDecoder.decode(relativePath, StandardCharsets.UTF_8);

        // Prevent directory traversal attacks
        if (relativePath.contains("..")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File file = new File(storageRoot, relativePath);
        if (!file.exists() || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Set content type
        String contentType = getServletContext().getMimeType(file.getName());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        resp.setContentType(contentType);
        resp.setContentLengthLong(file.length());

        // Stream file content
        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
