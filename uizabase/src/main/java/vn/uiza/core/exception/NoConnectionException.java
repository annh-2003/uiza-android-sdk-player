package vn.uiza.core.exception;

/**
 * Created by loitp on 5/21/2018.
 */

public class NoConnectionException extends Exception {

    public NoConnectionException(String message) {
        super(message);
    }

    public NoConnectionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
