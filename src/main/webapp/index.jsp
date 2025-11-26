<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    String ctx = request.getContextPath();
    response.sendRedirect(ctx + "/app/feed");
    return;
%>
