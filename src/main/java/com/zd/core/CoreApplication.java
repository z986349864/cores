package com.zd.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class CoreApplication {

    public static void main(String[] args) {
//        SpringApplication.run(CoreApplication.class, args);
        SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder();
        springApplicationBuilder.application().setAllowBeanDefinitionOverriding(true);
        springApplicationBuilder.sources(CoreApplication.class).run(args);
    }

}
