package org.wyyt.tool.bean;

import org.springframework.beans.BeanUtils;

/**
 * the common functions of Bean
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * ******************************************************************
 * Name               Action            Time          Description   *
 * Ning.Zhang       Initialize         10/1/2020        Initialize  *
 * ******************************************************************
 */
public final class BeanTool {
    public static <T> T copy(Object source, Class<T> target) {
        if (null == source) {
            return null;
        }

        try {
            T result = target.newInstance();
            BeanUtils.copyProperties(source, result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}