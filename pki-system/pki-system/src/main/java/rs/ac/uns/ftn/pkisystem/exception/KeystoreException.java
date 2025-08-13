package rs.ac.uns.ftn.pkisystem.exception;

public class KeystoreException extends RuntimeException {
    public KeystoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeystoreException(String message) {
        super(message);
    }
}