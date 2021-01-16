package org.wyyt.springcloud.gateway.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.springcloud.gateway.entity.ClientVo;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.service.AppServiceImpl;
import org.wyyt.tool.rpc.Result;

import java.util.List;

import static org.wyyt.springcloud.gateway.controller.AppController.PREFIX;


/**
 * The controller of App
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
public class AppController {
    public static final String PREFIX = "app";
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AppServiceImpl appServiceImpl;

    public AppController(final AppServiceImpl appServiceImpl) {
        this.appServiceImpl = appServiceImpl;
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
        model.addAttribute("app", this.appServiceImpl.getById(id));
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<App>> list(@RequestParam(value = "page") final Integer pageNum,
                                  @RequestParam(value = "limit") final Integer pageSize,
                                  @RequestParam(value = "clientId", required = false) final String clientId,
                                  @RequestParam(value = "name", required = false) final String name) {
        final IPage<App> page = this.appServiceImpl.page(pageNum, pageSize, clientId, name);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "clientId") final String clientId,
                         @RequestParam(value = "clientSecret") final String clientSecret,
                         @RequestParam(value = "name") final String name,
                         @RequestParam(value = "isAdmin") final Boolean isAdmin,
                         @RequestParam(value = "description") final String description) {
        this.appServiceImpl.add(clientId,
                clientSecret,
                this.passwordEncoder.encode(clientSecret),
                name,
                isAdmin,
                description);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "name") final String name,
                          @RequestParam(value = "isAdmin") final Boolean isAdmin,
                          @RequestParam(value = "description") final String description) throws Exception {
        this.appServiceImpl.edit(id, name, isAdmin, description);
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id") final Long id) throws Exception {
        this.appServiceImpl.del(id);
        return Result.ok();
    }

    @PostMapping("genName")
    @ResponseBody
    public Result<ClientVo> genName() {
        String clientId;
        do {
            clientId = RandomStringUtils.randomAlphanumeric(15);
        } while (null != this.appServiceImpl.getByClientId(clientId));

        final ClientVo clientVo = new ClientVo();
        clientVo.setClientId(clientId);
        clientVo.setClientSecret(RandomStringUtils.randomAlphanumeric(25));
        return Result.ok(clientVo);
    }
}