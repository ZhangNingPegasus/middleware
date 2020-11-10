package org.wyyt.kafka.monitor.template;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.util.Map;

/**
 * check if user has only select permission
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public class OnlySelectDirective extends AuthDirective implements TemplateDirectiveModel {

    @Override
    public void execute(Environment env,
                        Map params,
                        TemplateModel[] loopVars,
                        TemplateDirectiveBody body) throws TemplateException, IOException {
        if (body != null
                && checkPermission(Operation.SELECT)
                && !checkPermission(Operation.DELETE)
                && !checkPermission(Operation.UPDATE)) {
            body.render(env.getOut());
        }
    }
}