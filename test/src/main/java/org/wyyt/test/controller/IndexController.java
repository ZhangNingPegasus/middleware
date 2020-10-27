package org.wyyt.test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.test.service.FinAcOutFundChgService;

@RestController
public class IndexController {
    @Autowired
    private FinAcOutFundChgService finAcOutFundChgService;

    @GetMapping("save")
    public String save() {
        this.finAcOutFundChgService.save();
        return "OK";
    }
}