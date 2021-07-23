/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.rbm.samples.servlets;

// [START callback servlet]

// [START import_libraries]

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.rbm.samples.KitchenSinkBot;
import com.google.api.services.rcsbusinessmessaging.v1.RbmApiHelper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.stream.Collectors;
// [END import_libraries]

/**
 * Servlet for Pub/Sub push request callback.
 *
 * JSON data is posted to /callback, parsed and passed to the Kitchen Sink bot message handler.
 */
@WebServlet(name = "AgentCallback", value = "/callback")
public class AgentCallback extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AgentCallback.class.getName());

    /** A hypothetical expensive operation we want to defer on a background task. */
    public static class ExpensiveOperation implements DeferredTask {
        private String messageId;
        private String senderPhoneNumber;
        private String userResponse;

        public ExpensiveOperation(String messageId, String senderPhoneNumber, String userResponse) {
            this.messageId = messageId;
            this.senderPhoneNumber = senderPhoneNumber;
            this.userResponse = userResponse;
        }

        @Override
        public void run() {
            // create an instance of the RBM API helper
            RbmApiHelper rbmApiHelper = new RbmApiHelper(new File(this.getClass()
                .getClassLoader().getResource("rbm-agent-service-account-credentials.json").getFile()));

            // let the user know we received and read the message
            rbmApiHelper.sendReadMessage(messageId, senderPhoneNumber);

            // create instance of the bot
            KitchenSinkBot kitchenSinkBot
                    = new KitchenSinkBot(senderPhoneNumber);

            // forward the response to the bot handler
            kitchenSinkBot.handleResponse(userResponse);
        }
    }

    public AgentCallback() {
        super();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // check to see that this is a post
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            // read the JSON data from the post body
            String jsonResponse = request.getReader().lines().collect(
                    Collectors.joining(System.lineSeparator())
            );

            logger.info(jsonResponse);

            // load the JSON string into a Json parser
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(jsonResponse).getAsJsonObject();

            // URL validation check
            if (obj.has("secret")) { 
                response.getWriter().println("{\"secret\": \"" + obj.get("secret").toString() + "\"}");
                response.getWriter().flush();
            }
            else if (obj.has("message")) { // make sure there is a message parameter
                obj = obj.get("message").getAsJsonObject();

                // check to see that the message parameter has data
                if (obj.has("data")) {
                    // get the base64 encodded response
                    String encodedResponse = obj.get("data").getAsString();

                    // decode the response
                    byte decodedResponse[] = Base64.getDecoder().decode(encodedResponse);

                    // convert the decoded bytes into a string
                    String decodedJsonResponse = new String(decodedResponse, "UTF-8");

                    // convert JSON response into an object
                    obj = parser.parse(decodedJsonResponse).getAsJsonObject();

                    logger.info(obj.toString());

                    // make sure the Json object contains response text, either plaintext
                    // or a suggested response object
                    if (isUserGeneratedResponse(obj)) {
                        logger.info("handling response");

                        String userResponseText = getUserResponse(obj);

                        logger.info("userResponseText: " + userResponseText);

                        if (userResponseText.length() > 0) {
                            String senderPhoneNumber = obj.get("senderPhoneNumber").getAsString();
                            String messageId = obj.get("messageId").getAsString();

                            // Add the task to the default queue.
                            Queue queue = QueueFactory.getDefaultQueue();

                            // create a deferred task to run the Kitchen Sink bot, this way
                            // the process of handling the response is non-blocking
                            queue.add(
                                    TaskOptions.Builder.withPayload(new ExpensiveOperation(
                                            messageId, senderPhoneNumber, userResponseText))
                                            .etaMillis(System.currentTimeMillis()));

                        }
                    }
                }
            }
        }
    }

    private String getUserResponse(JsonObject jsonObject) {
        if (jsonObject.has("text")) {
            return jsonObject.get("text").getAsString();
        }
        else if (jsonObject.has("suggestionResponse")) {
            return jsonObject
                    .get("suggestionResponse")
                    .getAsJsonObject()
                    .get("postbackData")
                    .getAsString();
        }
        else if (jsonObject.has("userFile")) {
            return jsonObject
                    .get("userFile")
                    .getAsJsonObject()
                    .get("payload")
                    .getAsJsonObject()
                    .get("fileUri")
                    .getAsString();
        }
        else if (jsonObject.has("location")) {
            return jsonObject
                    .get("location")
                    .toString();
        }

        return null;
    }

    /**
     * Checks to see if the JSON payload contains data sent from the user.
     * @param jsonObject The postback payload from the client.
     * @return True if the message was created by the user.
     */
    private boolean isUserGeneratedResponse(JsonObject jsonObject) {
        return jsonObject.has("text")
                || jsonObject.has("suggestionResponse")
                || jsonObject.has("userFile")
                || jsonObject.has("location");
    }
}
// [END callback servlet]
