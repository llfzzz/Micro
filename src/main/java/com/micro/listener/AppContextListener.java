package com.micro.listener;

import com.micro.dao.CommentDao;
import com.micro.dao.FollowDao;
import com.micro.dao.LikeDao;
import com.micro.dao.MediaDao;
import com.micro.dao.PostDao;
import com.micro.dao.UserDao;
import com.micro.dao.impl.CommentDaoImpl;
import com.micro.dao.impl.FollowDaoImpl;
import com.micro.dao.impl.LikeDaoImpl;
import com.micro.dao.impl.MediaDaoImpl;
import com.micro.dao.impl.PostDaoImpl;
import com.micro.dao.impl.UserDaoImpl;
import com.micro.service.AdminService;
import com.micro.service.AuthService;
import com.micro.service.CommentService;
import com.micro.service.FollowService;
import com.micro.service.MediaService;
import com.micro.service.PostService;
import com.micro.service.UserService;
import com.micro.service.impl.AdminServiceImpl;
import com.micro.service.impl.AuthServiceImpl;
import com.micro.service.impl.CommentServiceImpl;
import com.micro.service.impl.FollowServiceImpl;
import com.micro.service.impl.MediaServiceImpl;
import com.micro.service.impl.PostServiceImpl;
import com.micro.service.impl.UserServiceImpl;
import com.micro.util.PropertyUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import java.io.File;
import java.util.Properties;

/**
 * Bootstraps shared resources (DataSource, file storage path) for the Micro application.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    public static final String DATA_SOURCE_ATTR = "MICRO_DATA_SOURCE";
    public static final String APP_PROPERTIES_ATTR = "MICRO_APP_PROPERTIES";
    public static final String FILE_STORAGE_ATTR = "MICRO_FILE_STORAGE";
    public static final String COMPONENTS_ATTR = "MICRO_COMPONENTS";

    private static final Logger LOGGER = LoggerFactory.getLogger(AppContextListener.class);

    private HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        Properties properties = PropertyUtil.load("application.properties");
        context.setAttribute(APP_PROPERTIES_ATTR, properties);

        this.dataSource = buildDataSource(properties);
        context.setAttribute(DATA_SOURCE_ATTR, dataSource);

        String storagePath = properties.getProperty("file.storage.path", System.getProperty("java.io.tmpdir") + "/micro/uploads");
        File storageDir = new File(storagePath).getAbsoluteFile();
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw new IllegalStateException("Unable to create storage directory at " + storageDir);
        }
        context.setAttribute(FILE_STORAGE_ATTR, storageDir.getAbsolutePath());

        Components components = buildComponents(dataSource, storageDir.getAbsolutePath(), properties);
        context.setAttribute(COMPONENTS_ATTR, components);
        LOGGER.info("Micro backend initialized. Storage path: {}", storageDir.getAbsolutePath());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private Components buildComponents(DataSource dataSource, String storagePath, Properties props) {
        UserDao userDao = new UserDaoImpl(dataSource);
        PostDao postDao = new PostDaoImpl(dataSource);
        CommentDao commentDao = new CommentDaoImpl(dataSource);
        MediaDao mediaDao = new MediaDaoImpl(dataSource);
        LikeDao likeDao = new LikeDaoImpl(dataSource);
        FollowDao followDao = new FollowDaoImpl(dataSource);

        int workFactor = Integer.parseInt(props.getProperty("bcrypt.workFactor", "12"));

        UserService userService = new UserServiceImpl(userDao, workFactor);
        AuthService authService = new AuthServiceImpl(userDao, workFactor);
        PostService postService = new PostServiceImpl(postDao, likeDao);
        CommentService commentService = new CommentServiceImpl(commentDao, postDao);
        MediaService mediaService = new MediaServiceImpl(mediaDao, storagePath);
        AdminService adminService = new AdminServiceImpl(userDao, postDao, commentDao);
        FollowService followService = new FollowServiceImpl(followDao);

        return new Components(userService, authService, postService, commentService, mediaService, adminService, followDao, followService);
    }

    private HikariDataSource buildDataSource(Properties properties) {
        String jdbcUrl = properties.getProperty("db.url");
        String username = properties.getProperty("db.user");
        String password = properties.getProperty("db.password", "");
        String maxPool = properties.getProperty("db.pool.max", "10");

        if (jdbcUrl == null || username == null) {
            throw new IllegalStateException("Database url/user must be configured");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL driver not found", e);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        if (password != null) {
            config.setPassword(password);
        }
        config.setMaximumPoolSize(Integer.parseInt(maxPool));
        config.setPoolName("MicroHikariPool");
        config.setAutoCommit(true);
        return new HikariDataSource(config);
    }

    public static DataSource getDataSource(ServletContext context) {
        Object ds = context.getAttribute(DATA_SOURCE_ATTR);
        if (ds instanceof DataSource) {
            return (DataSource) ds;
        }
        throw new IllegalStateException("DataSource is not initialized");
    }

    public static Properties getProperties(ServletContext context) {
        Object props = context.getAttribute(APP_PROPERTIES_ATTR);
        if (props instanceof Properties) {
            return (Properties) props;
        }
        throw new IllegalStateException("Application properties are not loaded");
    }

    public static String getFileStoragePath(ServletContext context) {
        Object path = context.getAttribute(FILE_STORAGE_ATTR);
        if (path instanceof String) {
            return (String) path;
        }
        throw new IllegalStateException("File storage path missing");
    }

    public static Components getComponents(ServletContext context) {
        Object components = context.getAttribute(COMPONENTS_ATTR);
        if (components instanceof Components) {
            return (Components) components;
        }
        throw new IllegalStateException("Components not initialized");
    }

    public static final class Components {
        private final UserService userService;
        private final AuthService authService;
        private final PostService postService;
        private final CommentService commentService;
        private final MediaService mediaService;
        private final AdminService adminService;
        private final FollowDao followDao;
        private final FollowService followService;

        public Components(UserService userService,
                          AuthService authService,
                          PostService postService,
                          CommentService commentService,
                          MediaService mediaService,
                          AdminService adminService,
                          FollowDao followDao,
                          FollowService followService) {
            this.userService = userService;
            this.authService = authService;
            this.postService = postService;
            this.commentService = commentService;
            this.mediaService = mediaService;
            this.adminService = adminService;
            this.followDao = followDao;
            this.followService = followService;
        }

        public UserService userService() {
            return userService;
        }

        public AuthService authService() {
            return authService;
        }

        public PostService postService() {
            return postService;
        }

        public CommentService commentService() {
            return commentService;
        }

        public MediaService mediaService() {
            return mediaService;
        }

        public AdminService adminService() {
            return adminService;
        }

        public FollowDao followDao() {
            return followDao;
        }

        public FollowService followService() {
            return followService;
        }
    }
}
