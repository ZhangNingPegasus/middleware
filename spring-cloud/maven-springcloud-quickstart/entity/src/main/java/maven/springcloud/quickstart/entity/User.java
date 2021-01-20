package maven.springcloud.quickstart.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用于SpringCloud之间传递的实体类
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@ApiModel(description = "用户信息")
@Data
public class User {
    @ApiModelProperty("用户姓名")
    private String userName;

    @ApiModelProperty("用户年龄")
    private Integer age;

    @ApiModelProperty("用户性别. true:男性; false:女性")
    private Boolean male;

    @ApiModelProperty("描述信息")
    private String description;
}
