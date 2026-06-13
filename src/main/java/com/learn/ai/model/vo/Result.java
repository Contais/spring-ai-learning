package com.learn.ai.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Result<T> {
    private Integer ok;
    private String msg;
    private T data;

    private Result(Integer ok, String msg) {
        this.ok = ok;
        this.msg = msg;
    }

    private Result(Integer ok, String msg, T data) {
        this.ok = ok;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok() {
        return new Result<>(1, "ok");
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(1, "ok", data);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(0, msg);
    }
}