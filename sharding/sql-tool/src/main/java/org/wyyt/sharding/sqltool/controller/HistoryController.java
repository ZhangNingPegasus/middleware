package org.wyyt.sharding.sqltool.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.admin.ui.service.SysAdminService;
import org.wyyt.sharding.sqltool.database.Db;
import org.wyyt.sharding.sqltool.entity.dto.SysSql;
import org.wyyt.tool.rpc.Result;

import java.util.List;

import static org.wyyt.sharding.sqltool.controller.HistoryController.PREFIX;


/**
 * The controller for SQL statement executing history
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public final class HistoryController {
    public static final String PREFIX = "his";
    private final SysAdminService sysAdminService;

    private final Db db;

    public HistoryController(final SysAdminService sysAdminService,
                             final Db db) {
        this.sysAdminService = sysAdminService;
        this.db = db;
    }

    @GetMapping("tolist")
    public final String toList(final Model model) throws Exception {
        model.addAttribute("admins", this.sysAdminService.list());
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("todetail")
    public final String todetail(final Model model,
                                 @RequestParam(value = "id") final Long id) throws Exception {
        model.addAttribute("sysSql", this.db.getSqlById(id));
        return String.format("%s/%s", PREFIX, "detail");
    }

    @PostMapping("list")
    @ResponseBody
    public final Result<List<SysSql>> list(@RequestParam(value = "page") final Integer pageNum,
                                           @RequestParam(value = "limit") final Integer pageSize,
                                           @RequestParam(name = "sysAdminId", required = false) final Long sysAdminId,
                                           @RequestParam(name = "ip", required = false) final String ip,
                                           @RequestParam(name = "fromExecutionTime", required = false) final Long fromExecutionTime,
                                           @RequestParam(name = "toExecutionTime", required = false) final Long toExecutionTime) throws Exception {
        final IPage<SysSql> result = this.db.listSql(pageNum, pageSize, sysAdminId, ip, fromExecutionTime, toExecutionTime);
        return Result.ok(result.getRecords(), result.getTotal());
    }
}