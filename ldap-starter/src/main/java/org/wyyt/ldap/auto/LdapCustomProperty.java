package org.wyyt.ldap.auto;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The custom property of ldap
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
@ConfigurationProperties(prefix = "spring.ldap.mapping")
public class LdapCustomProperty {
    private String objectClassAttrName;
    private String loginIdAttrName;
    private String nameAttrName;
    private String mailAttrName;
    private String phoneNumberAttrName;
    private String titleAttrName;
    @Value("#{'${filter.memberOf:}'.split('\\|')}")
    private String[] memberOf;
}