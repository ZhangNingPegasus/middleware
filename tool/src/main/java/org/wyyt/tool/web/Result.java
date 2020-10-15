package org.wyyt.tool.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.ToString;
import org.wyyt.tool.exception.ExceptionTool;

import java.io.Serializable;
import java.util.List;

/**
 * the uniformed response used for microservices.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         7/13/2020      Initialize   *
 * *****************************************************************
 */
@ToString
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public final class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long code;
    private String error;
    private Boolean success;
    private Long count;
    private T data;

    public Result() {
    }

    private Result(final long code,
                   final String error,
                   final T data,
                   final Long count,
                   final Boolean success) {
        this.code = code;
        this.error = error;
        this.data = data;
        this.count = count;
        this.success = success;
    }

    public static <T> Result<T> create(final ResultCode respondCode,
                                       final T data) {
        return new Result<>(respondCode.getCode(), respondCode.getDescription(), data, null, respondCode.getSuccess());
    }

    public static <T> Result<T> create(final ResultCode respondCode) {
        return create(respondCode, null);
    }

    public static <T> Result<T> success(final long code,
                                        final T data) {
        return new Result<>(code, null, data, null, true);
    }

    public static <T> Result<List<T>> success(final List<T> data,
                                              final long count) {
        return new Result<>(ResultCode.SUCCESS.getCode(), null, data, count, ResultCode.SUCCESS.getSuccess());
    }

    public static <T> Result<T> success(final T data) {
        return success(ResultCode.SUCCESS.getCode(), data);
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(final long code,
                                      final String error,
                                      final T data) {
        return new Result<>(code, error, data, null, false);
    }

    public static <T> Result<T> error(final long code,
                                      final Exception exception,
                                      final T data) {
        return error(code, ExceptionTool.getRootCauseMessage(exception), data);
    }

    public static <T> Result<T> error(final long code,
                                      final String error) {
        return error(code, error, null);
    }

    public static <T> Result<T> error(final long code,
                                      final Exception exception) {
        return error(code, exception, null);
    }

    public static <T> Result<List<T>> error(final String error,
                                            final List<T> data,
                                            final long count) {
        return new Result<>(ResultCode.ERROR.getCode(), error, data, count, ResultCode.ERROR.getSuccess());
    }

    public static <T> Result<T> error(final T data) {
        return error(ResultCode.ERROR.getCode(), "", data);
    }

    public static <T> Result<T> error(final String error) {
        return error(ResultCode.ERROR.getCode(), error, null);
    }

    public static <T> Result<T> error() {
        return error(null);
    }
}