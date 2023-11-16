package com.tqz.multithread.transaction.demo.exception;

/**
 * @author tianqingzhao
 * @since 2023/11/16 10:05
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String msg;

    private int code = 500;

    public ServiceException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public ServiceException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public ServiceException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public ServiceException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }

}
