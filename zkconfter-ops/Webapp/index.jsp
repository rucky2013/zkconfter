<%@ page contentType="text/html;charset=UTF-8" language="java" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="com.github.jsplite.Jsplite" %>
<% Jsplite.inherits("com.github.zkconfter.ops.web.Index", request, response); %>
<html>
<head>
  <title>ZkConfter OPS</title>
</head>
<body>
${hello}
</body>
</html>
