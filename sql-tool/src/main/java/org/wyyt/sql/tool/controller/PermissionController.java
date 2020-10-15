package org.wyyt.sql.tool.controller;


import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.sharding.service.ShardingService;
import org.wyyt.sql.tool.database.Db;
import org.wyyt.sql.tool.entity.dto.SysPermission;
import org.wyyt.sql.tool.entity.dto.SysRole;
import org.wyyt.tool.web.Result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.wyyt.sql.tool.controller.PermissionController.PREFIX;


/**
 * The controller for role's permission.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public final class PermissionController {
    public static final String PREFIX = "permission";

    private final Db db;
    private final ShardingService shardingService;

    public PermissionController(final Db db,
                                final ShardingService shardingService) {
        this.db = db;
        this.shardingService = shardingService;
    }

    @GetMapping("tolist")
    public final String toList(final Model model) throws Exception {
        model.addAttribute("roles", this.db.listRole().stream().filter(p -> !p.getSuperAdmin()).sorted(Comparator.comparing(SysRole::getName)).collect(Collectors.toList()));
        model.addAttribute("tables", this.shardingService.listTableProperties());
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public final String toAdd(final Model model) throws Exception {
        model.addAttribute("roles", this.db.listRole().stream().filter(p -> !p.getSuperAdmin()).sorted(Comparator.comparing(SysRole::getName)).collect(Collectors.toList()));
        model.addAttribute("tables", this.shardingService.listTableProperties());
        return String.format("%s/%s", PREFIX, "add");
    }

    @PostMapping("list")
    @ResponseBody
    public final Result<List<SysPermission>> list(@RequestParam(value = "page") final Integer pageNum,
                                                  @RequestParam(value = "limit") final Integer pageSize,
                                                  @RequestParam(value = "sysRoleId", required = false) final Long sysRoleId,
                                                  @RequestParam(value = "tableName", required = false) final String tableName) throws Exception {
        return Result.success(this.db.listPermission(pageNum, pageSize, sysRoleId, tableName));
    }

    @PostMapping("add")
    @ResponseBody
    public final Result<?> add(@RequestParam(value = "sysRoleId") final Long sysRoleId,
                               @RequestParam(value = "tableName") final String tableName,
                               @RequestParam(value = "insert", defaultValue = "false") final Boolean insert,
                               @RequestParam(value = "delete", defaultValue = "false") final Boolean delete,
                               @RequestParam(value = "update", defaultValue = "false") final Boolean update,
                               @RequestParam(value = "select", defaultValue = "false") final Boolean select) throws Exception {
        final SysPermission sysPermission = new SysPermission();
        sysPermission.setSysRoleId(sysRoleId);
        sysPermission.setTableName(tableName);
        sysPermission.setCanInsert(insert);
        sysPermission.setCanDelete(delete);
        sysPermission.setCanUpdate(update);
        sysPermission.setCanSelect(select);
        this.db.addPermission(sysPermission);
        return Result.success();
    }

    @PostMapping("edit")
    @ResponseBody
    public final Result<?> edit(@RequestParam(value = "id") final Long id,
                                @RequestParam(value = "type") final String type,
                                @RequestParam(value = "hasPermission") final Boolean hasPermission) throws Exception {
        final SysPermission sysPermission = db.getPermissionById(id);
        if (null != sysPermission) {
            switch (type.toLowerCase()) {
                case "insert":
                    sysPermission.setCanInsert(hasPermission);
                    break;
                case "delete":
                    sysPermission.setCanDelete(hasPermission);
                    break;
                case "update":
                    sysPermission.setCanUpdate(hasPermission);
                    break;
                case "select":
                    sysPermission.setCanSelect(hasPermission);
                    break;
                default:
                    throw new RuntimeException("不支持的操作类型");
            }
            this.db.updatePermissionById(sysPermission);
        }
        return Result.success();
    }

    @PostMapping("del")
    @ResponseBody
    public final Result<?> del(@RequestParam(value = "ids") final String ids) throws Exception {
        final String[] idsArray = ids.split(",");
        final List<Long> idsList = new ArrayList<>(idsArray.length);
        for (final String id : idsArray) {
            if (null != id && !StringUtils.isEmpty(id.trim())) {
                idsList.add(Long.parseLong(id.trim()));
            }
        }
        this.db.removePermissionByIds(idsList);
        return Result.success();
    }
}