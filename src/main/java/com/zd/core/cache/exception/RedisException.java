package com.zd.core.cache.exception;

public class RedisException extends RuntimeException {

    private static final long serialVersionUID = 1169479264795633109L;

    public RedisException(String message) {
        super(message);
    }

    public RedisException(Throwable e) {
        super(e);
    }

    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }
}
