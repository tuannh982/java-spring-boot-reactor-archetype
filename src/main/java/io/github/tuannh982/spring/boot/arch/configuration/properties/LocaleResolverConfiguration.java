package io.github.tuannh982.spring.boot.arch.configuration.properties;

import io.github.tuannh982.spring.boot.arch.component.DefaultLocaleContextResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.i18n.LocaleContextResolver;

@Configuration
public class LocaleResolverConfiguration {
    private final AppConfigurationProperties appConfig;

    public LocaleResolverConfiguration(AppConfigurationProperties appConfig) {
        this.appConfig = appConfig;
    }

    @Primary
    @Bean("defaultLocaleContextResolver")
    @ConditionalOnMissingBean(LocaleContextResolver.class)
    public LocaleContextResolver localeContextResolver() {
        return new DefaultLocaleContextResolver(appConfig.getLocales());
    }
}
