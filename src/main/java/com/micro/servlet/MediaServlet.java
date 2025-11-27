package com.micro.servlet;

import com.micro.entity.Media;
import com.micro.listener.AppContextListener;
import com.micro.service.MediaService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = "/api/upload")
@MultipartConfig
public class MediaServlet extends BaseServlet {

    private transient MediaService mediaService;

    @Override
    public void init() throws ServletException {
        this.mediaService = AppContextListener.getComponents(getServletContext()).mediaService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, 405, "Use POST to upload files");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        long userId = requireSessionUser(req, resp);
        if (userId < 0) {
            return;
        }
        Part file = req.getPart("file");
        if (file == null || file.getSize() == 0) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 5001, "File required");
            return;
        }
        if (!isAllowedMime(file.getContentType())) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, 5002, "Unsupported file type");
            return;
        }
        Media media = mediaService.storeFile(userId, file.getSubmittedFileName(), file.getContentType(), file.getSize(), file.getInputStream());
        writeSuccess(resp, Map.of(
                "id", media.getId(),
                "path", media.getPath(),
                "type", media.getType(),
                "size", media.getSize()
        ));
    }

    private boolean isAllowedMime(String mime) {
        if (mime == null) {
            return false;
        }
        return mime.startsWith("image/") || mime.startsWith("video/") || "application/octet-stream".equals(mime);
    }

    private long requireSessionUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, 4016, "Login required");
            return -1;
        }
        return (long) session.getAttribute("userId");
    }
}
