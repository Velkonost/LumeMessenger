package com.velkonost.lume.vkontakte.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import static com.velkonost.lume.Constants.DEBUG_TAG;
import static com.velkonost.lume.vkontakte.db.Constants.DB_NAME;
import static com.velkonost.lume.vkontakte.db.Constants.DB_TABLES.DIALOG_LAST_MESSAGE;
import static com.velkonost.lume.vkontakte.db.Constants.DB_TABLES.FWD_MESSAGES;
import static com.velkonost.lume.vkontakte.db.Constants.DB_TABLES.MESSAGES;
import static com.velkonost.lume.vkontakte.db.Constants.DB_TABLES.META_DATA;
import static com.velkonost.lume.vkontakte.db.Constants.DB_TABLES.USERS;
import static com.velkonost.lume.vkontakte.db.Constants.TABLE_PREFIX;

/**
 * @author Velkonost
 */

public class DBHelper extends SQLiteOpenHelper{
    private Context mContext;

    public DBHelper(Context context) {

            // конструктор суперкласса
            super(context, DB_NAME, null, 1);

            this.mContext = context;
        }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("LOG_DB", "--- onCreate database ---");

        createUsersTable(db);
        createMetaDataTable(db);
    }

    /**
     * Таблица пользователей
     */
    private void createUsersTable(SQLiteDatabase db){
        db.execSQL("create table if not exists " + TABLE_PREFIX + USERS + " ("
                + "id integer primary key autoincrement,"
                + "nickname text,"
                + "photo_50_url text,"
                + "user_id text" + ");");
    }

    /**
     * Таблица для пар ключ/значение
     */
    private void createMetaDataTable(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_PREFIX + META_DATA + " ("
                + "id integer primary key autoincrement,"
                + "meta_key text,"
                + "meta_value text" + ");");
    }

    /**
     * Таблица сообщений
     */
    private void createMessagesTable(SQLiteDatabase db){
        db.execSQL("create table if not exists " + TABLE_PREFIX + MESSAGES + " ("
                + "id integer primary key autoincrement,"
                + "message_id text,"
                + "dialog_id text,"
                + "body text,"
                + "is_out text,"
                + "sender text,"
                + "date text"
                + ");");
    }

    /**
     * Таблица пересланных сообщений
     */
    private void createFwdMessagesTable(SQLiteDatabase db){
        db.execSQL("create table if not exists " + TABLE_PREFIX + FWD_MESSAGES + " ("
                + "id integer primary key autoincrement,"
                + "message_id text,"
                + "body text,"
                + "sender text,"
                + "date text"
                + ");");

    }

    /**
     * Таблица последних сообщений диалогов
     */
    private void createDialogLastMessageTable(SQLiteDatabase db){
        db.execSQL("create table if not exists " + TABLE_PREFIX + DIALOG_LAST_MESSAGE + " ("
                + "id integer primary key autoincrement,"
                + "dialog_id text,"
                + "message_id text"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    /**
     * Добавление новой строки в таблицу пользователе
     * @param id - идентификатор пользователя
     * @param nickname - полное имя пользователя
     */
    public void insertUsers(String id, String nickname, String photo50Url) {
        if (this.getWritableDatabase().query(TABLE_PREFIX + USERS,
                null,
                "user_id = ?",
                new String[] {id},
                null, null, null).getCount() == 0) {
            ContentValues cvColumn = new ContentValues();
            cvColumn.put("nickname", nickname);
            cvColumn.put("user_id", id);
            cvColumn.put("photo_50_url", photo50Url);
            this.getWritableDatabase().insert(TABLE_PREFIX + USERS, null, cvColumn);
        }
    }

    /**
     * Добавление/изменение строки в таблице для хранения пар ключ/значение
     * @param key - ключ
     * @param value - значение
     */
    public void updateMetaData(String key, String value) {
        ContentValues cvColumn = new ContentValues();
        cvColumn.put("meta_key", key);
        cvColumn.put("meta_value", value);
        if (this.getWritableDatabase().query(TABLE_PREFIX + META_DATA,
                null,
                "meta_key = ?",
                new String[] {key},
                null, null, null).getCount() == 0) {
            this.getWritableDatabase().insert(TABLE_PREFIX + META_DATA, null, cvColumn);
        } else {
            this.getWritableDatabase().update(TABLE_PREFIX + META_DATA, cvColumn, "meta_key = ?",
                    new String[]{key});
        }
    }

    /**
     * Получение значение по ключу из таблицы для хранения пар ключ/значение
     * @param key - ключ
     * @return - значение
     */
    public String getValueFromMetaData(String key) {
        Cursor c = this.getWritableDatabase().query(TABLE_PREFIX + META_DATA,
                null,
                "meta_key = ?",
                new String[] {key},
                null, null, null);

        if (c.moveToFirst()) {
            int valueIndex = c.getColumnIndex("meta_value");
            return c.getString(valueIndex);
        }
        return " ";
    }

    /**
     * Добавление новой строки в таблицу собщений
     * @param dialogId - идентификатор диалога, которому принадлежит сообщение
     * @param messageId - идентификатор сообщения
     * @param body - тело сообщения
     * @param isOut - отправлено ли сообщение авторизованным пользователем
     * @param sender - автор сообщения
     * @param date - дата сообщения
     */
    public void insertMessages(String dialogId, String messageId, String body, boolean isOut, String sender, String date) {
        if (this.getWritableDatabase().query(TABLE_PREFIX + MESSAGES,
                null,
                "message_id = ?",
                new String[] {messageId},
                null, null, null).getCount() == 0) {
            ContentValues cvColumn = new ContentValues();
            cvColumn.put("dialog_id", dialogId);
            cvColumn.put("message_id", messageId);
            cvColumn.put("body", body);
            cvColumn.put("is_out", String.valueOf(isOut));
            cvColumn.put("sender", sender);
            cvColumn.put("date", date);

            ContentValues cvColumnD = new ContentValues();
            cvColumnD.put("dialog_id", dialogId);
            cvColumnD.put("message_id", messageId);



            this.getWritableDatabase().insert(TABLE_PREFIX + MESSAGES, null, cvColumn);

            if (this.getWritableDatabase().query(TABLE_PREFIX + DIALOG_LAST_MESSAGE,
                    null,
                    "dialog_id = ?",
                    new String[] {dialogId},
                    null, null, null).getCount() == 0) {
                this.getWritableDatabase().insert(TABLE_PREFIX + DIALOG_LAST_MESSAGE, null, cvColumnD);
                Log.i(DEBUG_TAG, "insert");
            } else {
                this.getWritableDatabase().update(TABLE_PREFIX + DIALOG_LAST_MESSAGE, cvColumnD, "dialog_id = ?",
                        new String[]{dialogId});
                Log.i(DEBUG_TAG, "update");
            }
        }
    }

    /**
     * Получение идентификатора последнего сообщения диалога
     * @param dialogId - идентификатор диалога
     * @return - идентификатор сообщения
     */
    public String getLastMessageOfDialog(String dialogId) {
        Cursor c = this.getWritableDatabase().query(TABLE_PREFIX + DIALOG_LAST_MESSAGE,
                null,
                "dialog_id = ?",
                new String[] {dialogId},
                null, null, null);

        if (c.moveToFirst()) {
            int messageIdIndex = c.getColumnIndex("message_id");
            return c.getString(messageIdIndex);
        }
        return " ";
    }

    /**
     * Добавление строки в таблицу пересланных сообщений
     * @param messageId - идентификатор сообщения
     * @param body - тело сообщения
     * @param sender - автор сообщения
     * @param date - дата сообщения
     */
    public void insertFwdMessages(String messageId, String body, String sender, String date) {
        ContentValues cvColumn = new ContentValues();
        cvColumn.put("message_id", messageId);
        cvColumn.put("body", body);
        cvColumn.put("sender", sender);
        cvColumn.put("date", date);

        this.getWritableDatabase().insert(TABLE_PREFIX + FWD_MESSAGES, null, cvColumn);
    }


    /**
     * Получение полного имени пользователя по его идентификатору
     * @param id - идентификатор пользователя
     * @return - полное имя пользователя
     */
    public String getFromUsersNicknameById(String id) {
        Cursor c = this.getWritableDatabase().query(TABLE_PREFIX + USERS,
                null,
                "user_id = ?",
                new String[] {id},
                null, null, null);

        if (c.moveToFirst()) {
            int nicknameIndex = c.getColumnIndex("nickname");
            return c.getString(nicknameIndex);
        }
        return null;
    }

    public String getFromUsersPhoto50UrlById(String id) {
        Cursor c = this.getWritableDatabase().query(TABLE_PREFIX + USERS,
                null,
                "user_id = ?",
                new String[] {id},
                null, null, null);

        if (c.moveToFirst()) {
            int photo50UrlIndex = c.getColumnIndex("photo_50_url");
            return c.getString(photo50UrlIndex);
        }
        return null;
    }

    /**
     * Получение сообщений диалога по его идентификатору
     * @param dialogId - идентификатор диалога
     * @param messagesIds - список идентификаторов сообщений
     * @param messagesBodies - список тел сообщений
     * @param messagesIsOut - булевский список направлений (отправлено сообщение авторизованным пользователем?)
     * @param messagesSenders - список отправителей сообщений
     * @param messagesDates - список дат сообщений
     * @return -
     */
    public Cursor getMessagesByDialogId(String dialogId, ArrayList<String> messagesIds,
                                        ArrayList<String> messagesBodies, ArrayList<Boolean> messagesIsOut,
                                        ArrayList<String> messagesSenders, ArrayList<String> messagesDates) {
        Cursor c = this.getWritableDatabase().query(TABLE_PREFIX + MESSAGES,
                null,
                "dialog_id = ?",
                new String[] {dialogId},
                null, null, null);

        if (c.moveToFirst()) {
            messagesIds.add(c.getString(c.getColumnIndex("message_id")));
            messagesBodies.add(c.getString(c.getColumnIndex("body")));
            messagesIsOut.add(Boolean.valueOf(c.getString(c.getColumnIndex("is_out"))));
            messagesSenders.add(c.getString(c.getColumnIndex("sender")));
            messagesDates.add(c.getString(c.getColumnIndex("date")));
            while (c.moveToNext()) {
                messagesIds.add(c.getString(c.getColumnIndex("message_id")));
                messagesBodies.add(c.getString(c.getColumnIndex("body")));
                messagesIsOut.add(Boolean.valueOf(c.getString(c.getColumnIndex("is_out"))));
                messagesSenders.add(c.getString(c.getColumnIndex("sender")));
                messagesDates.add(c.getString(c.getColumnIndex("date")));
            }


        }
        return null;
    }



    //    ui for check db
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }

}
