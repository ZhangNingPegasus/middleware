package org.wyyt.test.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * the entity of `fin_ac_out_fund_chg`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020       Initialize   *
 * *****************************************************************
 */
@Data
public final class FinAcOutFundChg {
    @TableId(value = "`id`", type = IdType.AUTO)
    private Long id;

    @TableField(value = "`row_create_time`")
    private Date rowCreateTime;

    @TableField(value = "`row_update_time`")
    private Date rowUpdateTime;

    @TableField(value = "`acc_no`")
    private String accNo;
}
