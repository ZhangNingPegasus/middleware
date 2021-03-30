package org.wyyt.admin.ui.spi;

import org.wyyt.admin.ui.entity.vo.AdminVo;
import org.wyyt.ldap.entity.UserInfo;

import java.util.List;

/**
 * The interface of user service
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public interface UserService {
    boolean authenticate(final String username,
                         final String password) throws Exception;

    AdminVo getByUserName(final String username) throws Exception;

    List<UserInfo> search(final String keyword,
                          final int offset,
                          final int limit) throws Exception;
}
