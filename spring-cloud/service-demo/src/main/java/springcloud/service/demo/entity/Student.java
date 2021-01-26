package springcloud.service.demo.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "`student`")
public class Student implements Serializable {
    /**
     * 自增主键
     */
    @TableId(value = "`id`", type = IdType.AUTO)
    private Long id;

    /**
     * 姓名
     */
    @TableField(value = "`name`")
    private String name;
}