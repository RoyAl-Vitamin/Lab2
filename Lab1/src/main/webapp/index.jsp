<%--
  Created by IntelliJ IDEA.
  User: alex
  Date: 22.04.18
  Time: 20:03
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Index page</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="css/bootstrap.css">
        <script src="js/jquery-3.3.1.min.js"></script>
        <script src="js/popper.min.js"></script>
        <script src="js/bootstrap.min.js"></script>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
        <link rel="stylesheet" href="css/main.css">
    </head>
    <body>
        <div class="container">
            <div class="row">
                <div class="col-lg-12">
                    <h1 style="text-align: center;">Lab 2</h1>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12">
                    <form action="index" method="post">
                        <div class="form-group btn-group col-lg-12">
                            <input name="text" type="text" class="form-control" value="${sentence}">
                            <button type="submit" class="btn btn-primary">Submit</button>

                            <div class="btn-group">
                                <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
                                    <i class="fa fa-cog" style="font-size:24px"></i>
                                </button>
                                <div class="dropdown-menu">
                                    <a class="dropdown-item" href="#">
                                        <i class="fa fa-database" style="font-size:24px; min-width: 26px;"></i> Drop DB
                                    </a>
                                    <a class="dropdown-item" href="#">
                                        <i class="fa fa-cloud-upload" style="font-size:24px; min-width: 26px;"></i> Download file
                                    </a>
                                </div>
                            </div>

                        </div>
                    </form>
                </div>
            </div>
            <c:if test="${list != null && fn:length(list) != 0}">
                <div class="row">
                    <div class="col-lg-12">
                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <th>name</th>
                                    <th>index</th>
                                </tr>
                            </thead>
                            <c:forEach items="${list}" var="item">
                                <tr>
                                    <td>
                                        <c:out value="${item.name}" />
                                    </td>
                                    <td>
                                        <c:out value="${item.index}" />
                                    </td>
                                </tr>
                            </c:forEach>
                        </table>
                    </div>
                </div
            </c:if>
        </div>
    </body>
</html>
