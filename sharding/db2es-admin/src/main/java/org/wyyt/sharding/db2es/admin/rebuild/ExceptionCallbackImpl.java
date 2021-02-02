package org.wyyt.sharding.db2es.admin.rebuild;

/**
 * The implemention of exception callback
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
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