# RCS BUSINESS MESSAGING: Kitchen Sink

This sample demonstrates how to use the [RCS Business Messaging Java client library](https://github.com/google-business-communications/java-rcsbusinessmessaging) for performing operations
with the [RCS Business Messaging API](https://developers.google.com/business-communications/rcs-business-messaging/reference/rest).

This sample provides an interactive way to explore RBM's features on your device and demonstrates how to use the Java
client library to create different RBM message types.

This sample is set up to run on the Google App Engine. See the
[Google App Engine](https://cloud.google.com/appengine/docs/java/) standard environment
documentation for more detailed instructions.

This application assumes that you're signed up with
[RCS Business Messaging](https://developers.google.com/business-communications/rcs-business-messaging/guides/get-started/register-partner).

## Documentation

The documentation for the RCS Business Messaging API can be found [here](https://developers.google.com/business-communications/rcs-business-messaging/reference/rest).

## Prerequisite

You must have the following software installed on your machine:

* [Apache Maven](http://maven.apache.org) 3.3.9 or greater
* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Google Cloud SDK](https://cloud.google.com/sdk/) (aka gcloud)
* [Google App Engine SDK for Java](https://cloud.google.com/appengine/docs/standard/java/download)

## Before you begin

1.  [Register with RCS Business Messaging](https://developers.google.com/business-communications/rcs-business-messaging/guides/get-started/register-partner).
1. Open the [Business Communications  Console](https://business-communications.cloud.google.com/console/) with your
registered Google account and create a new RBM agent.
1. When the agent is available, click the agent's card.
1. In the left navigation, click **Service account**.
1. Click **Create key**, then click **Create**. Your browser downloads a service account key for
    your agent. You need this key to make RBM API calls as your agent.
1. Rename the service account key "rbm-agent-service-account-credentials.json" and move it
    into the "rbm-java-kitchen-sink/src/main/resources" directory.

## Execute the sample

1. In a terminal, navigate to this sample's root directory.
1. Run the following commands:

    mvn appengine:run
1. In a browser, navigate to http://localhost:8080/.

In order for the sample agent to process your responses, you need to deploy the agent.

## Deploy the sample

1. In a terminal, navigate to this sample's root directory.
1. Run the following commands:

    mvn appengine:deploy
1. In a browser, navigate to  https://YOUR-GCP-PROJECT-ID.appspot.com.

## Configure the agent webhook

1. Return to the [Business Communications Console](https://business-communications.cloud.google.com/), in the left
navigation, click **Integrations**.
1. Click Edit subscription and configure a push subscription with a
URL of https://YOUR-GCP-PROJECT-ID.appspot.com/callback

## Learn more

To learn more about setting up RCS Business Messaging see the
[documentation](https://developers.google.com/business-communications/rcs-business-messaging/guides/get-started/how-it-works).