<!DOCTYPE html>
<!-- [START_EXCLUDE] -->
<%--
  ~ Copyright 2021 Google Inc.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you
  ~ may not use this file except in compliance with the License. You may
  ~ obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  ~ implied. See the License for the specific language governing
  ~ permissions and limitations under the License.
  --%>
<!-- [END_EXCLUDE] -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.rbm.samples.servlets.AgentConversationStart" %>
<%@ page import="com.google.rbm.samples.servlets.AgentCallback" %>
<html>
<head>
    <link href='//fonts.googleapis.com/css?family=Marmelad' rel='stylesheet' type='text/css'>
    <link
        rel="stylesheet"
        href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css"
        integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm"
        crossorigin="anonymous">
    </link>
    <link rel="stylesheet" href="/stylesheets/style.css?<%= new java.util.Date().getTime() %>">

    <script
      src="https://code.jquery.com/jquery-3.3.1.min.js"
      integrity="sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8="
      crossorigin="anonymous">
    </script>

    <script
        src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.0/js/bootstrap.min.js"
        integrity="sha384-uefMccjFJAIv6A+rW+L4AHf99KvxDjWSu1z9VI8SKNVmz4sk7buKt/6v9KI65qnm"
        crossorigin="anonymous">
    </script>

    <link rel="icon"
          type="image/png"
          href="/images/favicon.png">

    <title>The RBM Kitchen Sink Agent</title>

    <!-- Global site tag (gtag.js) - Google Analytics -->
    <script async src="https://www.googletagmanager.com/gtag/js?id=UA-124108791-2"></script>
    <script>
      window.dataLayer = window.dataLayer || [];
      function gtag(){dataLayer.push(arguments);}
      gtag('js', new Date());

      gtag('config', 'UA-124108791-2');
    </script>

    <script type="text/javascript">
        // regex for testing phone number format
        var regex = /^\+(?:[0-9] ?){6,14}[0-9]$/;

        $(document).ready(function() {
            $(document).on("click", ".register-link", function(e){
                e.preventDefault();

                showTestRegistration();
            });

            $("#submit-registration-btn").click(function(e) {
                e.preventDefault();

                submitRegistration();
            });

            $("#start-conversation-btn").click(function(e) {
                e.preventDefault();

                startConversation();
            });

            // start conversation form submission
            $(document).on("submit", "#conversation-form", function(event){
                event.preventDefault();

                removeErrors();

                var phoneNumber = $("#phone-number").val();

                if($.trim(phoneNumber) == "") {
                    event.stopPropagation();

                    showInputError("#phone-number", "Sorry, but you must provide a " +
                        "phone number to an RCS-enabled device.");
                }
                else if(!regex.test(phoneNumber)) {
                    event.stopPropagation();

                    showInputError("#phone-number", "Sorry, but you must enter your " +
                        "phone number in the format of +12223334444.");
                }
                else {
                    phoneNumber = encodeURIComponent(phoneNumber);
                    showWaiting("Initiating Conversation...", "#start-conversation-btn");

                    $.getJSON("/startConversation?phone_number="+phoneNumber, function(response) {
                        resetButton("#start-conversation-btn");

                        if(response.response == "ok") {
                           $("#success-msg").show();
                           $("#fail-msg").hide();
                        }
                        else {
                           if(response.message != undefined) {
                               showErrorMessage(response.message,
                                   "#fail-msg", "#success-msg");
                           }
                           else {
                               showErrorMessage("<p>Sorry, but the conversation could not be initiated. " +
                                  "Make sure that you have registered your phone as a test device " +
                                  "prior to initiating the conversation.</p>" +
                                  "<p class=\"text-center\"><a href=\"#\" " +
                                  "class=\"btn btn-outline-secondary register-link\">Register Now</a></p>",
                                  "#fail-msg", "#success-msg");
                           }
                        }
                    });
                }
            });

            // register the test device form submission
            $(document).on("submit", "#register-form", function(event){
                event.preventDefault();

                removeErrors();

                $("#success-register-msg").hide();
                $("#fail-register-msg").hide();

                var phoneNumber = $("#registration-number").val();

                if($.trim(phoneNumber) == "") {
                    showInputError("#registration-number", "Sorry, but you must provide a " +
                            "phone number to an RCS-enabled device.");
                }
                else if(!regex.test(phoneNumber)) {
                    showInputError("#registration-number", "Sorry, but you must enter your " +
                        "phone number in the format of +12223334444.");
                }
                else {
                    phoneNumber = encodeURIComponent(phoneNumber);
                    showWaiting("Registering...", "#submit-registration-btn");

                    $.getJSON("/registerDevice?phone_number="+phoneNumber, function(response) {
                        resetButton("#submit-registration-btn");

                        if(response.response == "ok") {
                            $("#registration-form-container").hide();
                            $("#success-register-msg").show();

                            $("#phone-number").val($("#registration-number").val());
                            $("#fail-msg").hide();

                            $("#registration-number").val("")
                        }
                        else {
                            if(response.message != undefined) {
                                showErrorMessage(response.message,
                                    "#fail-register-msg", "#success-register-msg");
                            }
                            else {
                                showErrorMessage("<p>Sorry, but an error occurred while " +
                                    "registering your device.</p>",
                                    "#fail-register-msg", "#success-register-msg");
                            }
                        }
                    });
                }
            });
        });

        function showWaiting(waitingText, button) {
            var loadingText = '<i class="fa fa-circle-o-notch fa-spin"></i> ' + waitingText;
            if ($(button).html() !== loadingText) {
                $(button).data("original-text", $(button).html());
                $(button).html(loadingText);
            }
        }

        function resetButton(button) {
            $(button).html($(button).data("original-text"));
        }

        function showTestRegistration() {
            removeErrors();

            $("#registration-number").val($("#phone-number").val());

            $("#registration-form-container").show();
            $("#success-register-msg").hide();
            $("#fail-register-msg").hide();
            $("#register-dialog").modal();
        }

        function startConversation() {
            $("#success-msg").hide();
            $("#fail-msg").hide();

            $("#conversation-form").submit();
        }

        function submitRegistration() {
            $("#success-register-msg").hide();
            $("#fail-register-msg").hide();

            $("#register-form").submit();
        }

        function removeErrors() {
            $("input").removeClass("is-invalid");
        }

        function showInputError(inputId, message) {
            $(inputId).addClass("is-invalid");
            $(inputId).parent().find(".invalid-feedback").html(message);
        }

        function showErrorMessage(message, failId, successId) {
            $(failId).html(message);
            $(failId).show();
            $(successId).hide();
        }
    </script>
</head>
<body>
    <div class="container" style="padding-top: 20px;">
        <h1 class="text-center">The RBM Kitchen Sink</h1>

        <div class="text-center center-block"><img style="width: 200px;" src="/images/kitchen-sink-logo.png" /></div>

        <div class="row">
            <div class="col-xs-12 col-sm-8 offset-sm-2 col-md-6 offset-md-3">
                <form method="get" action="/startConversation" id="conversation-form">
                    <h4 class="text-center">Enter your registered test device phone number:</h4>
                    <input class="form-control" id="phone-number" required autofocus
                            type="text" name="phone_number" placeholder="+12223334444" value="" />

                    <div class="invalid-feedback"></div>
                    <a
                        id="start-conversation-btn"
                        class="btn btn-lg btn-primary btn-block" href="#">Start Conversation</a>
                </form>

                <div class="alert alert-danger text-center" style="display: none;" id="fail-msg" role="alert">
                    Sorry, but conversation could not be initiated. Make sure that you have
                    <a href="#" class="register-link">registered your phone as a test device</a> prior to initiating the conversation.
                </div>

                <div class="alert alert-success text-center" style="display: none;" id="success-msg" role="alert">
                    You should be hearing from The RBM Kitchen Sink shortly.
                </div>

                <p class="text-center"><a href="#" class="register-link">Need access to the demo?</a></p>
            </div>

            <div class="col-xs-12 col-sm-10 offset-sm-1 col-md-8 offset-md-2 what-is-this">
                <h2 class="text-center d-none d-sm-block">What is the RBM Kitchen Sink?</h2>
                <div class="row">
                    <div class="col-sm-7 col-xs-12 d-flex align-items-center">
                        <div class="">
                                <h3>About this Demo</h3>

                                <p>The Kitchen Sink RCS Business Messaging agent demonstrates
                                all of the functionality available within an RBM agent.</p>

                                <p>Registering your RCS-enabled device will
                                give you a chance to experience the demo.</p>

                                <p><a href="#" class="register-link">Click here to register now</a>.</p>

                                <hr />

                                <p>
                                    You can learn more about
                                    <a href="https://jibe.google.com/business-messaging/"
                                    target="_blank">RCS business messaging here</a>.
                                </p>
                        </div>
                    </div>
                    <div class="col-sm-5 col-xs-12">
                        <div class="business-phone-container">
                            <img class="business-phone" src="/images/phone-shadow-right.png">
                            <div class="phone-screen">
                                <video id="business-video" loop autoplay muted playsinline>
                                    <source src="/images/kitchen-sink.mp4">
                                </video>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="modal" id="register-dialog" tabindex="-1" role="dialog">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-body">
                    <div id="registration-form-container">
                        <h3 class="text-center" style="margin-top: 20px; font-size: 18px;">Register for the Demo</h3>

                        <div class="row">
                            <div class="col-sm-8 offset-sm-2">
                                <form method="get" id="register-form" action="/registerDevice">
                                    <p class="text-center">Enter your phone number:</p>
                                    <input class="form-control" type="text" id="registration-number"
                                        required autofocus name="phone_number" placeholder="+12223334444" value="" />

                                    <div class="invalid-feedback"></div>
                                    <button id="submit-registration-btn" class="btn btn-lg btn-primary btn-block" type="submit">Register Device</button>
                                </form>
                            </div>

                            <div class="col-sm-12">
                                <hr />

                                <p class="text-center registration-explanation">
                                    After registering, you will receive a text message asking if you
                                    would like to become a tester.
                                    Select "Make me a tester" and then click the "Start Conversation"
                                    button to experience the RBM Kitchen Sink demo.
                                </p>
                            </div>
                        </div>
                    </div>

                    <div class="alert alert-danger text-center" id="fail-register-msg" role="alert">
                        Sorry, but an error occurred while registering your device.
                    </div>

                    <div class="alert alert-success" id="success-register-msg" role="alert">
                        Thank you, your phone should receive an invite to join as a tester shortly.
                        Once you accept, use the main form to initiate a conversation with the
                        RBM Kitchen Sink demo.
                    </div>

                    <div class="center-block text-center close-link">
                        <a href="#" data-dismiss="modal">[CLOSE]</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
