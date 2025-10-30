package com.textlinksms.textlink;

public class QueuedSMS {
    private final Integer id;
    private final String phoneNumber;
    private final String text;

    private  final String simCardName;

    public QueuedSMS(Integer id, String phoneNumber, String text, String simCardName) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.text = text;
        this.simCardName = simCardName;
    }

    public Integer getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getText() {
        return text;
    }

    public String getSimCardName() {
        return simCardName;
    }

    @Override
    public String toString() {
        return "QueuedSMS{" +
                "id=" + id +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
