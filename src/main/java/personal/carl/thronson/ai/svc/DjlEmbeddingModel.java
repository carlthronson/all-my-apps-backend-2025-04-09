package personal.carl.thronson.ai.svc;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.Embedding;

import java.util.ArrayList;
import java.util.List;

public class DjlEmbeddingModel implements EmbeddingModel {

    private final Predictor<String, float[]> predictor;

    public DjlEmbeddingModel() throws Exception {
        Criteria<String, float[]> criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
                .build();
        this.predictor = ModelZoo.loadModel(criteria).newPredictor();
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> texts = request.getInstructions(); // <-- FIXED
        List<Embedding> embeddings = new ArrayList<>();
        for (String text : texts) {
            try {
                float[] vector = predictor.predict(text);
                embeddings.add(new Embedding(vector, null)); // <-- FIXED
            } catch (Exception e) {
                throw new RuntimeException("Embedding failed", e);
            }
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        try {
            return predictor.predict(document.getText()); // <-- FIXED
        } catch (Exception e) {
            throw new RuntimeException("Embedding failed", e);
        }
    }
}
