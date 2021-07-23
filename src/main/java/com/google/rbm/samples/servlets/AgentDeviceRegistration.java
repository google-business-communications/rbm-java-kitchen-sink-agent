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

// [START register a test device servlet]

// [START import_libraries]
import com.google.api.services.rcsbusinessmessaging.v1.RbmApiHelper;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
// [END import_libraries]

/**
 * Servlet for registering a phone number as a test device for the Kitchen Sink agent demo.
 */
@WebServlet(name = "AgentDeviceRegistration", value = "/registerDevice")
public class AgentDeviceRegistration extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AgentDeviceRegistration.class.getName());

    public AgentDeviceRegistration() {
        super();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // set the response type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // get the test device number
        String phoneNumber = request.getParameter("phone_number");

        try {
            // create an instance of the RBM API helper
            RbmApiHelper rbmApiHelper = new RbmApiHelper(new File(this.getClass()
                .getClassLoader().getResource("rbm-agent-service-account-credentials.json").getFile()));

            // register the device
            rbmApiHelper.registerTester(phoneNumber);

            response.getWriter().println("{\"response\": \"ok\"}");
            response.getWriter().flush();
        } catch(Exception e) {
            logger.info(e.getMessage());

            response.getWriter().println("{\"response\": \"fail\"}");
            response.getWriter().flush();
        }
    }
}
// [END register a test device servlet]