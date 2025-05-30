package personal.carl.thronson.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import personal.carl.thronson.ai.svc.DjlEmbeddingModel;

@Configuration
public class EmbeddingModelConfig {
    @Bean
    public DjlEmbeddingModel embeddingModel() throws Exception {
        return new DjlEmbeddingModel();
    }
}
