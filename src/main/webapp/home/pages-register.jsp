<%--
  Created by IntelliJ IDEA.
  User: ACER
  Date: 10/21/2021
  Time: 4:16 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="util.CSRFUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <title>Register | Hyper - Responsive Bootstrap 5 Admin Dashboard</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta content="A fully featured admin theme which can be used to build CRM, CMS, etc." name="description" />
    <meta content="Coderthemes" name="author" />
    <!-- App favicon -->
    <link rel="shortcut icon" href="../assets/images/title.ico">

    <!-- App css -->
    <link href="<c:url value = "../assets/css/icons.min.css"/>" rel="stylesheet" type="text/css">
    <link href="<c:url value = "../assets/css/app.min.css"/>" rel="stylesheet" type="text/css" id="light-style">
    <link href="<c:url value = "../assets/css/app-dark.min.css"/>" rel="stylesheet" type="text/css" id="dark-style">

</head>

<body class="loading authentication-bg" data-layout-config='{"leftSideBarTheme":"dark","layoutBoxed":false, "leftSidebarCondensed":false, "leftSidebarScrollable":false,"darkMode":false, "showRightSidebarOnStart": true}'>

<div class="account-pages pt-2 pt-sm-5 pb-4 pb-sm-5">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-xxl-4 col-lg-5">
                <div class="card">
                    <!-- Logo-->
                    <div class="card-header pt-4 pb-4 text-center bg-primary">
                        <a href="<c:url value = "/view-home?action=home"/>">
                            <span><img src="<c:url value = "../assets/images/logo.png"/>" alt="" height="18"></span>
                        </a>
                    </div>

                    <div class="card-body p-4">

                        <div class="text-center w-75 m-auto">
                            <h4 class="text-dark-50 text-center mt-0 fw-bold">Free Sign Up</h4>
                            <p class="text-muted mb-4">Don't have an account? Create your account, it takes less than a minute </p>
                        </div>
                        <%
                            // generate a random CSRF token
                            String csrfToken = CSRFUtil.getToken();

                            // place the CSRF token in a cookie
                            javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie("csrfToken", csrfToken);
                            response.setHeader("X-Content-Type-Options", "nosniff");
                            //Cookie without SameSite Attribute
                            response.setHeader("Set-Cookie", "key=value; HttpOnly; SameSite=Strict");
                            cookie.setHttpOnly(true);
                            cookie.setSecure(true);
                            response.addCookie(cookie);
                        %>

                        <form action="/view-register" method="post">
                            <input type="hidden" name="csrf" value="<%= csrfToken %>"/>
                            <c:if test="${not empty messageResponse}">
                                <div class="alert alert-${alert} mb-3" role="alert">
                                        ${messageResponse}
                                    <c:if test="${requestScope.alert.equals('danger')}">
                                        - Email or User Name already exists
                                    </c:if>
                                </div>
                            </c:if>
                            <div class="mb-3">
                                <label for="fullname" class="form-label">Full Name</label>
                                <input class="form-control" type="text" id="fullname" name="fullname" placeholder="Enter your full name" required>
                            </div>

                            <div class="mb-3">
                                <label for="fullname" class="form-label">User Name</label>
                                <input class="form-control" type="text" id="username" name="username" placeholder="Enter your user name" required>
                            </div>

                            <div class="mb-3">
                                <label for="emailaddress" class="form-label">Email address</label>
                                <input class="form-control" type="email" id="emailaddress" name="email" required placeholder="Enter your email">
                            </div>

                            <div class="mb-3">
                                <label for="password" class="form-label">Password</label>
                                <div class="input-group input-group-merge">
                                    <input type="password" id="password" name="pass" class="form-control" placeholder="Enter your password"
                                        onkeyup="validate()" onchange="validate()" onclick="validate()"
                                    >
                                    <div class="input-group-text" data-password="false">
                                        <span class="password-eye"></span>
                                    </div>
                                </div>
                                <span style="color: red; font-size: 9px" id="temp"></span>
                            </div>
                            <div class="mb-3">
                                <label for="confirm-password" class="form-label">Confirm Password</label>
                                <div class="input-group input-group-merge">
                                    <input type="password" id="confirm-password" name="comfirmPass" class="form-control" placeholder="Enter your confirm password">
                                    <div class="input-group-text" data-password="false">
                                        <span class="password-eye"></span>
                                    </div>
                                </div>
                            </div>
                            <div class="mb-3">
                                <div class="form-check">
                                    <input type="checkbox" class="form-check-input" id="checkbox-signup" name="confirmBox" required>
                                    <label class="form-check-label" for="checkbox-signup">I accept <a href="#" class="text-muted">Terms and Conditions</a></label>
                                </div>
                            </div>

                            <div class="mb-3 text-center">
                                <button id="submit" class="btn btn-primary" type="submit"> Sign Up </button>
                            </div>

                        </form>
                    </div> <!-- end card-body -->
                </div>
                <!-- end card -->

                <div class="row mt-3">
                    <div class="col-12 text-center">
                        <p class="text-muted">Already have account? <a href="<c:url value = "/view-login?action=login"/>" class="text-muted ms-1"><b>Log In</b></a></p>
                    </div> <!-- end col-->
                </div>
                <!-- end row -->

            </div> <!-- end col -->
        </div>
        <!-- end row -->
    </div>
    <!-- end container -->
</div>
<!-- end page -->


<footer class="footer footer-alt">
    Community HCM University of Technology and Education
</footer>

<!-- bundle -->

<script src="<c:url value = "../assets/js/vendor.min.js"/>"></script>
<script src="<c:url value = "../assets/js/app.min.js"/>"></script>
<script>
    function validate() {
        var format = /[!@#$%^&*()_+\-=\[\]{}:\\|,.<>\/?]+/;
        var formatAlphabetCap = /[ABCDEFGHIJKLMNOPQRSTUVWXYZ]+/;
        var formatAlphabet = /[abcdefghijklmnopqrstuvwxyz]+/;
        var formatNumber = /[1234567890]+/;
        var pw = document.getElementById("password").value;

        if (pw == "") {
            document.getElementById("temp").innerHTML = "Invalid!!";
            document.getElementById("submit").disabled = true;
            document.getElementById("submit").style.cursor = "not-allowed"
        }
        else if (pw.length < 8 || pw.length > 16) {
            document.getElementById("temp").innerHTML = "Min length: 8 & Max length: 16";
            document.getElementById("submit").disabled = true;
            document.getElementById("submit").style.cursor = "not-allowed"
        }
        else if (formatNumber.test(pw) == false) {
            document.getElementById("temp").innerHTML = "Can it nhat 1 chu so";
            document.getElementById("submit").disabled = true;
            document.getElementById("submit").style.cursor = "not-allowed"
        }
        else if (formatAlphabet.test(pw) == false) {
            document.getElementById("temp").innerHTML = "Can ton tai mot ki tu a-z";
            document.getElementById("submit").disabled = true;
            document.getElementById("submit").style.cursor = "not-allowed"
        }
        else if (formatAlphabetCap.test(pw) == false) {
            document.getElementById("temp").innerHTML = "Can ton tai mot ki tu A-Z";
            document.getElementById("submit").disabled = true;
            document.getElementById("submit").style.cursor = "not-allowed"
        }
        else if (format.test(pw) == false) {
            document.getElementById("temp").innerHTML = "Can ton tai mot ki tu dac biet";
            document.getElementById("submit").disabled = true;
            document.getElementById("submit").style.cursor = "not-allowed"
        }
        else {
            document.getElementById("temp").innerHTML = "correct";
            document.getElementById("temp").style.color = "green"
            document.getElementById("submit").disabled = false;
            document.getElementById("submit").style.cursor = "pointer"
        }
    }
</script>

</body>
</html>
