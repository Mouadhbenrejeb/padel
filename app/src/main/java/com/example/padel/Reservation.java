package com.example.padel;

/**
 * Model class representing a Reservation in Firebase
 */
public class Reservation {
    private String id;
    private String userId;
    private String court;
    private String date;
    private String time;
    private String players;
    private String status;

    // Required empty constructor for Firebase
    public Reservation() {
    }

    public Reservation(String court, String date, String time, String players) {
        this.court = court;
        this.date = date;
        this.time = time;
        this.players = players;
        this.status = "confirmed";
    }

    public Reservation(String id, String userId, String court, String date, String time, String players, String status) {
        this.id = id;
        this.userId = userId;
        this.court = court;
        this.date = date;
        this.time = time;
        this.players = players;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourt() {
        return court;
    }

    public void setCourt(String court) {
        this.court = court;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPlayers() {
        return players;
    }

    public void setPlayers(String players) {
        this.players = players;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQrContent() {
        if (players != null && !players.isEmpty()) {
            return court + " | " + date + " | " + time + " | " + players;
        }
        return court + " | " + date + " | " + time;
    }
}
