package org.wyyt.admin.ui.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.admin.ui.entity.base.BaseDto;

/**
 * The entity for table sys_page. Using for managing monitor's pages.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`sys_page`")
public final class SysPage extends BaseDto {
    private static final long serialVersionUID = 1L;

    /**
     * 页面名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 页面地址
     */
    @TableField(value = "`url`")
    private String url;

    /**
     * 页面是否出现在菜单栏
     */
    @TableField(value = "`is_menu`")
    private Boolean isMenu;

    /**
     * 是否是默认页(只允许有一个默认页，如果设置多个，以第一个为准)
     */
    @TableField(value = "`is_default`")
    private Boolean isDefault;

    /**
     * 是否新开窗口打开页面
     */
    @TableField(value = "`is_blank`")
    private Boolean isBlank;

    /**
     * html中的图标样式
     */
    @TableField(value = "`icon_class`")
    private String iconClass;

    /**
     * 父级id(即本表的主键id)
     */
    @TableField(value = "`parent_id`")
    private Long parentId;

    /**
     * 顺序号(值越小, 排名越靠前)
     */
    @TableField(value = "`order_num`")
    private Long orderNum;

    /**
     * 备注
     */
    @TableField(value = "`remark`")
    private String remark;
}