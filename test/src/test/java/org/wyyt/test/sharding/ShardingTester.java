package org.wyyt.test.sharding;

import io.seata.rm.datasource.sql.struct.TableMetaCache;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.wyyt.test.TestApplication;
import org.wyyt.test.service.FinAcOutFundChgService;

import java.util.ServiceLoader;

/**
 * the test of Sharding
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Rollback(false)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ShardingTester {
    @Autowired
    private FinAcOutFundChgService finAcOutFundChgService;

    @Test
    public void save() {
        this.finAcOutFundChgService.save();
    }
}