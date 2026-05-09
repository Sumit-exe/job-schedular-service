package com.airtribe.jobschedular.util;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class SecurityUtil {

    public static Mono<UUID> getCurrentUserId() {

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getPrincipal().toString())
                .map(UUID::fromString);
    }
}