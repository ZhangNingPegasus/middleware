package org.wyyt.springcloud.gateway.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.springcloud.gateway.entity.ClientVo;
import org.wyyt.springcloud.gateway.entity.entity.IgnoreUrl;
import org.wyyt.springcloud.gateway.service.IgnoreUrlServiceImpl;
import org.wyyt.tool.rpc.Result;

import java.util.List;

import static org.wyyt.springcloud.gateway.controller.IgnoreUrlController.PREFIX;


/**
 * The controller of IgnoreUrl
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021        Initialize  *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class IgnoreUrlController {
    public static final String PREFIX = "ignoreurl";
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final IgnoreUrlServiceImpl ignoreUrlServiceImpl;

    public IgnoreUrlController(final IgnoreUrlServiceImpl ignoreUrlServiceImpl) {
        this.ignoreUrlServiceImpl = ignoreUrlServiceImpl;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd() {
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(value = "id") final Long id) {
        model.addAttribute("url", this.ignoreUrlServiceImpl.getById(id));
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<IgnoreUrl>> list(@RequestParam(value = "page") final Integer pageNum,
                                        @RequestParam(value = "limit") final Integer pageSize,
                                        @RequestParam(value = "clientId", required = false) final String clientId,
                                        @RequestParam(value = "url", required = false) final String url) {
        final IPage<IgnoreUrl> page = this.ignoreUrlServiceImpl.page(pageNum, pageSize, url);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "url") final String url,
                         @RequestParam(value = "description") final String description) throws Exception {
        this.ignoreUrlServiceImpl.add(url, description);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "url") final String url,
                          @RequestParam(value = "description") final String description) throws Exception {
        this.ignoreUrlServiceImpl.edit(id, url, description);

        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id") final Long id) throws Exception {
        this.ignoreUrlServiceImpl.del(id);
        return Result.ok();
    }

    @PostMapping("genName")
    @ResponseBody
    public Result<ClientVo> genName() {
        final ClientVo clientVo = new ClientVo();
        clientVo.setClientId(RandomStringUtils.randomAlphanumeric(25));
        clientVo.setClientSecret(RandomStringUtils.randomAlphanumeric(35));
        return Result.ok(clientVo);
    }
}