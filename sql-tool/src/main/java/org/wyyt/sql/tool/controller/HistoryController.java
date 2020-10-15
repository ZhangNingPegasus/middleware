package org.wyyt.sql.tool.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.sql.tool.database.Db;
import org.wyyt.sql.tool.entity.dto.SysSql;
import org.wyyt.tool.web.Result;

import java.util.List;

import static org.wyyt.sql.tool.controller.HistoryController.PREFIX;


/**
 * The controller for SQL statement executing history
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
public final class HistoryController {
    public static final String PREFIX = "his";

    private final Db db;

    public HistoryController(final Db db) {
        this.db = db;
    }

    @GetMapping("tolist")
    public final String toList(final Model model) throws Exception {
        model.addAttribute("admins", this.db.listAdmin());
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
        return Result.success(result.getRecords(), result.getTotal());
    }
}