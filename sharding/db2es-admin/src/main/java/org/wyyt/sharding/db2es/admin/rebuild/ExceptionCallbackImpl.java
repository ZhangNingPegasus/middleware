package org.wyyt.sharding.db2es.admin.rebuild;

/**
 * The implemention of exception callback
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public class ExceptionCallbackImpl implements ExceptionCallback {
    private Exception exception;
    private ExceptionPropertyChanged exceptionPropertyChanged;

    public ExceptionCallbackImpl(final ExceptionPropertyChanged exceptionPropertyChanged) {
        this.exceptionPropertyChanged = exceptionPropertyChanged;
    }

    public Exception getException() {
        return this.exception;
    }

    public void setException(final Exception exception) {
        this.exception = exception;
        if (null != exceptionPropertyChanged) {
            exceptionPropertyChanged.changed(exception);
        }
    }

    @Override
    public void setExceptionPropertyChanged(final ExceptionPropertyChanged exceptionPropertyChanged) {
        this.exceptionPropertyChanged = exceptionPropertyChanged;
    }
}