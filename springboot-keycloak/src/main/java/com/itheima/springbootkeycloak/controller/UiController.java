package com.itheima.springbootkeycloak.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

@Controller
public class UiController {

    @GetMapping("/")
    public String getIndex(Model model, Authentication auth) {
        System.out.println("=== 访问首页 ===");

        // 调试信息
        if (auth != null) {
            System.out.println("=== Authentication Info ===");
            System.out.println("Name: " + auth.getName());
            System.out.println("Authenticated: " + auth.isAuthenticated());
            System.out.println("Authorities: " + auth.getAuthorities());

            if (auth instanceof OAuth2AuthenticationToken oauth && oauth.getPrincipal() instanceof OidcUser oidc) {
                System.out.println("Preferred Username: " + oidc.getPreferredUsername());

                model.addAttribute("name", oidc.getPreferredUsername());
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("isNice", auth.getAuthorities().stream()
                        .anyMatch(authority -> Objects.equals("NICE", authority.getAuthority())));

                return "index.html";
            }
        }

        // 未认证用户
        model.addAttribute("name", "");
        model.addAttribute("isAuthenticated", false);
        model.addAttribute("isNice", false);

        return "index.html";
    }

    @GetMapping("/nice")
    public String getNice(Model model, Authentication auth) {
        System.out.println("=== 访问 /nice 页面 ===");
        if (auth != null && auth instanceof OAuth2AuthenticationToken oauth && oauth.getPrincipal() instanceof OidcUser oidc) {
            model.addAttribute("name", oidc.getPreferredUsername());
        }
        return "nice.html";
    }

    // 调试端点
    @GetMapping("/debug")
    @ResponseBody
    public String debug(Authentication auth) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Debug Authentication ===\n");

        if (auth != null) {
            sb.append("Name: ").append(auth.getName()).append("\n");
            sb.append("Authenticated: ").append(auth.isAuthenticated()).append("\n");
            sb.append("Authorities: ").append(auth.getAuthorities()).append("\n");

            if (auth instanceof OAuth2AuthenticationToken oauth && oauth.getPrincipal() instanceof OidcUser oidc) {
                sb.append("Preferred Username: ").append(oidc.getPreferredUsername()).append("\n");
                sb.append("Email: ").append(oidc.getEmail()).append("\n");
                sb.append("Claims:\n");
                oidc.getClaims().forEach((key, value) ->
                        sb.append("  ").append(key).append(": ").append(value).append("\n")
                );
            }
        } else {
            sb.append("No authentication - User is anonymous\n");
        }

        return sb.toString();
    }
}