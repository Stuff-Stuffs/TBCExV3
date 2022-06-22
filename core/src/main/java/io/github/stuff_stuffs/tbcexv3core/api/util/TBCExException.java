package io.github.stuff_stuffs.tbcexv3core.api.util;

public class TBCExException extends RuntimeException {
    public TBCExException() {
    }

    public TBCExException(final String message) {
        super(message);
    }

    public TBCExException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TBCExException(final Throwable cause) {
        super(cause);
    }
}
