package org.wyyt.admin.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.admin.ui.entity.dto.SysPage;
import org.wyyt.admin.ui.entity.vo.PageVo;
import org.wyyt.admin.ui.service.SysPageService;
import org.wyyt.tool.rpc.Result;

import java.util.ArrayList;
import java.util.List;


/**
 * The controller for monitor's pages.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PageController.PREFIX)
public class PageController {
    public static final String PREFIX = "page";

    private final SysPageService sysPageService;

    public PageController(final SysPageService sysPageService) {
        this.sysPageService = sysPageService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @RequestMapping("toadd")
    public String toAdd(final Model model) throws Exception {
        model.addAttribute("pages", this.sysPageService.getEmptyUrl());
        return "page/add";
    }

    @RequestMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(value = "id") final Long id) throws Exception {
        model.addAttribute("page", this.sysPageService.getById(id));
        model.addAttribute("pages", this.sysPageService.getEmptyUrl());
        return "page/edit";
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<PageVo>> list(@RequestParam(value = "page") final Integer pageNum,
                                     @RequestParam(value = "limit") final Integer pageSize,
                                     @RequestParam(value = "name", required = false) final String name) throws Exception {
        return Result.ok(this.sysPageService.list(name, pageNum, pageSize).getRecords());
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(final SysPage sysPage) throws Exception {
        if (null == sysPage.getIsDefault()) {
            sysPage.setIsDefault(false);
        }
        if (null == sysPage.getIsMenu()) {
            sysPage.setIsMenu(false);
        }
        if (null == sysPage.getIsBlank()) {
            sysPage.setIsBlank(false);
        }
        final Long orderNum = this.sysPageService.getMaxOrderNum(sysPage.getParentId());
        sysPage.setOrderNum(orderNum + 1);
        this.sysPageService.insert(sysPage);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(final SysPage sysPage) throws Exception {
        final SysPage dbSysPage = this.sysPageService.getById(sysPage.getId());
        if (null != dbSysPage) {
            if (null == sysPage.getIsDefault()) {
                sysPage.setIsDefault(false);
            }
            if (null == sysPage.getIsMenu()) {
                sysPage.setIsMenu(false);
            }
            if (null == sysPage.getIsBlank()) {
                sysPage.setIsBlank(false);
            }
            this.sysPageService.updateById(sysPage);
        }
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "ids") final String ids) throws Exception {
        final String[] idsArray = ids.split(",");
        final List<Long> idsList = new ArrayList<>(idsArray.length);
        for (final String id : idsArray) {
            if (null != id && !ObjectUtils.isEmpty(id.trim())) {
                idsList.add(Long.parseLong(id));
            }
        }
        this.sysPageService.removeByIds(idsList);
        return Result.ok();
    }
}