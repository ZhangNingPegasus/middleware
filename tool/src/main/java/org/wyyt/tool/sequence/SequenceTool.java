package org.wyyt.tool.sequence;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import org.wyyt.tool.common.CommonTool;

/**
 * Generating a strictly incremental sequence number in milliseconds
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020        Initialize  *
 * *****************************************************************
 */
public final class SequenceTool {
    private static final int MAX_SEQUENCE = 99999;
    private static final long MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS = 1000;
    private static long lastMilliseconds;
    private static long sequence = 0;

    public synchronized static long getSequence() {
        long currentMilliseconds = System.currentTimeMillis();
        if (waitTolerateTimeDifferenceIfNeed(currentMilliseconds)) {
            currentMilliseconds = System.currentTimeMillis();
        }

        if (lastMilliseconds == currentMilliseconds) { //同毫秒内的序列号递增
            sequence++;
            if (sequence >= MAX_SEQUENCE) {
                currentMilliseconds = waitUntilNextTime(currentMilliseconds);
            }
        } else {
            sequence = 0;
        }
        lastMilliseconds = currentMilliseconds;
        return Long.parseLong(String.valueOf(currentMilliseconds).concat(StrUtil.padPre(String.valueOf(sequence), 5, "0")));
    }

    private static boolean waitTolerateTimeDifferenceIfNeed(long currentMilliseconds) {
        if (lastMilliseconds <= currentMilliseconds) {
            return false;
        } else {
            long timeDifferenceMilliseconds = lastMilliseconds - currentMilliseconds;
            Assert.isTrue(timeDifferenceMilliseconds < MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS, "Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds", lastMilliseconds, currentMilliseconds);
            CommonTool.sleep(timeDifferenceMilliseconds);
            return true;
        }
    }

    private static long waitUntilNextTime(long lastTime) {
        long result;
        for (result = System.currentTimeMillis(); result <= lastTime; result = System.currentTimeMillis()) {
        }
        return result;
    }
}
