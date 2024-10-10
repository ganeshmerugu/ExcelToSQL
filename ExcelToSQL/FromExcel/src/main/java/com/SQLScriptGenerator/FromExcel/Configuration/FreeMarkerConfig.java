package com.SQLScriptGenerator.FromExcel.Configuration;


import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;


import java.io.IOException;

@org.springframework.context.annotation.Configuration
public class FreeMarkerConfig {

    @Bean
    public Configuration freeMarkerConfiguration() throws IOException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setClassForTemplateLoading(this.getClass(), "/templates/");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
        return configuration;
    }
}
