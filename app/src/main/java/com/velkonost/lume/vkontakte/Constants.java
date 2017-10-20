package com.velkonost.lume.vkontakte;

/**
 * @author Velkonost
 */

public class Constants {


    public static final class VIEW_PAGER_PAGES {
        public static String DIALOGS_PAGE = "dialogs";
        public static String FRIENDS_PAGE = "friends";
        public static String SETTINGS_PAGE = "settings";
    }


    public static final class API_METHODS {
        public static String GET_PROFILE_INFO = "account.getProfileInfo";
        public static String GET_MESSAGES = "messages.getHistory";
        public static String SEND_MESSAGE = "messages.send";
        public static String GET_LONG_POLL_SERVER = "messages.getLongPollServer";

    }

    public static final class API_PARAMETERS {
        public static String FIELDS = "fields";
        public static String ORDER = "order";
        public static String COUNT = "count";

        public static int AMOUNT_DIALOGS = 10;
        public static int AMOUNT_MESSAGES = 10;
        public static String ID = "id";
        public static String HINTS = "hints";
        public static String FIRST_NAME = "first_name";
        public static String LAST_NAME = "last_name";
        public static String DOMAIN = "domain";
    }

    public static final class RESPONSE_FIELDS {
        public static final String RESPONSE = "response";
        public static final String ITEMS = "items";
        public static final String MESSAGE = "message";
        public static final String CHAT_ID = "chat_id";
        public static final String USER_ID = "user_id";
    }

    public static final class MESSAGES_DATA {
        public static String MESSAGES_IDS = "messagesIds";
        public static String MESSAGES_BODIES = "messagesBodies";
        public static String MESSAGES_SENDERS = "messagesSenders";
        public static String MESSAGES_IS_OUT = "messagesIsOut";
        public static String MESSAGES_DATES = "messagesDates";

        public static String FWD_MESSAGES_DATES_LISTS = "fwdMessagesBodiesLists";
        public static String FWD_MESSAGES_SENDERS_LISTS = "fwdMessagesDatesLists";
        public static String FWD_MESSAGES_BODIES_LISTS = "fwdMessagesSendersLists";
    }







}
