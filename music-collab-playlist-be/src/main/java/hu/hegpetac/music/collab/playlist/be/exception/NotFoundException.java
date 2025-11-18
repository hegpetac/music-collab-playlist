package hu.hegpetac.music.collab.playlist.be.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
