package hu.hegpetac.music.collab.playlist.be.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
