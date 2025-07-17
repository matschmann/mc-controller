package com.example.musiccast.model;

public class Room {
    private String name;
    private String ipAddress;
    private String zone;
    private boolean powerOn;
    private int volume;
    private boolean muted;
    private String currentTrack;
    private String playbackStatus;

    public Room(String name, String ipAddress) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.zone = "main"; // Default zone for most MusicCast devices
        this.powerOn = false;
        this.volume = 50;
        this.muted = false;
        this.currentTrack = "No track playing";
        this.playbackStatus = "stop";
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public boolean isPowerOn() {
        return powerOn;
    }

    public void setPowerOn(boolean powerOn) {
        this.powerOn = powerOn;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public String getCurrentTrack() {
        return currentTrack;
    }

    public void setCurrentTrack(String currentTrack) {
        this.currentTrack = currentTrack;
    }

    public String getPlaybackStatus() {
        return playbackStatus;
    }

    public void setPlaybackStatus(String playbackStatus) {
        this.playbackStatus = playbackStatus;
    }
}
