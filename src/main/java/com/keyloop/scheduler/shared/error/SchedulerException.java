package com.keyloop.scheduler.shared.error;

public class SchedulerException extends RuntimeException {
    private final ErrorCode code;

    public SchedulerException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
