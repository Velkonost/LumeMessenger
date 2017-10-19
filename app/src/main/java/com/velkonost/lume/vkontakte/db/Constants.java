package com.velkonost.lume.vkontakte.db;

/**
 * @author Velkonost
 */

class Constants {
    final class DB_TABLES {
        /**
         * Таблица пользователей
         */
        static final String USERS = "vkontakteUsersTable";

        /**
         * Таблица сообщений диалогов
         */
        static final String MESSAGES = "vkontakteMessagesTable";

        /**
         * Таблица пересланных сообщений
         */
        static final String FWD_MESSAGES = "vkontakteFwdMessagesTable";

        /**
         * Таблица идентификаторов последних сообщений диалогов
         */
        static final String DIALOG_LAST_MESSAGE = "vkontakteDialogLastMessageTable";

    }

    static final String TABLE_PREFIX = "lume_";

    /**
     * Название таблицы
     */
    static final String DB_NAME = "lumeVkontaktedb";

}
