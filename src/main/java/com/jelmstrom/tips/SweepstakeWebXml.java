package com.jelmstrom.tips;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;


@SuppressWarnings("UnusedDeclaration")
public class SweepstakeWebXml extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        ConfigurationLoader.initialiseData();
        return application.sources(Application.class);
    }





}