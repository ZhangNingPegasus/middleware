package org.wyyt.db2es.admin.rebuild;

/**
 * The interface of exception callback
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public interface ExceptionCallback {
    Exception getException();

    void setException(final Exception exception);

    void setExceptionPropertyChanged(final ExceptionPropertyChanged exceptionPropertyChanged);

    interface ExceptionPropertyChanged {
        void changed(final Exception exception);
    }
}