package com.velkonost.lume.vkontakte.structures;

import java.util.ArrayList;

/**
 * @author Velkonost
 */

public class MessagesList {

    /**
     * Список идентификаторов сообщений
     */
    private ArrayList<String> ids;

    /**
     * Список тел сообщений
     */
    private ArrayList<String> bodies;

    /**
     * Список дат сообщений
     */
    private ArrayList<String> dates;

    /**
     * Список отправителей сообщений
     */
    private ArrayList<String> senders;

    /**
     * Булевский список направлений сообщений (отправлено сообщено от авторизованного пользователя?)
     */
    private ArrayList<String> isOut;

    /**
     * Список списков тел пересланных сообщений
     */
    ArrayList< ArrayList<String> > fwdMessagesBodiesLists = new ArrayList<>();

    /**
     * Список списков отправителей пересланных сообщений
     */
    ArrayList< ArrayList<String> > fwdMessagesSendersLists = new ArrayList<>();

    /**
     * Список списков дат пересланных сообщений
     */
    ArrayList< ArrayList<String> > fwdMessagesDatesLists = new ArrayList<>();

    public MessagesList(ArrayList<String> ids, ArrayList<String> bodies, ArrayList<String> dates,
                        ArrayList<String> senders, ArrayList<String> isOut,
                        ArrayList< ArrayList<String> > fwdMessagesBodiesLists,
                        ArrayList< ArrayList<String> > fwdMessagesDatesLists,
                        ArrayList< ArrayList<String> > fwdMessagesSendersLists) {

        this.ids = ids;
        this.bodies = bodies;
        this.dates = dates;
        this.senders = senders;
        this.isOut = isOut;

        this.fwdMessagesBodiesLists = fwdMessagesBodiesLists;
        this.fwdMessagesDatesLists = fwdMessagesDatesLists;
        this.fwdMessagesSendersLists = fwdMessagesSendersLists;
    }

    public MessagesList(ArrayList<String> bodies, ArrayList<String> dates, ArrayList<String> senders) {
        this.bodies = bodies;
        this.dates = dates;
        this.senders = senders;
    }

    public int getMessagesAmount() {
        return bodies.size();
    }

    public String getMessageBodyByPosition(int position) {
        return bodies.get(position);
    }
    public String getMessageIdsByPosition(int position) {
        return ids.get(position);
    }

    public String getMessageDateByPosition(int position) {
        return dates.get(position);
    }

    public ArrayList<String> getFwdMessagesBodiesByPosition(int position) {
        return fwdMessagesBodiesLists.get(position);
    }

    public ArrayList<String> getFwdMessagesDatesByPosition(int position) {
        return fwdMessagesDatesLists.get(position);
    }

    public ArrayList<String> getFwdMessagesSendersByPosition(int position) {
        return fwdMessagesSendersLists.get(position);
    }


    public ArrayList<ArrayList<String>> getFwdMessagesBodiesLists() {
        return fwdMessagesBodiesLists;
    }

    public ArrayList<ArrayList<String>> getFwdMessagesSendersLists() {
        return fwdMessagesSendersLists;
    }

    public ArrayList<String> getIds() {
        return ids;
    }

    public ArrayList<ArrayList<String>> getFwdMessagesDatesLists() {
        return fwdMessagesDatesLists;
    }

    public String getMessageSendersByPosition(int position) {
        return senders.get(position);
    }

    public String getMessageIsOutByPosition(int position) {
        return isOut.get(position);
    }

    public void setBodies(ArrayList<String> bodies) {
        this.bodies = bodies;
    }

    public void setDates(ArrayList<String> dates) {
        this.dates = dates;
    }

    public void setSenders(ArrayList<String> attachments) {
        this.senders = senders;
    }

    public void setIsOut(ArrayList<String> isOut) {
        this.isOut = isOut;
    }

    public ArrayList<String> getBodies() {
        return bodies;
    }

    public ArrayList<String> getDates() {
        return dates;
    }

    public ArrayList<String> getSenders() {
        return senders;
    }

    public ArrayList<String> getIsOut() {
        return isOut;
    }
}
