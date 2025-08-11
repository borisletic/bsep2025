package rs.ac.uns.ftn.pkisystem.security;

public class AuditEventType {
    // Authentication events
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String LOGOUT = "LOGOUT";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String ACCOUNT_ACTIVATED = "ACCOUNT_ACTIVATED";
    public static final String PASSWORD_RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";
    public static final String PASSWORD_RESET_COMPLETED = "PASSWORD_RESET_COMPLETED";

    // User management events
    public static final String USER_CREATED = "USER_CREATED";
    public static final String USER_UPDATED = "USER_UPDATED";
    public static final String USER_DELETED = "USER_DELETED";
    public static final String CA_USER_CREATED = "CA_USER_CREATED";

    // Certificate events
    public static final String CERTIFICATE_ISSUED = "CERTIFICATE_ISSUED";
    public static final String CERTIFICATE_REVOKED = "CERTIFICATE_REVOKED";
    public static final String CERTIFICATE_DOWNLOADED = "CERTIFICATE_DOWNLOADED";
    public static final String CERTIFICATE_VIEWED = "CERTIFICATE_VIEWED";
    public static final String CSR_UPLOADED = "CSR_UPLOADED";

    // Template events
    public static final String TEMPLATE_CREATED = "TEMPLATE_CREATED";
    public static final String TEMPLATE_UPDATED = "TEMPLATE_UPDATED";
    public static final String TEMPLATE_DELETED = "TEMPLATE_DELETED";

    // Password manager events
    public static final String PASSWORD_ENTRY_CREATED = "PASSWORD_ENTRY_CREATED";
    public static final String PASSWORD_ENTRY_UPDATED = "PASSWORD_ENTRY_UPDATED";
    public static final String PASSWORD_ENTRY_DELETED = "PASSWORD_ENTRY_DELETED";
    public static final String PASSWORD_ENTRY_VIEWED = "PASSWORD_ENTRY_VIEWED";
    public static final String PASSWORD_SHARED = "PASSWORD_SHARED";
    public static final String PASSWORD_SHARE_REVOKED = "PASSWORD_SHARE_REVOKED";

    // Token events
    public static final String TOKEN_REVOKED = "TOKEN_REVOKED";
    public static final String ALL_TOKENS_REVOKED = "ALL_TOKENS_REVOKED";

    // System events
    public static final String SYSTEM_CONFIG_CHANGED = "SYSTEM_CONFIG_CHANGED";
    public static final String SECURITY_ALERT = "SECURITY_ALERT";
}