package org.delcom._1s25021springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.delcom._1s25021springbootcashflow.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

class AppConfigTest {
    private static ResourceHandlerRegistry newRegistry() {
        MockServletContext servletContext = new MockServletContext();
        return new ResourceHandlerRegistry(new GenericWebApplicationContext(servletContext), servletContext);
    }

    @Test
    void handlerUploadUntukFolderYangSudahAda(@TempDir Path dir) {
        ResourceHandlerRegistry registry = newRegistry();
        new AppConfig(dir.toString()).addResourceHandlers(registry);
        assertTrue(registry.hasMappingForPattern("/uploads/**"));
    }

    @Test
    void handlerUploadUntukFolderYangBelumAdaDitambahkanGarisMiring(@TempDir Path dir) {
        ResourceHandlerRegistry registry = newRegistry();
        new AppConfig(dir.resolve("belum-dibuat").toString()).addResourceHandlers(registry);
        assertTrue(registry.hasMappingForPattern("/uploads/**"));
    }
}
