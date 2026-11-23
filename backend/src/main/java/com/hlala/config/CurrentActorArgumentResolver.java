package com.hlala.config;

import com.hlala.domain.Enums;
import com.hlala.service.Actor;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/** Turns the security principal into an Actor, so controllers and services never touch SecurityContextHolder. */
public class CurrentActorArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentActor.class) && parameter.getParameterType() == Actor.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UUID userId)) {
            throw new AuthenticationCredentialsNotFoundException("Not signed in");
        }
        Set<Enums.Role> roles = EnumSet.noneOf(Enums.Role.class);
        for (GrantedAuthority a : auth.getAuthorities()) {
            String name = a.getAuthority();
            if (name != null && name.startsWith("ROLE_")) {
                try {
                    roles.add(Enums.Role.valueOf(name.substring(5)));
                } catch (IllegalArgumentException ignored) {
                    // not one of ours
                }
            }
        }
        return new Actor(userId, roles);
    }
}
