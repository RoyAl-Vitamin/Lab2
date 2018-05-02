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
        <!-- IE -->
        <link rel="shortcut icon" type="image/x-icon" href="favicon.ico" />
        <!-- other browsers -->
        <link rel="icon" type="image/x-icon" href="favicon.ico"/>
    </head>
    <body>
        <div class="container h-100 primary-main">
            <div class="row p-1 primary-dark">
                <div class="col-lg-12">
                    <h1 style="text-align: center;">Lab 2</h1>
                </div>
            </div>
            <div class="row pt-4 primary-main">
                <div class="col-lg-12">
                    <form action="index" method="post">
                        <div class="form-group btn-group col-lg-12">
                            <input name="text" type="text" class="form-control" value="${sentence}" placeholder="DB contains ${count} files">
                            <button type="submit" class="btn btn-primary">Submit</button>

                            <div class="btn-group">
                                <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
                                    <i class="fa fa-cog" style="font-size:24px"></i>
                                </button>
                                <div class="dropdown-menu">
                                    <a class="dropdown-item" href="dropdb">
                                        <i class="fa fa-database" style="font-size:24px; min-width: 26px;"></i> Drop DB
                                    </a>
                                    <a class="dropdown-item" id="download-file" data-toggle="modal" data-target="#myModal">
                                        <i class="fa fa-cloud-upload" style="font-size:24px; min-width: 26px;"></i> Download file
                                    </a>
                                </div>
                            </div>

                        </div>
                    </form>
                </div>
            </div>
            <c:if test="${list != null && fn:length(list) != 0}">
                <div class="row p-3 primary-main">
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

        <!-- The Modal -->
        <div class="modal fade" id="myModal">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">

                    <!-- Modal Header -->
                    <div class="modal-header">
                        <h4 class="modal-title">Download file</h4>
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                    </div>

                    <!-- Modal body -->
                    <div class="modal-body">
                        <div class="row align-content-md-center">
                            <form method="POST" action="download" enctype="application/x-www-form-urlencoded" class="form-inline w-100">
                                <div class="row w-100">
                                    <div class="col">
                                        <label class="title-file" for="path">Put file adress:</label>
                                    </div>
                                    <div class="col">
                                        <input type="url" name="url" class="form-control" id="path" placeholder="Http adress">
                                    </div>
                                    <div class="col align-baseline">
                                        <button type="submit" class="btn btn-primary">Submit</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                        <div class="row align-content-md-center">
                            <form method="POST" action="download" enctype="multipart/form-data" class="form-inline w-100">
                                <div class="row w-100">
                                    <div class="col">
                                        <label class="title-file" for="file">Put file:</label>
                                    </div>
                                    <div class="col">
                                        <input type="file" name="file" class="form-control" id="file">
                                    </div>
                                    <div class="col">
                                        <button type="submit" class="btn btn-primary">Submit</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Modal footer -->
                    <div class="modal-footer">
                        <button type="button" class="btn btn-danger" data-dismiss="modal">Close</button>
                    </div>

                </div>
            </div>
        </div>
    </body>
</html>
