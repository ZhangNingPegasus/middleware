package org.wyyt.sharding.sqltool.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.sharding.sqltool.database.Db;

/**
 * The job of deleting expired data
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Component
public class DeleteSchedule {

    private final Db db;

    public DeleteSchedule(final Db db) {
        this.db = db;
    }

    //每天00:01:00执行一次
    @Scheduled(cron = "0 1 0 1/1 * ?")
    public void deleteExpired() throws Exception {
        this.db.deleteExpired();
    }
}