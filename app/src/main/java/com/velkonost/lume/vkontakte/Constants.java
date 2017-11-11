package com.velkonost.lume.vkontakte;

/**
 * @author Velkonost
 */

public class Constants {

    public static final int REFRESH_MESSAGES_PERIOD = 3000;

    public static final class VIEW_PAGER_PAGES {
        public static final String DIALOGS_PAGE = "dialogs";
        public static final String FRIENDS_PAGE = "friends";
        public static String SETTINGS_PAGE = "settings";
    }


    public static final class API_METHODS {
        public static final String GET_PROFILE_INFO = "account.getProfileInfo";
        public static final String GET_MESSAGES = "messages.getHistory";
        public static final String SEND_MESSAGE = "messages.send";
        public static final String CREATE_CHAT = "messages.createChat";
        public static final String GET_LONG_POLL_SERVER = "messages.getLongPollServer";
        public static final String GET_LONG_POLL_MESSAGES_HISTORY = "messages.getLongPollHistory";
        public static final String SEARCH_USERS = "users.search";
        public static final String ADD_FRIEND = "friends.add";
        public static final String DELETE_FRIEND = "friends.delete";

    }

    public static final class API_PARAMETERS {
        public static final String FIELDS = "fields";
        public static final String OFFSET = "offset";
        public static final String ORDER = "order";
        public static final String COUNT = "count";

        public static final int AMOUNT_DIALOGS = 10;
        public static final int AMOUNT_MESSAGES = 10;

        public static final String ID = "id";
        public static final String HINTS = "hints";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String DOMAIN = "domain";


        public static final String TS = "ts";
        public static final String TS_MESSAGES = "ts_messages";
        public static final String TS_DIALOGS = "ts_dialogs";
        public static final String PTS = "pts";
        public static final String PTS_MESSAGES = "pts_messages";
        public static final String PTS_DIALOGS = "pts_dialogs";

        public static final String IS_NEED_PTS = "need_pts";

        public static final String QUERY = "q";
        public static final String START_MESSAGE_ID = "start_message_id";
        public static final String FORWARD_MESSAGES = "forward_messages";
    }

    public static final class RESPONSE_FIELDS {
        public static final String RESPONSE = "response";
        public static final String ITEMS = "items";
        public static final String MESSAGE = "message";
        public static final String MESSAGES = "messages";
        public static final String CHAT_ID = "chat_id";
        public static final String USER_ID = "user_id";
        public static final String USER_IDS = "user_ids";
        public static final String PHOTO_50 = "photo_100";
        public static final String TITLE = "title";
        public static final String BODY = "body";
    }

    public static final class MESSAGES_DATA {
        public static final String MESSAGES_IDS = "messagesIds";
        public static final String MESSAGES_BODIES = "messagesBodies";
        public static final String MESSAGES_SENDERS = "messagesSenders";
        public static final String MESSAGES_SENDERS_IDS = "messagesSendersIds";
        public static final String MESSAGES_IS_OUT = "messagesIsOut";
        public static final String MESSAGES_DATES = "messagesDates";

        public static final String FWD_MESSAGES_DATES_LISTS = "fwdMessagesBodiesLists";
        public static final String FWD_MESSAGES_SENDERS_LISTS = "fwdMessagesDatesLists";
        public static final String FWD_MESSAGES_BODIES_LISTS = "fwdMessagesSendersLists";
    }

}
