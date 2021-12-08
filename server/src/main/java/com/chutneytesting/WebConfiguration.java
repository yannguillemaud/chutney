package com.chutneytesting;

import com.chutneytesting.tools.ui.MyMixInForIgnoreType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.nio.file.Paths;
import javax.sql.DataSource;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

@Configuration
public class WebConfiguration {

    @Bean
    @Primary // Because of https://github.com/spring-projects/spring-boot/issues/20308
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .addMixIn(Resource.class, MyMixInForIgnoreType.class)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .findAndRegisterModules();
    }

    @Bean
    public ObjectMapper reportObjectMapper() {
        return new ObjectMapper()
            .addMixIn(Resource.class, MyMixInForIgnoreType.class)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.mappedFeature())
            .findAndRegisterModules();
    }

    @Bean
    ObjectMapper persistenceObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .enable(SerializationFeature.INDENT_OUTPUT);

        return objectMapper.setVisibility(
            objectMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
        );
    }

    @Bean
    JdbcTemplate uiJdbcTemplate(DataSource internalDataSource) {
        return new JdbcTemplate(internalDataSource);
    }

    @Bean
    NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate(JdbcTemplate uiJdbcTemplate) {
        return new NamedParameterJdbcTemplate(uiJdbcTemplate);
    }

    @Bean
    WebServerFactoryCustomizer<AbstractServletWebServerFactory> embeddedServletContainerCustomizer() {
        // We need to explicitly change the assets path
        // because even when in DEV asset are always generated by webpack in target/www
        return this::setLocationForStaticAssets;
    }

    private void setLocationForStaticAssets(AbstractServletWebServerFactory container) {
        File root = new File(resolvePathPrefix() + "ui/dist/chutney/"); // TODO use Path instead ?
        if (root.exists() && root.isDirectory()) {
            container.setDocumentRoot(root);
        }
    }

    /**
     * Resolve path prefix to static resources.
     */
    private String resolvePathPrefix() {
        String fullExecutablePath = this.getClass().getResource("").getPath();
        String rootPath = Paths.get(".").toUri().normalize().getPath();
        String extractedPath = fullExecutablePath.replace(rootPath, "");
        int extractionEndIndex = extractedPath.indexOf("target/");
        if (extractionEndIndex < 0) {
            return "";
        } else if (extractionEndIndex == 0) {
            return "../";
        }
        return rootPath;
    }
}
