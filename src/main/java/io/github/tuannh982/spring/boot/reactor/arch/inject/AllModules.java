package io.github.tuannh982.spring.boot.reactor.arch.inject;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"io.github.tuannh982.spring.boot.reactor.arch"})
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class
})
public class AllModules {
}
