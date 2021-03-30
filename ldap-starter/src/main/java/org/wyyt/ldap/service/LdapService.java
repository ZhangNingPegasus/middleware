package org.wyyt.ldap.service;

import org.springframework.boot.autoconfigure.ldap.LdapProperties;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.SearchScope;
import org.springframework.util.ObjectUtils;
import org.wyyt.ldap.auto.LdapCustomProperty;
import org.wyyt.ldap.entity.UserInfo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * The service for ldap
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class LdapService {
    private static final String MEMBER_OF_ATTR_NAME = "memberOf";
    private static final String MEMBER_UID_ATTR_NAME = "memberUid";
    private final LdapTemplate ldapTemplate;
    private final LdapProperties ldapProperties;
    private final LdapCustomProperty ldapCustomProperty;

    public LdapService(final LdapTemplate ldapTemplate,
                       final LdapProperties ldapProperties,
                       final LdapCustomProperty ldapCustomProperty) {
        this.ldapTemplate = ldapTemplate;
        this.ldapProperties = ldapProperties;
        this.ldapCustomProperty = ldapCustomProperty;
    }

    /**
     * authenticate with login account and password
     *
     * @param username the login account
     * @param password the login password
     * @return true: login success; false: login failed
     */
    public boolean authenticate(final String username,
                                final String password) {
        final EqualsFilter filter = new EqualsFilter(this.ldapCustomProperty.getLoginIdAttrName(), username);
        this.ldapTemplate.setIgnorePartialResultException(true);
        return this.ldapTemplate.authenticate(this.ldapProperties.getBase(), filter.toString(), password);
    }

    /**
     * obtain user information according to login account
     *
     * @param username the login account
     * @return user information
     */
    public UserInfo getByUserName(final String username) {
        try {
            return this.ldapTemplate.searchForObject(
                    this.ldapProperties.getBase(),
                    query().where(this.ldapCustomProperty.getLoginIdAttrName()).is(username).filter().toString(),
                    ctx -> {
                        final DirContextAdapter contextAdapter = (DirContextAdapter) ctx;
                        final UserInfo userInfo = new UserInfo();
                        userInfo.setUsername(contextAdapter.getStringAttribute(this.ldapCustomProperty.getLoginIdAttrName()));
                        userInfo.setName(contextAdapter.getStringAttribute(this.ldapCustomProperty.getNameAttrName()));
                        userInfo.setPhoneNumber(contextAdapter.getStringAttribute(this.ldapCustomProperty.getPhoneNumberAttrName()));
                        userInfo.setEmail(contextAdapter.getStringAttribute(this.ldapCustomProperty.getMailAttrName()));
                        userInfo.setRemark(contextAdapter.getStringAttribute(this.ldapCustomProperty.getTitleAttrName()));
                        return userInfo;
                    });
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    /**
     * search user information according to keyword
     *
     * @param keyword keyword
     * @return return user information which match the keyword
     */
    public List<UserInfo> search(final String keyword,
                                 final int offset,
                                 final int limit) {
        final ContainerCriteria criteria = ldapQueryCriteria().and
                (
                        query().where(this.ldapCustomProperty.getLoginIdAttrName()).like("*" + keyword + "*")
                                .or(this.ldapCustomProperty.getNameAttrName()).like("*" + keyword + "*")
                );
        final List<UserInfo> result = ldapTemplate.search(this.ldapProperties.getBase(), criteria.filter().toString(), (AttributesMapper<UserInfo>) ctx -> {
            final UserInfo userInfo = new UserInfo();
            if (null != ctx.get(this.ldapCustomProperty.getLoginIdAttrName())) {
                userInfo.setUsername(ctx.get(this.ldapCustomProperty.getLoginIdAttrName()).get().toString());
            }
            if (null != ctx.get(this.ldapCustomProperty.getNameAttrName())) {
                userInfo.setName(ctx.get(this.ldapCustomProperty.getNameAttrName()).get().toString());
            }
            if (null != ctx.get(this.ldapCustomProperty.getPhoneNumberAttrName())) {
                userInfo.setPhoneNumber(ctx.get(this.ldapCustomProperty.getPhoneNumberAttrName()).get().toString());
            }
            if (null != ctx.get(this.ldapCustomProperty.getMailAttrName())) {
                userInfo.setEmail(ctx.get(this.ldapCustomProperty.getMailAttrName()).get().toString());
            }
            if (null != ctx.get(this.ldapCustomProperty.getTitleAttrName())) {
                userInfo.setRemark(ctx.get(this.ldapCustomProperty.getTitleAttrName()).get().toString());
            }
            return userInfo;
        });

        return result.stream().sorted(Comparator.comparing(UserInfo::getUsername)).skip(offset).limit(limit).collect(Collectors.toList());
    }

    private ContainerCriteria ldapQueryCriteria() {
        final ContainerCriteria criteria = query().searchScope(SearchScope.SUBTREE)
                .where("objectClass").is(this.ldapCustomProperty.getObjectClassAttrName());
        if (this.ldapCustomProperty.getMemberOf().length > 0 && !ObjectUtils.isEmpty(this.ldapCustomProperty.getMemberOf()[0])) {
            final ContainerCriteria memberOfFilters = query().where(MEMBER_OF_ATTR_NAME).is(this.ldapCustomProperty.getMemberOf()[0]);
            Arrays.stream(this.ldapCustomProperty.getMemberOf()).skip(1)
                    .forEach(filter -> memberOfFilters.or(MEMBER_OF_ATTR_NAME).is(filter));
            criteria.and(memberOfFilters);
        }
        return criteria;
    }
}