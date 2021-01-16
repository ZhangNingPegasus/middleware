package org.wyyt.admin.ui.advice;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.wyyt.admin.ui.config.AdminUiProperties;

/**
 * Providing global attributes into Model
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@ControllerAdvice
public class ModelAdvice {
    private final AdminUiProperties adminUiProperties;

    public ModelAdvice(final AdminUiProperties adminUiProperties) {
        this.adminUiProperties = adminUiProperties;
    }

    @ModelAttribute
    public void addAttributes(final Model model) {
        model.addAttribute("title", this.adminUiProperties.getTitle());
        model.addAttribute("fullName", this.adminUiProperties.getFullName());
        model.addAttribute("shortName", this.adminUiProperties.getShortName());
    }
}
