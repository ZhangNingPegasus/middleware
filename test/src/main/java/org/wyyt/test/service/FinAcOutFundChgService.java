package org.wyyt.test.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shardingsphere.core.strategy.keygen.SnowflakeShardingKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wyyt.kafka.service.KafkaService;
import org.wyyt.kafka.tran.anno.TranKafka;
import org.wyyt.sharding.anno.TranSave;
import org.wyyt.test.entity.FinAcOutFundChg;
import org.wyyt.test.mapper.FinAcOutFundChgMapper;

/**
 * service of FinAcOutFundChg
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Service
public class FinAcOutFundChgService extends ServiceImpl<FinAcOutFundChgMapper, FinAcOutFundChg> {
    private static final SnowflakeShardingKeyGenerator snowflakeShardingKeyGenerator = new SnowflakeShardingKeyGenerator();

    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private FinAcOutFundChgService finAcOutFundChgService;

    @TranSave
    @TranKafka
    public void save() throws Exception {
        FinAcOutFundChg finAcOutFundChg1 = new FinAcOutFundChg();
        finAcOutFundChg1.setId(Long.parseLong(snowflakeShardingKeyGenerator.generateKey().toString()));
        finAcOutFundChg1.setAccNo("1");

        FinAcOutFundChg finAcOutFundChg2 = new FinAcOutFundChg();
        finAcOutFundChg2.setId(Long.parseLong(snowflakeShardingKeyGenerator.generateKey().toString()));
        finAcOutFundChg2.setAccNo("2");

        this.finAcOutFundChgService.save(finAcOutFundChg1);
        this.finAcOutFundChgService.save(finAcOutFundChg2);

        this.kafkaService.send("A", "A", String.valueOf(System.currentTimeMillis()));
    }
}