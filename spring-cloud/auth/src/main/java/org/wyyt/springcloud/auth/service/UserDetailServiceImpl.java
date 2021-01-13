package org.wyyt.springcloud.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    public UserDetailServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        log.info("usernameis:" + username);
        // 查询数据库操作
        if (StringUtils.isEmpty(username)) {
            throw new UsernameNotFoundException("the user is not found");
        }

        // 用户角色也应在数据库中获取
        String role = "ROLE_ADMIN";
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        String password = passwordEncoder.encode("123456");
        return new org.springframework.security.core.userdetails.User(username, password, authorities);
    }
}
