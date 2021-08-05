package io.github.tuannh982.spring.boot.arch.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
public class DefaultLocaleContextResolver extends AcceptHeaderLocaleContextResolver {
    public DefaultLocaleContextResolver(String[] locales) {
        List<Locale> lst = new ArrayList<>();
        for (String locale : locales) {
            lst.add(new Locale(locale));
        }
        if (lst.size() > 0) {
            this.setDefaultLocale(lst.get(0));
        }
        this.setSupportedLocales(lst);
    }
}
