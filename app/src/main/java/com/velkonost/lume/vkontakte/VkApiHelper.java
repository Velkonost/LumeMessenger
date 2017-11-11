package com.velkonost.lume.vkontakte;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;

import static com.velkonost.lume.Constants.COMMA;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.ADD_FRIEND;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.CREATE_CHAT;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.DELETE_FRIEND;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.GET_LONG_POLL_MESSAGES_HISTORY;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.GET_LONG_POLL_SERVER;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.GET_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.SEARCH_USERS;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.SEND_MESSAGE;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.AMOUNT_DIALOGS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.AMOUNT_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.COUNT;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.DOMAIN;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIELDS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIRST_NAME;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FORWARD_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.HINTS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.IS_NEED_PTS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.LAST_NAME;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.OFFSET;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ORDER;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.PTS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.QUERY;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.START_MESSAGE_ID;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.TS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.PHOTO_50;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.TITLE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.USER_ID;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.USER_IDS;

/**
 * @author Velkonost
 */

public class VkApiHelper {


    public static VKRequest getAuthUserData(String userId) {
        return VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, userId, FIELDS, PHOTO_50));
    }

    public static VKRequest getFriends () {
        return VKApi.friends().get(VKParameters.from(
                FIELDS, FIRST_NAME + COMMA + LAST_NAME + COMMA + DOMAIN + COMMA + PHOTO_50,
                ORDER, HINTS
        ));
    }

    public static VKRequest getDialogs() {
        return VKApi.messages().getDialogs(VKParameters.from(COUNT, AMOUNT_DIALOGS));
    }

    public static VKRequest getDialogs(int offset) {
        return VKApi.messages().getDialogs(VKParameters.from(
                COUNT, AMOUNT_DIALOGS,
                OFFSET, offset
        ));
    }

    public static VKRequest getUserInfoById(String userId) {
        return VKApi.users().get(VKParameters.from(
                VKApiConst.USER_ID, userId,
                FIELDS, PHOTO_50
        ));
    }

    public static VKRequest getMessagesOfDialog(String typeOfDialog, String id) {
        return new VKRequest(
                GET_MESSAGES,
                VKParameters.from(typeOfDialog, id, COUNT, AMOUNT_MESSAGES)
        );
    }

    public static VKRequest getMessagesOfDialog(String typeOfDialog, String dialogId, String lastMessageId) {
        return new VKRequest(GET_MESSAGES, VKParameters.from(
                typeOfDialog, dialogId,
                COUNT, AMOUNT_MESSAGES,
                START_MESSAGE_ID, lastMessageId
        ));
    }


    public static VKRequest sendMessage(
            String typeOfDialog, String id,
            String messageBody, String fwdMessagesToSendStr
    ) {
        return new VKRequest(
                SEND_MESSAGE,
                VKParameters.from(
                        typeOfDialog, id,
                        VKApiConst.MESSAGE, messageBody,
                        FORWARD_MESSAGES, fwdMessagesToSendStr
                )
        );
    }

    public static VKRequest getLongPollServer() {
        return new VKRequest(GET_LONG_POLL_SERVER, VKParameters.from(IS_NEED_PTS, true));
    }

    public static VKRequest getMessagesHistoryFromLongPollServer(String ts, String pts) {
        return new VKRequest(GET_LONG_POLL_MESSAGES_HISTORY, VKParameters.from(TS, ts, PTS, pts, FIELDS, ""));
    }

    public static VKRequest createNewChat(String chatTitle, String userIdsStr) {
        return new VKRequest(CREATE_CHAT, VKParameters.from(TITLE, chatTitle, USER_IDS, userIdsStr));
    }

    public static VKRequest deleteFriend(String userId) {
        return new VKRequest(
                DELETE_FRIEND,
                VKParameters.from(USER_ID, userId));
    }

    public static VKRequest addFriend(String userId) {
        return new VKRequest(ADD_FRIEND, VKParameters.from(USER_ID, userId));
    }

    public static VKRequest searchUsers(String query) {
        return new VKRequest(SEARCH_USERS, VKParameters.from(QUERY, query, FIELDS, PHOTO_50));
    }

}
