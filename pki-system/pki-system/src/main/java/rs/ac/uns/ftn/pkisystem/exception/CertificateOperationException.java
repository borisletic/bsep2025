package rs.ac.uns.ftn.pkisystem.exception;

public class CertificateOperationException extends RuntimeException {
    public CertificateOperationException(String message) {
        super(message);
    }

    public CertificateOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}