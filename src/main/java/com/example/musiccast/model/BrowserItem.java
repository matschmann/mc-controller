package com.example.musiccast.model;

public class BrowserItem {
    private String text;
    private String attribute;
    private boolean playable;
    private boolean container;
    private int index;
    private String albumartUrl;

    public BrowserItem() {}

    public BrowserItem(String text, String attribute, boolean playable, boolean container, int index) {
        this.text = text;
        this.attribute = attribute;
        this.playable = playable;
        this.container = container;
        this.index = index;
    }

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public boolean isPlayable() {
        return playable;
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
    }

    public boolean isContainer() {
        return container;
    }

    public void setContainer(boolean container) {
        this.container = container;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getAlbumartUrl() {
        return albumartUrl;
    }

    public void setAlbumartUrl(String albumartUrl) {
        this.albumartUrl = albumartUrl;
    }

    public String getIcon() {
        if (container) {
            return "📁"; // Folder icon
        } else if (playable) {
            if (text.toLowerCase().contains("mp3") || text.toLowerCase().contains("music")) {
                return "🎵"; // Music note
            } else {
                return "🎧"; // Audio file
            }
        } else {
            return "📄"; // Document
        }
    }
}
