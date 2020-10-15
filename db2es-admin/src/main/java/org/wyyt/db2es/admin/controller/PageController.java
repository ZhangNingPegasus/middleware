package org.wyyt.db2es.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.db2es.admin.entity.dto.SysPage;
import org.wyyt.db2es.admin.entity.vo.PageVo;
import org.wyyt.db2es.admin.service.SysPageService;
import org.wyyt.tool.web.Result;

import java.util.ArrayList;
import java.util.List;


/**
 * The controller for monitor's pages.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
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
    public String toAdd(final Model model) {
        model.addAttribute("pages", this.sysPageService.list(new QueryWrapper<SysPage>().lambda().eq(SysPage::getUrl, "").orderByAsc(SysPage::getName)));
        return "page/add";
    }

    @RequestMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(value = "id") final Long id) {
        model.addAttribute("page", this.sysPageService.getById(id));
        model.addAttribute("pages", this.sysPageService.list(new QueryWrapper<SysPage>().lambda().eq(SysPage::getUrl, "").orderByAsc(SysPage::getName)));
        return "page/edit";
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<PageVo>> list(@RequestParam(value = "page") final Integer pageNum,
                                     @RequestParam(value = "limit") final Integer pageSize,
                                     @RequestParam(value = "name", required = false) final String name) {
        return Result.success(this.sysPageService.list(pageNum, pageSize, name).getRecords());
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(final SysPage sysPage) {
        if (null == sysPage.getIsDefault()) {
            sysPage.setIsDefault(false);
        }
        if (null == sysPage.getIsMenu()) {
            sysPage.setIsMenu(false);
        }
        final Long orderNum = this.sysPageService.getMaxOrderNum(sysPage.getParentId());
        sysPage.setOrderNum(orderNum + 1);
        this.sysPageService.save(sysPage);
        return Result.success();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(final SysPage sysPage) {
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
        return Result.success();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "ids") final String ids) {
        final String[] idsArray = ids.split(",");
        final List<Long> idsList = new ArrayList<>(idsArray.length);
        for (final String id : idsArray) {
            if (null != id && !StringUtils.isEmpty(id.trim())) {
                idsList.add(Long.parseLong(id));
            }
        }
        this.sysPageService.removeByIds(idsList);
        return Result.success();
    }
}