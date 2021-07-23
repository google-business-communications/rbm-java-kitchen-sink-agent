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
package com.google.rbm.samples;

// [START of the chat bot for the RBM Kitchen Sink Bot]

// [START import_libraries]

import com.google.api.services.rcsbusinessmessaging.v1.model.*;
import com.google.appengine.api.datastore.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.api.services.rcsbusinessmessaging.v1.RbmApiHelper;
import com.google.api.services.rcsbusinessmessaging.v1.StandaloneCardHelper;
import com.google.api.services.rcsbusinessmessaging.v1.SuggestionHelper;
import com.google.api.services.rcsbusinessmessaging.v1.model.cards.CardOrientation;
import com.google.api.services.rcsbusinessmessaging.v1.model.cards.CardWidth;
import com.google.api.services.rcsbusinessmessaging.v1.model.cards.MediaHeight;
import com.google.api.services.rcsbusinessmessaging.v1.model.cards.ThumbnailImageAlignment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
// [END import_libraries]

/**
 * Kicthen Sink Bot
 *
 * This agent let's a user explore all the features of RBM via an interactive bot.
 */
public class KitchenSinkBot {
    // logger for info and error messages
    private static final Logger logger = Logger.getLogger(KitchenSinkBot.class.getName());

    // string that user can type to restart the bot experience
    private static final String START_AGENT = "start";

    private static final String EXCEPTION_WAS_THROWN = "an exception was thrown";

    // constant for the URL to a sample video for the rich card example
    private static final String SAMPLE_VIDEO_URL =
            "https://storage.googleapis.com/kitchen-sink-sample-images/sample-video.mp4";

    public static final String SAMPLE_VIDEO_THUMBNAIL_URL =
            "https://storage.googleapis.com/kitchen-sink-sample-images/sample-video-thumbnail.jpg";

    private static final SuggestionHelper EXPLORE_RBM_FEATURES
            = new SuggestionHelper("Explore RBM", "explore_rbm");

    private static final SuggestionHelper CAROUSEL_OPTION
            = new SuggestionHelper("Carousel", "carousel");

    private static final SuggestionHelper ACTIONS_CHIP_LIST_OPTION
            = new SuggestionHelper("Actions Chip List", "actions_chip_list");

    private static final SuggestionHelper TEXT_MESSAGE_OPTION
            = new SuggestionHelper("Text Message", "text_message");

    private static final SuggestionHelper TEXT_WITH_CHIP_LIST_OPTION
            = new SuggestionHelper("Text Message w/ Chip List", "text_with_chip_list");

    private static final SuggestionHelper RICH_CARD_OPTION
            = new SuggestionHelper("Rich Card", "rich_card");

    private static final SuggestionHelper USER_GENERATED
            = new SuggestionHelper("User Generated", "user_generated");

    private static final SuggestionHelper USER_SENT_IMAGE
            = new SuggestionHelper("Image", "user_image");

    private static final SuggestionHelper USER_SENT_TEXT
            = new SuggestionHelper("Text", "user_text");

    private static final SuggestionHelper VERTICAL_RICH_CARD_OPTION
            = new SuggestionHelper("Vertical Rich Card", "VERTICAL");

    private static final SuggestionHelper HORIZONTAL_RICH_CARD_OPTION
            = new SuggestionHelper("Horizontal Rich Card", "HORIZONTAL");

    private static final SuggestionHelper HORIZONTAL_LEFT_THUMBNAIL_OPTION
            = new SuggestionHelper("Left", "LEFT");

    private static final SuggestionHelper HORIZONTAL_RIGHT_THUMBNAIL_OPTION
            = new SuggestionHelper("Right", "RIGHT");

    private static final SuggestionHelper VERTICAL_HEIGHT_SHORT_OPTION
            = new SuggestionHelper("Short", "SHORT");

    private static final SuggestionHelper VERTICAL_HEIGHT_MEDIUM_OPTION
            = new SuggestionHelper("Medium", "MEDIUM");

    private static final SuggestionHelper VERTICAL_HEIGHT_TALL_OPTION
            = new SuggestionHelper("Tall", "TALL");

    private static final SuggestionHelper RICH_CARD_WITH_IMAGE
            = new SuggestionHelper("Image", "image_rich_card");

    private static final SuggestionHelper RICH_CARD_WITH_VIDEO
            = new SuggestionHelper("Video", "video_rich_card");

    public static final SuggestionHelper RICH_CARD_WITH_IMAGE_THUMBNAIL
            = new SuggestionHelper("Image w/ Thumbnail", "image_thumbnail_rich_card");

    public static final SuggestionHelper RICH_CARD_WITH_VIDEO_THUMBNAIL
            = new SuggestionHelper("Video w/ Thumbnail", "video_thumbnail_rich_card");

    // constant for suggestion to view your location
    private static final SuggestionHelper VIEW_LOCATION_OPTION
            = new SuggestionHelper("View Location", "map");

    // constant for suggestion of a calendar event
    private static final SuggestionHelper CALENDAR_OPTION
            = new SuggestionHelper("Create Calendar Event", "calendar");

    private static final SuggestionHelper DIAL_OPTION
            = new SuggestionHelper("Dial", "dial");

    private static final SuggestionHelper URL_OPTION
            = new SuggestionHelper("Open Url", "url");

    private static final SuggestionHelper SHARE_LOCATION_OPTION
            = new SuggestionHelper("Share Location", "share_location");

    private static final SuggestionHelper FUN_VIDEO_OPTION
            = new SuggestionHelper("Fun Video", "fun_video");

    // constant for liking an image
    private static final SuggestionHelper LIKE_ITEM
            = new SuggestionHelper("\uD83D\uDC4D Like", "like-item");

    // constant for disliking an image
    private static final SuggestionHelper DISLIKE_ITEM
            = new SuggestionHelper("\uD83D\uDC4E Dislike", "dislike-item");

    // constant for carousel suggestion items
    private static final List<SuggestionHelper> CAROUSEL_SUGGESTION_ITEMS
            = Arrays.asList(LIKE_ITEM, DISLIKE_ITEM);

    // constant for carousel sample
    private static final StandaloneCardHelper ADVENTURE_CLIFF_CARD =
            new StandaloneCardHelper(
                    "Snowy cliff",
                    "What do you think?",
                    "https://storage.googleapis.com/kitchen-sink-sample-images/adventure-cliff.jpg",
                    CAROUSEL_SUGGESTION_ITEMS);

    // constant for carousel sample
    private static final StandaloneCardHelper CUTE_DOG_CARD =
            new StandaloneCardHelper(
                    "Cute dog",
                    "What do you think?",
                    "https://storage.googleapis.com/kitchen-sink-sample-images/cute-dog.jpg",
                    CAROUSEL_SUGGESTION_ITEMS);

    // constant for carousel sample
    private static final StandaloneCardHelper ELEPHANT_CARD =
            new StandaloneCardHelper(
                    "Elephant in the woods",
                    "What do you think?",
                    "https://storage.googleapis.com/kitchen-sink-sample-images/elephant.jpg",
                    CAROUSEL_SUGGESTION_ITEMS);

    // constant for carousel sample
    private static final StandaloneCardHelper GOLDEN_GATE_CARD =
            new StandaloneCardHelper(
                    "Golden Gate Bridge",
                    "What do you think?",
                    "https://storage.googleapis.com/kitchen-sink-sample-images/golden-gate-bridge.jpg",
                    CAROUSEL_SUGGESTION_ITEMS);

    // constant for carousel sample
    private static final StandaloneCardHelper SHEEP_CARD =
            new StandaloneCardHelper(
                    "Cute sheep",
                    "What do you think?",
                    "https://storage.googleapis.com/kitchen-sink-sample-images/sheep.jpg",
                    CAROUSEL_SUGGESTION_ITEMS);

    // key values for storing user selections when generating rich cards
    private static final String RICH_CARD_HEIGHT = "rich_card_height";
    private static final String RICH_CARD_ALIGNNMENT = "thumbnail_alignment";
    private static final String RICH_CARD_ORIENTATION = "card_orientation";

    // key value for storing user selections when sending content to the bot
    private static final String USER_CONTENT_SELECTION = "user_content_selection";

    private static final String FOLLOW_UP_DEFAULT_OPTIONS = "Pick another feature to explore:";

    // the phone number, in E.164 format, to start a conversation with
    private String msisdn;

    // wrapper class for the RBM API, makes calls simpler
    private RbmApiHelper rbmApiHelper;

    /**
     * Constructor for the Kitchen Sink bot.
     * @param msisdn The phone number in E.164 format.
     */
    public KitchenSinkBot(String msisdn) {
        this.msisdn = msisdn;

        this.rbmApiHelper = new RbmApiHelper(new File(this.getClass()
            .getClassLoader().getResource("rbm-agent-service-account-credentials.json").getFile()));
    }

    /**
     * Sends the initial greeting of to the user.
     */
    public void sendGreeting() throws IOException {
        sendDefaultOptions();
    }

    /**
     * Response message handler for client responses.
     * Checks to see if the client requested the greeting message,
     * if not, the bot is selected based on the last mode selected,
     * and the message handling is passed along to the bot.
     * @param userResponse The text of the user response.
     */
    public void handleResponse(String userResponse) {
        logger.info("KitchenSinkBot handleResponse");

        rbmApiHelper.sendIsTypingMessage(msisdn);

        String cleanResponse = userResponse.toLowerCase();

        try {
            // check to see if we need to resend the bot greeting
            if (cleanResponse.equals(START_AGENT)) {
                sendGreeting();
            }
            else if (userResponse.equals(EXPLORE_RBM_FEATURES.getPostbackData())) {
                sendDefaultOptions();
            }
            else if (isImageResponse(userResponse)) {
                // handles like/dislike of an image
                sendImageResponseNotification(userResponse);
            }
            else if (isRichCardChoiceResponse(userResponse)) {
                // asks the user to select rich card height or alignment
                sendRichCardMediaOptions(userResponse);
            }
            else if (isRichCardOptionResponse(userResponse)) {
                // asks the user to select image or video for content
                sendRichCardContentOptions(userResponse);
            }
            else if (isRichCardContentResponse(userResponse)) {
                // sends the rich card based on what the user has selected for options
                sendRichCardExample(userResponse);

                sendDefaultOptions(FOLLOW_UP_DEFAULT_OPTIONS);
            }
            else if (isRbmTestFunctionality(userResponse)) {
                // sends the next message for the test based on the user's response
                sendRbmTest(userResponse);
            }
            else if (isUserContentSelection(userResponse)) {
                // sends a message prompting the user to send content matching their selection
                sendContentPrompt(userResponse);
            }
            else if (isLatLng(userResponse)) {
                sendLatLngFollowUp(userResponse);
            }
            else if (isUserSentContent()) {
                // check to see if this response is due to prompting for user-generated content
                sendContentResponse(userResponse);
            }
            else if (!isIgnoredResponse(userResponse)) {
                sendDefaultOptions(FOLLOW_UP_DEFAULT_OPTIONS);
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Echos the user's shared location back to their device.
     * @param userResponse The client's location as a JSON string.
     */
    private void sendLatLngFollowUp(String userResponse) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(userResponse).getAsJsonObject();

        String lat = "";
        if(jsonObject.has("latitude")) {
            lat = jsonObject.get("latitude").toString();
        }

        String lng  = "";
        if(jsonObject.has("longitude")) {
            lng = jsonObject.get("longitude").toString();
        }

        String messageText = "You shared the location: [" + lat + ", " + lng + "]\n\n" +
                "Please select another feature to explore:";

        logger.info("shared location = " + messageText);

        sendDefaultOptions(messageText);
    }

    /**
     * Sends the client a message based on the content they sent the bot. If an image was sent
     * the image is echoed back to the user as a rich card. If a message was sent, the message
     * is echoed back to the user.
     * @param userResponse The last message received from the user.
     */
    private void sendContentResponse(String userResponse) {
        logger.info("content value = " + userResponse);

        Entity clientConfig = getExistingClientConfig();

        String contentType = clientConfig.getProperty(USER_CONTENT_SELECTION).toString();

        // reset the content in the client config
        saveClientConfig(USER_CONTENT_SELECTION, "");

        // check to see if the user sent an image file
        if (contentType.equals(USER_SENT_IMAGE.getPostbackData())) {
            sendFile(userResponse, "Here is the file you sent!");
        }
        else {
            String messageText = "You sent the message \"" + userResponse + "\"";

            sendDefaultOptions(messageText);
        }
    }

    /**
     * Response handler for the different RBM features a user can explore.
     * @param userResponse The postback data from the user's chip selection.
     */
    private void sendRbmTest(String userResponse) {
        if (userResponse.equals(CAROUSEL_OPTION.getPostbackData())) {
            // send carousel
            sendSampleCarousel();

            // send follow-up test options
            sendDefaultOptions(FOLLOW_UP_DEFAULT_OPTIONS);
        }
        else if (userResponse.equals(ACTIONS_CHIP_LIST_OPTION.getPostbackData())) {
            // send actions chip list
            sendRbmActionsChipListTest();
        }
        else if (userResponse.equals(TEXT_MESSAGE_OPTION.getPostbackData())) {
            // send text message
            sendRbmTextMessageTest();
        }
        else if (userResponse.equals(TEXT_WITH_CHIP_LIST_OPTION.getPostbackData())) {
            // send text with chip list
            sendRbmTextMessageWithChipListTest();
        }
        else if (userResponse.equals(RICH_CARD_OPTION.getPostbackData())) {
            // send rich card options (horizontal/vertical)
            sendRichCardOrientationOptions();
        }
        else if (userResponse.equals(USER_GENERATED.getPostbackData())) {
            // send options where a use sends the bot information
            sendUserGeneratedOptions();
        }
    }

    private void sendContentPrompt(String userResponse) {
        try {
            saveClientConfig(USER_CONTENT_SELECTION, userResponse);

            String messageText = "Type a message and send it to me.";

            if(userResponse.equals(USER_SENT_IMAGE.getPostbackData())) {
                messageText = "Select an image and send it to me.";
            }

            rbmApiHelper.sendTextMessage(
                    messageText,
                    this.msisdn
            );
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    private void sendUserGeneratedOptions() {
        try {
            List<Suggestion> suggestions = new ArrayList<Suggestion>();

            suggestions.add(USER_SENT_IMAGE.getSuggestedReply());
            suggestions.add(USER_SENT_TEXT.getSuggestedReply());

            String messageText = "What type of content do you want to send?";

            rbmApiHelper.sendTextMessage(
                    messageText,
                    this.msisdn,
                    suggestions
            );
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Sends the client an image in a rich card along with a chip list.
     */
    private void sendFile(String fileUrl, String messageText) {
        try {
            // create a standalone card
            StandaloneCard standaloneCard = rbmApiHelper.createStandaloneCard(
                    messageText,
                    null,
                    fileUrl,
                    MediaHeight.TALL,
                    CardOrientation.VERTICAL,
                null
            );

            rbmApiHelper.sendStandaloneCard(standaloneCard, msisdn);

            sendDefaultOptions("Pick another feature to explore:");
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Sends the client a rich card sample based on the options they have chosen.
     * @param userResponse The last response from the client.
     */
    private void sendRichCardExample(String userResponse) {
        try {
            String fileUrl = "";
            String thumbnailFileUrl = "";

            if (userResponse.equals(RICH_CARD_WITH_IMAGE.getPostbackData())
                    || userResponse.equals(RICH_CARD_WITH_IMAGE_THUMBNAIL.getPostbackData())) {
                fileUrl = CUTE_DOG_CARD.getImageFileUrl();

                // add the thumbnail if that option was selected
                if (userResponse.equals(RICH_CARD_WITH_IMAGE_THUMBNAIL.getPostbackData())) {
                    thumbnailFileUrl = CUTE_DOG_CARD.getImageFileUrl();
                }
            }
            else {
                fileUrl = SAMPLE_VIDEO_URL;

                // add the thumbnail if that option was selected
                if (userResponse.equals(RICH_CARD_WITH_VIDEO_THUMBNAIL.getPostbackData())) {
                    thumbnailFileUrl = SAMPLE_VIDEO_THUMBNAIL_URL;
                }
            }

            Entity clientConfig = getExistingClientConfig();

            // set defaults for richcard configuration
            ThumbnailImageAlignment thumbnailAlignment = ThumbnailImageAlignment.LEFT;
            MediaHeight height = MediaHeight.TALL;
            CardOrientation orientation = CardOrientation.VERTICAL;

            // set the rich card configuration options based on what the client has selected
            if (clientConfig != null) {
                orientation = CardOrientation.valueOf(clientConfig.getProperty(RICH_CARD_ORIENTATION).toString());

                if (clientConfig.getProperty(RICH_CARD_HEIGHT) != null) {
                    height = MediaHeight.valueOf(clientConfig.getProperty(RICH_CARD_HEIGHT).toString());
                }

                if (clientConfig.getProperty(RICH_CARD_ALIGNNMENT) != null) {
                    thumbnailAlignment = ThumbnailImageAlignment.valueOf(clientConfig.getProperty(RICH_CARD_ALIGNNMENT).toString());
                }
            }

            // the title for the rich card
            String title = "I am a rich card.";

            // the description for the rich card
            String description = "I am a description.";

            // create a standalone card
            StandaloneCard standaloneCard = rbmApiHelper.createStandaloneCard(
                    title,
                    description,
                    fileUrl,
                    height,
                    orientation,
                    null
            );

            if(thumbnailFileUrl.length() > 0) {
                // set the thumbnail details
                standaloneCard.getCardContent().getMedia().getContentInfo().setThumbnailUrl(thumbnailFileUrl);
                standaloneCard.setThumbnailImageAlignment(thumbnailAlignment.toString());
            }

            rbmApiHelper.sendStandaloneCard(standaloneCard, msisdn);
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Asks the client whether they want a vertical or horizontal rich card.
     */
    private void sendRichCardOrientationOptions() {
        try {
            List<Suggestion> suggestions = new ArrayList<Suggestion>();

            suggestions.add(VERTICAL_RICH_CARD_OPTION.getSuggestedReply());
            suggestions.add(HORIZONTAL_RICH_CARD_OPTION.getSuggestedReply());

            String messageText = "Choose an orientation for the rich card:";

            rbmApiHelper.sendTextMessage(
                    messageText,
                    this.msisdn,
                    suggestions
            );
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Based on the orientation choice by the user, asks about sizing and alignment for the card.
     * @param userResponse The orientation choice from the user.
     */
    private void sendRichCardMediaOptions(String userResponse) {
        // save the orientation to a datastore
        saveClientConfig(RICH_CARD_ORIENTATION, userResponse);

        // send client the rich card size or alignment options based on the orientation they chose
        if (userResponse.equals("VERTICAL")) {
            sendVerticalCardOptions();
        }
        else {
            sendHorizontalCardOptions();
        }
    }

    /**
     * Sends the client the vertical rich card sizing options.
     */
    private void sendVerticalCardOptions() {
        try {
            List<Suggestion> suggestions = new ArrayList<Suggestion>();

            // create sizing suggestions
            suggestions.add(VERTICAL_HEIGHT_SHORT_OPTION.getSuggestedReply());
            suggestions.add(VERTICAL_HEIGHT_MEDIUM_OPTION.getSuggestedReply());
            suggestions.add(VERTICAL_HEIGHT_TALL_OPTION.getSuggestedReply());

            String messageText = "Choose the rich card image size:";

            rbmApiHelper.sendTextMessage(
                    messageText,
                    this.msisdn,
                    suggestions
            );
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Sends the client the horizontal rich card alignment options.
     */
    private void sendHorizontalCardOptions() {
        try {
            List<Suggestion> suggestions = new ArrayList<Suggestion>();

            // create alignment suggestions
            suggestions.add(HORIZONTAL_LEFT_THUMBNAIL_OPTION.getSuggestedReply());
            suggestions.add(HORIZONTAL_RIGHT_THUMBNAIL_OPTION.getSuggestedReply());

            String messageText = "Choose the thumbnail alignment:";

            rbmApiHelper.sendTextMessage(
                    messageText,
                    this.msisdn,
                    suggestions
            );
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Sends the client a chip list of choices for the content payload of the rich card.
     * @param richCardImageConfig The previously selected image config for the rich card.
     */
    private void sendRichCardContentOptions(String richCardImageConfig) {
        try {
            if (isRichCardHorizontalOptionResponse(richCardImageConfig)) {
                // save to the correct config key based on a horizontal rich card
                saveClientConfig(RICH_CARD_ALIGNNMENT, richCardImageConfig);
            } else {
                // save to the correct config key based on a vertical rich card
                saveClientConfig(RICH_CARD_HEIGHT, richCardImageConfig);
            }

            List<Suggestion> suggestions = new ArrayList<Suggestion>();

            // create suggestions for the content type to send
            suggestions.add(RICH_CARD_WITH_IMAGE.getSuggestedReply());
            suggestions.add(RICH_CARD_WITH_VIDEO.getSuggestedReply());
            suggestions.add(RICH_CARD_WITH_IMAGE_THUMBNAIL.getSuggestedReply());
            suggestions.add(RICH_CARD_WITH_VIDEO_THUMBNAIL.getSuggestedReply());

            String messageText = "Choose the content type for the rich card:";

            rbmApiHelper.sendTextMessage(
                    messageText,
                    this.msisdn,
                    suggestions
            );
        } catch (IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Sends the client a response after they liked/disliked an image.
     * @param userResponse The postback data from the client.
     */
    private void sendImageResponseNotification(String userResponse) {
        try {
            String messageText = "Cool! I'm glad you liked the image 😀.";

            if (userResponse.equals(DISLIKE_ITEM.getPostbackData())) {
                messageText = "☹️ I'm sorry that you did not like the image.";
            }

            List<Suggestion> suggestions = getDefaultSuggestionList();

            rbmApiHelper.sendTextMessage(
                    messageText,
                    this.msisdn,
                    suggestions
            );
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Sends the client a plaintext RBM message.
     */
    private void sendRbmTextMessageTest() {
        try {
            String messageText = "This is a text message.";

            rbmApiHelper.sendTextMessage(
                    messageText,
                    this.msisdn
            );
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Sends the client an RBM message with a chip list.
     */
    private void sendRbmTextMessageWithChipListTest() {
        try {
            List<Suggestion> suggestions = getDefaultSuggestionList();

            String messageText = "This is a text message with a chip list.";

            rbmApiHelper.sendTextMessage(
                    messageText,
                    this.msisdn,
                    suggestions
            );
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Sends the client an RBM message with all possible RBM suggested actions.
     */
    private void sendRbmActionsChipListTest() {
        try {
            List<Suggestion> suggestedActions = new ArrayList<Suggestion>();

            // add all the RBM suggested actions
            suggestedActions.add(getCalendarAction());
            suggestedActions.add(getDialAction());
            suggestedActions.add(getViewLocationAction());
            suggestedActions.add(getUrlAction());
            suggestedActions.add(getShareLocationAction());
            suggestedActions.add(getFunVideoAction());

            String messageText = "This is a chip list of suggested actions.";

            rbmApiHelper.sendTextMessage(
                    messageText,
                    this.msisdn,
                    suggestedActions
            );
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Checks to see if we should ignore the user's response.
     * @param userResponse The last response from the client.
     * @return True if we should ignore.
     */
    private boolean isIgnoredResponse(String userResponse) {
        return userResponse.equals("share_location");
    }

    /**
     * Checks to see if the response contains location information for lat, lon.
     * @return True if the response now is due to user generated content.
     */
    private boolean isLatLng(String userResponse) {
        return userResponse.indexOf("latitude") >= 0;
    }

    /**
     * Checks to see if the response the bot is receiving happened after a user content event.
     * @return True if the response now is due to user generated content.
     */
    private boolean isUserSentContent() {
        Entity clientConfig = getExistingClientConfig();

        return clientConfig != null
                && clientConfig.getProperty(USER_CONTENT_SELECTION) != null
                && clientConfig.getProperty(USER_CONTENT_SELECTION).toString().length() > 0;
    }

    /**
     * Checks to see if the response is the client selecting to send the bot an image or text.
     * @param userResponse The last response from the user.
     * @return True if the last user response is for sending an image or text to the bot.
     */
    private boolean isUserContentSelection(String userResponse) {
        return userResponse.equals(USER_SENT_IMAGE.getPostbackData())
                || userResponse.equals(USER_SENT_TEXT.getPostbackData());
    }

    /**
     * Checks the user response to see if they selected one of the RBM features.
     * @param userResponse The postback data from the user.
     * @return True if the user selected an RBM feature to explore.
     */
    private boolean isRbmTestFunctionality(String userResponse) {
        List<Suggestion> suggestions = getDefaultSuggestionList();

        for(Suggestion suggestion : suggestions) {
            if(suggestion.getReply().getPostbackData().equals(userResponse)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks to see if the user has selected rich card height or alignment option.
     * @param userResponse The last response from the user.
     * @return True if the user made a rich card height or alignment choice.
     */
    private boolean isRichCardOptionResponse(String userResponse) {
        return isRichCardHorizontalOptionResponse(userResponse)
                || isRichCardVerticalOptionResponse(userResponse);
    }

    /**
     * Checks to see if the user made an alignment choice for their rich card.
     * @param userResponse The last response from the user.
     * @return True if the user chose an alignment.
     */
    private boolean isRichCardHorizontalOptionResponse(String userResponse) {
        return userResponse.equals(HORIZONTAL_LEFT_THUMBNAIL_OPTION.getPostbackData())
                || userResponse.equals(HORIZONTAL_RIGHT_THUMBNAIL_OPTION.getPostbackData());
    }

    /**
     * Checks to see if the user made a height choice for their rich card.
     * @param userResponse The last response from the user.
     * @return True if the user chose a height.
     */
    private boolean isRichCardVerticalOptionResponse(String userResponse) {
        return userResponse.equals(VERTICAL_HEIGHT_SHORT_OPTION.getPostbackData())
                || userResponse.equals(VERTICAL_HEIGHT_MEDIUM_OPTION.getPostbackData())
                || userResponse.equals(VERTICAL_HEIGHT_TALL_OPTION.getPostbackData());
    }

    /**
     * Checks the user response to see if they responded like/dislike on an image that was sent.
     * @param userResponse The postback data from the user.
     * @return True if the user reacted to an image.
     */
    private boolean isImageResponse(String userResponse) {
        return userResponse.equals(LIKE_ITEM.getPostbackData())
                || userResponse.equals(DISLIKE_ITEM.getPostbackData());
    }

    /**
     * Checks the user response to see if they selected a rich card orientation.
     * @param userResponse The postback data from the user.
     * @return True if the user chose a rich card orientation.
     */
    private boolean isRichCardChoiceResponse(String userResponse) {
        return userResponse.equals(VERTICAL_RICH_CARD_OPTION.getPostbackData())
                || userResponse.equals(HORIZONTAL_RICH_CARD_OPTION.getPostbackData());
    }

    /**
     * Checks the user response to see if they selected a rich card content type.
     * @param userResponse The postback data from the user.
     * @return True if the user chose a rich card content type.
     */
    private boolean isRichCardContentResponse(String userResponse) {
        return userResponse.equals(RICH_CARD_WITH_IMAGE.getPostbackData())
                || userResponse.equals(RICH_CARD_WITH_VIDEO.getPostbackData())
                || userResponse.equals(RICH_CARD_WITH_IMAGE_THUMBNAIL.getPostbackData())
                || userResponse.equals(RICH_CARD_WITH_VIDEO_THUMBNAIL.getPostbackData());
    }

    /**
     * Sends the client a chip list of RBM features to explore with a default message.
     */
    private void sendDefaultOptions() {
        sendDefaultOptions("Welcome! Pick a feature to explore.");
    }

    /**
     * Sends the client a chip list of RBM features to explore.
     * @param messageText The text of the message.
     */
    private void sendDefaultOptions(String messageText) {
        try {
            List<Suggestion> suggestions = getDefaultSuggestionList();

            rbmApiHelper.sendTextMessage(
                    messageText,
                    this.msisdn,
                    suggestions
            );
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Creates the default set of suggested replies for a client.
     * @return A list of suggested replies.
     */
    private List<Suggestion> getDefaultSuggestionList() {
        List<Suggestion> suggestions = new ArrayList<Suggestion>();

        suggestions.add(CAROUSEL_OPTION.getSuggestedReply());
        suggestions.add(ACTIONS_CHIP_LIST_OPTION.getSuggestedReply());
        suggestions.add(TEXT_MESSAGE_OPTION.getSuggestedReply());
        suggestions.add(TEXT_WITH_CHIP_LIST_OPTION.getSuggestedReply());
        suggestions.add(RICH_CARD_OPTION.getSuggestedReply());
        suggestions.add(USER_GENERATED.getSuggestedReply());

        return suggestions;
    }

    /**
     * Sends a carousel card to the client with example images.
     */
    private void sendSampleCarousel() {
        // list of card content for the carousel
        List<CardContent> cardContents = new ArrayList<CardContent>();

        // add items as card content
        cardContents.add(ADVENTURE_CLIFF_CARD.getCardContent(MediaHeight.SHORT));
        cardContents.add(CUTE_DOG_CARD.getCardContent(MediaHeight.SHORT));
        cardContents.add(ELEPHANT_CARD.getCardContent(MediaHeight.SHORT));
        cardContents.add(GOLDEN_GATE_CARD.getCardContent(MediaHeight.SHORT));
        cardContents.add(SHEEP_CARD.getCardContent(MediaHeight.SHORT));

        try {
            // send the message to the user
            rbmApiHelper.sendCarouselCards(cardContents, CardWidth.MEDIUM, msisdn);
        } catch(IOException e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Creates a calendar suggested action.
     * @return A suggestion object for a calendar action.
     */
    private Suggestion getCalendarAction() {
        // create a calendar object to create a sample start date and time for the calendar action
        Calendar cal = Calendar.getInstance();

        // adds one month to current month
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);

        // initialize a start date for this fake start date and time
        Date startDate = cal.getTime();

        // add travel time to the existing calendar object
        cal.add(Calendar.HOUR, 14);
        cal.add(Calendar.MINUTE, 2);

        // initialize an end date for this fake arrival date and time
        Date endDate = cal.getTime();

        // needed for formatting the date/time for the API
        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        String startTime = format.format(startDate);
        String endTime = format.format(endDate);

        // create the calendar event action for the trip
        CreateCalendarEventAction calendarEventAction = new CreateCalendarEventAction();
        calendarEventAction.setTitle("RBM Agent Explorer");
        calendarEventAction.setDescription("Calendar event created by the RBM Kitchen Sink");
        calendarEventAction.setStartTime(startTime);
        calendarEventAction.setEndTime(endTime);

        // attach the calendar action to a suggested action
        SuggestedAction suggestedAction = new SuggestedAction();
        suggestedAction.setCreateCalendarEventAction(calendarEventAction);
        suggestedAction.setText(CALENDAR_OPTION.getText());
        suggestedAction.setPostbackData(CALENDAR_OPTION.getPostbackData());

        // attach the action to a suggestion object
        Suggestion suggestion = new Suggestion();
        suggestion.setAction(suggestedAction);

        return suggestion;
    }

    /**
     * Creates a phone call suggested action.
     * @return A suggestion object for a dial action.
     */
    private Suggestion getDialAction() {
        // creating a dial an agent suggested action
        DialAction dialAction = new DialAction();
        dialAction.setPhoneNumber("+12223334444");

        // creating a suggested action based on a dial action
        SuggestedAction suggestedAction = new SuggestedAction();
        suggestedAction.setText(DIAL_OPTION.getText());
        suggestedAction.setPostbackData(DIAL_OPTION.getPostbackData());
        suggestedAction.setDialAction(dialAction);

        // attaching action to a suggestion
        Suggestion suggestion = new Suggestion();
        suggestion.setAction(suggestedAction);

        return suggestion;
    }

    /**
     * Creates a open url suggested action for a YouTube video.
     * @return A suggestion object for an open URL action.
     */
    private Suggestion getFunVideoAction() {
        return getUrlAction(FUN_VIDEO_OPTION, "https://www.youtube.com/embed/xSE9Qk9wkig");
    }

    /**
     * Creates a view location suggested action.
     * @return A suggestion object for a view location action.
     */
    private Suggestion getViewLocationAction() {
        // create an open url action
        ViewLocationAction viewLocationAction = new ViewLocationAction();
        viewLocationAction.setQuery("Googleplex Mountain View, CA");

        // attach the open url action to a suggested action
        SuggestedAction suggestedAction = new SuggestedAction();
        suggestedAction.setViewLocationAction(viewLocationAction);
        suggestedAction.setText(VIEW_LOCATION_OPTION.getText());
        suggestedAction.setPostbackData(VIEW_LOCATION_OPTION.getPostbackData());

        // attach the action to a suggestion object
        Suggestion suggestion = new Suggestion();
        suggestion.setAction(suggestedAction);

        return suggestion;
    }

    /**
     * Creates an open URL suggested action.
     * @return A suggestion object for an open URL action.
     */
    private Suggestion getUrlAction() {
        return getUrlAction(URL_OPTION, "https://www.google.com");
    }

    /**
     * Creates a share location suggested action.
     * @return A suggestion object for a share location action.
     */
    private Suggestion getShareLocationAction() {
        // create an open url action
        ShareLocationAction shareLocationAction = new ShareLocationAction();

        // attach the open url action to a suggested action
        SuggestedAction suggestedAction = new SuggestedAction();
        suggestedAction.setShareLocationAction(shareLocationAction);
        suggestedAction.setText(SHARE_LOCATION_OPTION.getText());
        suggestedAction.setPostbackData(SHARE_LOCATION_OPTION.getPostbackData());

        // attach the action to a suggestion object
        Suggestion suggestion = new Suggestion();
        suggestion.setAction(suggestedAction);

        return suggestion;
    }

    /**
     * Creates a generic open URL suggested action.
     * @return A suggestion object for an open URL action.
     */
    private Suggestion getUrlAction(SuggestionHelper suggestionHelper, String url) {
        // create an open url action
        OpenUrlAction openUrlAction = new OpenUrlAction();
        openUrlAction.setUrl(url);

        // attach the open url action to a suggested action
        SuggestedAction suggestedAction = new SuggestedAction();
        suggestedAction.setOpenUrlAction(openUrlAction);
        suggestedAction.setText(suggestionHelper.getText());
        suggestedAction.setPostbackData(suggestionHelper.getPostbackData());

        // attach the action to a suggestion object
        Suggestion suggestion = new Suggestion();
        suggestion.setAction(suggestedAction);

        return suggestion;
    }

    /**
     * Saves config information about the choices a user has made.
     * @param key The key associated with the config choice.
     * @param value The value for the key.
     */
    private void saveClientConfig(String key, String value) {
        Entity currentConfig = getExistingClientConfig();

        try {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

            // create a new config for the datastore if we do not have one already
            if (currentConfig == null) {
                currentConfig = new Entity("RbmConfig");
                currentConfig.setProperty("created_date", new Date().getTime());
                currentConfig.setProperty("msisdn", msisdn);
            }

            // set the key/value that was passed in
            currentConfig.setProperty(key, value);

            datastore.put(currentConfig);
        } catch (Exception e) {
            logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
        }
    }

    /**
     * Checks the datastore for the current client decisions.
     * @return A datastore entity if there exists one.
     */
    private Entity getExistingClientConfig() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // retrieve all messages matching the msisdn
        final Query q = new Query("RbmConfig")
                .setFilter(
                        new Query.FilterPredicate("msisdn",
                                Query.FilterOperator.EQUAL,
                                msisdn)
                );

        PreparedQuery pq = datastore.prepare(q);

        List<Entity> currentConfig = pq.asList(FetchOptions.Builder.withLimit(1));

        // return the current configuration settings
        if (!currentConfig.isEmpty()) {
            return currentConfig.get(0);
        }

        return null;
    }
}
// [END of the chat bot for the RBM Kitchen Sink Bot]