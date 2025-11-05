package com.itheima.springbootkeycloak.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class OAuth2Config {

    public OAuth2Config() {
        System.out.println("=== OAuth2Config 被加载 ===");
    }

    // 使用具体的 Converter 类而不是 lambda
    @Bean
    Converter<Map<String, Object>, Collection<GrantedAuthority>> realmRolesAuthoritiesConverter() {
        return new ClaimsToAuthoritiesConverter();
    }

    @Bean
    GrantedAuthoritiesMapper userAuthoritiesMapper(Converter<Map<String, Object>, Collection<GrantedAuthority>> authoritiesConverter) {
        return new KeycloakAuthoritiesMapper(authoritiesConverter);
    }

    // 具体的 Converter 实现类
    private static class ClaimsToAuthoritiesConverter implements Converter<Map<String, Object>, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Map<String, Object> claims) {
            System.out.println("=== Keycloak Claims for Authority Conversion ===");

            // 从 realm_access.roles 提取角色
            var realmAccess = Optional.ofNullable((Map<String, Object>) claims.get("realm_access"));
            var roles = realmAccess.flatMap(map -> Optional.ofNullable((List<String>) map.get("roles")));

            Collection<GrantedAuthority> authorities = roles.map(List::stream)
                    .orElse(Stream.empty())
                    .map(role -> {
                        System.out.println("Converting role: " + role);
                        return new SimpleGrantedAuthority(role);
                    })
                    .collect(Collectors.toList());

            System.out.println("=== Extracted Authorities ===");
            authorities.forEach(auth -> System.out.println("Authority: " + auth.getAuthority()));

            return authorities;
        }
    }

    // 具体的 GrantedAuthoritiesMapper 实现类
    private static class KeycloakAuthoritiesMapper implements GrantedAuthoritiesMapper {
        private final Converter<Map<String, Object>, Collection<GrantedAuthority>> authoritiesConverter;

        public KeycloakAuthoritiesMapper(Converter<Map<String, Object>, Collection<GrantedAuthority>> authoritiesConverter) {
            this.authoritiesConverter = authoritiesConverter;
        }

        @Override
        public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
            System.out.println("=== 开始权限映射 ===");

            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            for (GrantedAuthority authority : authorities) {
                if (authority instanceof OidcUserAuthority) {
                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
                    System.out.println("=== Processing OidcUserAuthority ===");

                    Map<String, Object> claims = oidcUserAuthority.getIdToken().getClaims();
                    Collection<GrantedAuthority> keycloakAuthorities = authoritiesConverter.convert(claims);

                    mappedAuthorities.addAll(keycloakAuthorities);

                    // 保留原有的scope authorities
                    mappedAuthorities.add(new SimpleGrantedAuthority("OIDC_USER"));
                } else {
                    // 保留其他类型的authorities
                    mappedAuthorities.add(authority);
                }
            }

            System.out.println("=== Final Mapped Authorities ===");
            mappedAuthorities.forEach(auth -> System.out.println("Final Authority: " + auth.getAuthority()));

            return mappedAuthorities;
        }
    }
}