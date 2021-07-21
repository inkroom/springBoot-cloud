<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta name="format-detection" content="telephone=no,email=no"/>
    <meta name="theme-color" content="#42d3a5"/>
    <link rel="shortcut icon" type="image/png"/>
    <title>Eureka - Login</title>
    <link href="assets/login/css/chunk-common.6aba055e.css" rel="stylesheet"/>
</head>
<body class="login">
<section class="hero is-fullheight">
    <div class="hero-body">
        <div class="container has-text-centered">
            <div class="column is-4 is-offset-4">
                <div class="box">
                    <figure class="image is-128x128 login--logo"></figure>
                    <h1 class="title has-text-primary">Eureka</h1>
                    <form method="post">
                        <div class="field">
                            <#if error>
                                <p class="is-medium has-text-danger">Invalid username or
                                    password</p>
                            </#if>
<#--                            <p class="is-medium">Logout successful</p>-->
                        </div>
                        <div class="field">
                            <div class="control">
                                <input class="input is-medium" type="input" name="username" placeholder="Username"
                                       autofocus=""/>
                            </div>
                        </div>
                        <div class="field">
                            <div class="control">
                                <input class="input is-medium" type="password" name="password" placeholder="Password"/>
                            </div>
                        </div>
                        <div class="field">
                            <div class="control">
                                <input type="submit" class="button is-block is-primary is-medium is-fullwidth"
                                       value="Login"/>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</section>
<script src="assets/login/js/chunk-vendors.ec700424.js"></script>
<script src="assets/login/js/chunk-common.dec76664.js"></script>
<script src="assets/login/js/login.afed180c.js"></script>
</body>
</html>