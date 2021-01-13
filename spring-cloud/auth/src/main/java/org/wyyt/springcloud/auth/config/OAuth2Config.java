package org.wyyt.springcloud.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@EnableAuthorizationServer
public class OAuth2Config extends AuthorizationServerConfigurerAdapter {
    private final PasswordEncoder passwordEncoder;
    public final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final TokenStore tokenStore;
    private final DataSource dataSource;
    private final JwtAccessTokenConverter jwtAccessTokenConverter;

    public OAuth2Config(final PasswordEncoder passwordEncoder,
                        final UserDetailsService userDetailsService,
                        final AuthenticationManager authenticationManager,
                        final TokenStore tokenStore,
                        final DataSource dataSource,
                        final JwtAccessTokenConverter jwtAccessTokenConverter) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.tokenStore = tokenStore;
        this.dataSource = dataSource;
        this.jwtAccessTokenConverter = jwtAccessTokenConverter;
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {
        return new JWTokenEnhancer();
    }

    @Override
    public void configure(final AuthorizationServerEndpointsConfigurer endpoints) {
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        enhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), jwtAccessTokenConverter));
        endpoints.tokenStore(tokenStore) //指定 token 的存储方式
                .authenticationManager(authenticationManager)  //支持 password 模式
                .userDetailsService(userDetailsService) //刷新token的请求会用用到
                .tokenEnhancer(enhancerChain)
                // refresh_token有两种使用方式：重复使用(true)、非重复使用(false)，默认为true
                // 1.重复使用：access_token过期刷新时， refresh token过期时间未改变，仍以初次生成的时间为准
                // 2.非重复使用：access_token过期刷新时， refresh_token过期时间延续，在refresh_token有效期内刷新而无需失效再次登录
                .reuseRefreshTokens(true);
    }

    @Override
    public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource).passwordEncoder(passwordEncoder); //存入到数据库中
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.allowFormAuthenticationForClients();  //允许客户端访问 OAuth2 授权接口，否则请求 token 会返回 401
        security.checkTokenAccess("permitAll()"); //允许客户端访问 OAuth2 授权接口，否则请求 token 会返回 401
        security.tokenKeyAccess("isAuthenticated()"); //允许已授权用户访问 checkToken 接口和获取 token 接口
    }
}
