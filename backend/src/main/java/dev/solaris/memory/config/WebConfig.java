package dev.solaris.memory.config;

import dev.solaris.memory.service.MemoryService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Lets query params like {@code ?status=active&type=temporary} bind case-insensitively, as the API contract in the doc uses lowercase. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(new StringToStatusFilter());
        registry.addConverter(new StringToTypeFilter());
    }

    private static class StringToStatusFilter implements Converter<String, MemoryService.StatusFilter> {
        @Override
        public MemoryService.StatusFilter convert(@NonNull String source) {
            return MemoryService.StatusFilter.valueOf(source.trim().toUpperCase());
        }
    }

    private static class StringToTypeFilter implements Converter<String, MemoryService.TypeFilter> {
        @Override
        public MemoryService.TypeFilter convert(@NonNull String source) {
            return MemoryService.TypeFilter.valueOf(source.trim().toUpperCase());
        }
    }
}
