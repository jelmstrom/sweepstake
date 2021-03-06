package com.jelmstrom.tips;

import com.jelmstrom.tips.configuration.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import java.text.ParseException;
import java.util.EnumSet;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("=============> Seeding");
        try {
            Config.seed();
        } catch (ParseException e) {
            System.out.printf("Failed to seed data : %s" , e.getMessage());
            e.printStackTrace();
        }
    }


    @SuppressWarnings("UnusedDeclaration")
    @Bean
    public EmbeddedServletContainerFactory containerFactory(){
        return new TomcatEmbeddedServletContainerFactory(8085);
    }

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return (ServletContext servletContext) -> {
            final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
            characterEncodingFilter.setEncoding("UTF-8");
            characterEncodingFilter.setForceEncoding(false);

        servletContext.addFilter("characterEncodingFilter", characterEncodingFilter)
                    .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
        };
    }
}
