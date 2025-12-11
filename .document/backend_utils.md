# 后端工具类 (Util) 文档

本文档详细介绍了项目中 `com.micro.util` 包下的工具类。这些工具类提供了数据库连接、文件操作、JSON 处理、密码加密、属性加载以及数据库架构更新等通用功能。

## 目录

1. [DbUtil (数据库工具)](#1-dbutil-数据库工具)
2. [FileUtil (文件工具)](#2-fileutil-文件工具)
3. [JsonUtil (JSON 工具)](#3-jsonutil-json-工具)
4. [PasswordUtil (密码工具)](#4-passwordutil-密码工具)
5. [PropertyUtil (属性工具)](#5-propertyutil-属性工具)
6. [SchemaUpdater (架构更新工具)](#6-schemaupdater-架构更新工具)

---

## 1. DbUtil (数据库工具)

### 简介
`DbUtil` 是一个用于从 Servlet 上下文中获取数据库连接池连接的辅助类。它简化了从 `DataSource` 获取 `Connection` 的过程。

### 核心功能
- 从 `ServletContext` 中获取 `DataSource`。
- 从 `DataSource` 中获取数据库连接 `Connection`。

### 代码参考
```java
package com.micro.util;

import com.micro.listener.AppContextListener;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public final class DbUtil {
    private DbUtil() {}

    public static Connection getConnection(ServletContext context) throws SQLException {
        Objects.requireNonNull(context, "context");
        DataSource dataSource = AppContextListener.getDataSource(context);
        return dataSource.getConnection();
    }
}
```

### 调用方式
在 Servlet 或 Filter 中，可以通过传入 `ServletContext` 来获取连接：

```java
try (Connection conn = DbUtil.getConnection(getServletContext())) {
    // 使用连接执行 SQL 操作
} catch (SQLException e) {
    e.printStackTrace();
}
```

---

## 2. FileUtil (文件工具)

### 简介
`FileUtil` 处理媒体上传的文件存储约定。它负责将输入流保存到指定的存储路径，并生成唯一的文件名。

### 核心功能
- **saveToStorage**: 将输入流保存到磁盘，支持按日期分目录存储，自动生成 UUID 文件名。
- **deleteFile**: 删除指定路径的文件。
- **extractExtension**: 提取文件扩展名。

### 代码参考
```java
package com.micro.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

public final class FileUtil {
    private FileUtil() {}

    // 保存文件，自动按日期生成子目录
    public static String saveToStorage(InputStream inputStream, String storageRoot, long userId, String originalName) throws IOException {
        LocalDate today = LocalDate.now();
        String subDir = String.format("%d/%02d/user_%d", today.getYear(), today.getMonthValue(), userId);
        return saveToStorage(inputStream, storageRoot, subDir, originalName);
    }

    // 保存文件到指定子目录
    public static String saveToStorage(InputStream inputStream, String storageRoot, String subDir, String originalName) throws IOException {
        String normalizedRoot = Path.of(storageRoot).toAbsolutePath().toString();
        String extension = extractExtension(originalName);
        String relative = String.format("%s/%s%s", subDir, UUID.randomUUID(), extension);
        Path target = Path.of(normalizedRoot, relative);
        Files.createDirectories(target.getParent());
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        return relative.replace('\\', '/');
    }

    // 删除文件
    public static void deleteFile(String storageRoot, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        try {
            Path target = Path.of(storageRoot, relativePath);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            // 忽略删除错误
        }
    }

    private static String extractExtension(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        if (dot >= 0 && dot < name.length() - 1) {
            return name.substring(dot);
        }
        return "";
    }
}
```

### 调用方式
```java
// 保存上传的文件
String storageRoot = "/path/to/uploads";
String relativePath = FileUtil.saveToStorage(inputStream, storageRoot, userId, "image.png");

// 删除文件
FileUtil.deleteFile(storageRoot, relativePath);
```

---

## 3. JsonUtil (JSON 工具)

### 简介
`JsonUtil` 封装了 Jackson 的 `ObjectMapper`，提供统一的 JSON 序列化和反序列化入口。

### 核心功能
- **toJson**: 将对象转换为 JSON 字符串。
- **fromJson**: 将 JSON 字符串转换为对象。
- **mapper**: 获取底层的 `ObjectMapper` 实例。
- 配置了 `JavaTimeModule` 以支持 Java 8 时间类型。

### 代码参考
```java
package com.micro.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JsonUtil() {}

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize object", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to parse json", e);
        }
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
```

### 调用方式
```java
// 对象转 JSON
User user = new User();
String json = JsonUtil.toJson(user);

// JSON 转对象
User user = JsonUtil.fromJson(jsonString, User.class);
```

---

## 4. PasswordUtil (密码工具)

### 简介
`PasswordUtil` 是对 BCrypt 算法的轻量级封装，用于密码的哈希加密和验证。

### 核心功能
- **hashPassword**: 对明文密码进行加盐哈希。
- **matches**: 验证明文密码与哈希值是否匹配。

### 代码参考
```java
package com.micro.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private PasswordUtil() {}

    public static String hashPassword(String plain, int workFactor) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(workFactor));
    }

    public static boolean matches(String plain, String hashed) {
        if (plain == null || hashed == null) return false;
        return BCrypt.checkpw(plain, hashed);
    }
}
```

### 调用方式
```java
// 加密密码
String hashedPassword = PasswordUtil.hashPassword("mySecretPassword", 12);

// 验证密码
boolean isValid = PasswordUtil.matches("inputPassword", hashedPassword);
```

---

## 5. PropertyUtil (属性工具)

### 简介
`PropertyUtil` 用于从类路径加载 `.properties` 配置文件。

### 核心功能
- **load**: 加载指定名称的资源文件并返回 `Properties` 对象。

### 代码参考
```java
package com.micro.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public final class PropertyUtil {
    private PropertyUtil() {}

    public static Properties load(String resourceName) {
        Objects.requireNonNull(resourceName, "resourceName");
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = loader.getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Cannot find resource " + resourceName);
            }
            Properties props = new Properties();
            props.load(in);
            return props;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load properties from " + resourceName, ex);
        }
    }
}
```

### 调用方式
```java
Properties props = PropertyUtil.load("application.properties");
String dbUrl = props.getProperty("db.url");
```

---

## 6. SchemaUpdater (架构更新工具)

### 简介
`SchemaUpdater` 用于在应用启动时检查并自动更新数据库架构（如添加缺失的列）。

### 核心功能
- **checkAndUpdate**: 检查数据库表结构，如果缺少特定列（如 `banner_path`, `avatar_data` 等），则自动执行 `ALTER TABLE` 语句进行添加。

### 代码参考
```java
package com.micro.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.*;

public class SchemaUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaUpdater.class);

    public static void checkAndUpdate(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            
            // 示例：检查并添加 banner_path 列
            if (!columnExists(meta, "users", "banner_path")) {
                LOGGER.info("Column 'banner_path' not found in 'users' table. Adding it...");
                addColumn(conn, "users", "banner_path", "VARCHAR(255) AFTER avatar_path");
            }
            // ... 其他列检查
        } catch (SQLException e) {
            LOGGER.error("Failed to check or update database schema", e);
        }
    }

    private static boolean columnExists(DatabaseMetaData meta, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static void addColumn(Connection conn, String tableName, String columnName, String columnDefinition) throws SQLException {
        String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnDefinition);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            LOGGER.info("Successfully added column {} to table {}", columnName, tableName);
        }
    }
}
```

### 调用方式
通常在应用启动监听器（如 `ServletContextListener`）中调用：

```java
DataSource dataSource = ...; // 获取数据源
SchemaUpdater.checkAndUpdate(dataSource);
```
