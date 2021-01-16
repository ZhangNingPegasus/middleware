package org.wyyt.admin.ui.template;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.util.Map;

/**
 * check if user has not update permission
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public class NoUpdateDirective extends AuthDirective implements TemplateDirectiveModel {
    @Override
    public void execute(final Environment env,
                        final Map params,
                        final TemplateModel[] loopVars,
                        final TemplateDirectiveBody body)
            throws TemplateException, IOException {
        if (null != body && !checkPermission(Operation.UPDATE)) {
            body.render(env.getOut());
        }
    }

}