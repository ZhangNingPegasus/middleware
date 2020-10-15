package org.wyyt.db2es.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.db2es.admin.service.PropertyService;
import org.wyyt.db2es.core.entity.persistent.Property;
import org.wyyt.tool.web.Result;

import java.util.List;

/**
 * The controller for params's pages.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(ParamsController.PREFIX)
public class ParamsController {
    public static final String PREFIX = "params";

    private final PropertyService propertyService;

    public ParamsController(final PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(name = "id") final Long id) {
        model.addAttribute("property", this.propertyService.getById(id));
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<Property>> list(@RequestParam(value = "name", required = false) final String name) {
        final QueryWrapper<Property> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            queryWrapper.lambda().like(Property::getName, name);
        }
        return Result.success(this.propertyService.list(queryWrapper));
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "value") final String value) throws Exception {
        if (this.propertyService.editValue(id, value)) {
            return Result.success();
        } else {
            return Result.error("");
        }
    }
}