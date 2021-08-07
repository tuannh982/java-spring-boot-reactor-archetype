package io.github.tuannh982.spring.boot.reactor.arch.inject.annotation;

import io.github.tuannh982.spring.boot.reactor.arch.inject.AllModules;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AllModules.class)
@Documented
public @interface ImportAllModules {
}
