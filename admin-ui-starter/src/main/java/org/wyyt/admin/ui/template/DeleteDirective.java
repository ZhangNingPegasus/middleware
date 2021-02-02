package org.wyyt.admin.ui.template;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.util.Map;

/**
 * check if user has delete permission
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class DeleteDirective extends AuthDirective implements TemplateDirectiveModel {
    @Override
    public void execute(final Environment env,
                        final Map params,
                        final TemplateModel[] loopVars,
                        final TemplateDirectiveBody body) throws TemplateException, IOException {
        if (null != body && checkPermission(Operation.DELETE)) {
            body.render(env.getOut());
        }
    }
}