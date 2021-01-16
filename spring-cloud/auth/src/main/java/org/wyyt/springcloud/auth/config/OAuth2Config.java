package org.wyyt.springcloud.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

/**
 * the configuration of Spring Security OAuth2.0
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2Config extends AuthorizationServerConfigurerAdapter {
    private final PasswordEncoder passwordEncoder;
    private final TokenStore tokenStore;
    private final DataSource dataSource;
    private final JwtAccessTokenConverter jwtAccessTokenConverter;

    public OAuth2Config(final PasswordEncoder passwordEncoder,
                        final TokenStore tokenStore,
                        final DataSource dataSource,
                        final JwtAccessTokenConverter jwtAccessTokenConverter) {
        this.passwordEncoder = passwordEncoder;
        this.tokenStore = tokenStore;
        this.dataSource = dataSource;
        this.jwtAccessTokenConverter = jwtAccessTokenConverter;
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {
        return new JWTTokenEnhancer();
    }

    @Override
    public void configure(final AuthorizationServerEndpointsConfigurer endpoints) {
        final TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        enhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), jwtAccessTokenConverter));
        endpoints.tokenStore(tokenStore)
                .tokenEnhancer(enhancerChain)
                .reuseRefreshTokens(true);
    }

    @Override
    public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource).passwordEncoder(passwordEncoder); //存入到数据库中
    }

    @Override
    public void configure(final AuthorizationServerSecurityConfigurer security) {
        security.allowFormAuthenticationForClients();  //允许客户端访问 OAuth2 授权接口，否则请求 token 会返回 401
        security.checkTokenAccess("permitAll()"); //允许客户端访问 OAuth2 授权接口，否则请求 token 会返回 401
        security.tokenKeyAccess("isAuthenticated()"); //允许已授权用户访问 checkToken 接口和获取 token 接口
    }
}
