package com.example.padel;


public class Reservation {
    private String court;
    private String date;
    private String time;
    private String players;

    public Reservation(String court, String date, String time, String players) {
        this.court = court;
        this.date = date;
        this.time = time;
        this.players = players;
    }

    public String getCourt() { return court; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getPlayers() { return players; }

    public String getQrContent() {
        return court + " | " + date + " | " + time + " | " + players;
    }
}
