package com.haolin.haotool.exections;

import java.security.PrivilegedActionException;
import java.util.Map;

public class ParseSQLException extends Exception {

    /**
     * 参数列表
     */
    private Map<String, Object> paramMap;

    private String mid;

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public ParseSQLException(Map<String, Object> paramMap, String mid) {
        this.paramMap = paramMap;
        this.mid = mid;
    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public ParseSQLException(String message, Map<String, Object> paramMap, String mid) {
        super(message);
        this.paramMap = paramMap;
        this.mid = mid;
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public ParseSQLException(String message, Throwable cause, Map<String, Object> paramMap, String mid) {
        super(message, cause);
        this.paramMap = paramMap;
        this.mid = mid;
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * PrivilegedActionException}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public ParseSQLException(Throwable cause, Map<String, Object> paramMap, String mid) {
        super(cause);
        this.paramMap = paramMap;
        this.mid = mid;
    }

    /**
     * Constructs a new exception with the specified detail message,
     * cause, suppression enabled or disabled, and writable stack
     * trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause.  (A {@code null} value is permitted,
     *                           and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable
     * @since 1.7
     */
    public ParseSQLException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Map<String, Object> paramMap, String mid) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.paramMap = paramMap;
        this.mid = mid;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }
}
