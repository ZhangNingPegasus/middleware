package org.wyyt.admin.ui.advice;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.wyyt.admin.ui.config.AdminUiProperties;

@ControllerAdvice
public class ModelAdvice {
    private final AdminUiProperties adminUiProperties;

    public ModelAdvice(AdminUiProperties adminUiProperties) {
        this.adminUiProperties = adminUiProperties;
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("title", this.adminUiProperties.getTitle());
        model.addAttribute("fullName", this.adminUiProperties.getFullName());
        model.addAttribute("shortName", this.adminUiProperties.getShortName());
    }
}
