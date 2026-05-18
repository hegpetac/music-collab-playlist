package hu.hegpetac.music.collab.playlist.be.playlist.entity;

public enum Provider {
    YOUTUBE("YOUTUBE"),

    SPOTIFY("SPOTIFY");

    private final String value;

    Provider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
