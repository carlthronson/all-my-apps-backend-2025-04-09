package personal.carl.thronson.ai.svc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import personal.carl.thronson.core.BaseObject;

@Service
public class JobVectorService {

  @Autowired
  private VectorStore vectorStore;

  public Document storeVectorEmbedding(BaseObject entity, String text) throws Exception {
    Map<String, Object> metaData = new HashMap<>();
    metaData.put("id", entity.getId());
    Document doc = new Document(text, metaData);
    vectorStore.add(List.of(doc));
    return doc;
  }

  public List<Document> findSimilarDocuments(String query, int topK) {
    SearchRequest request = SearchRequest.builder()
      .query(query)
      .topK(topK)
      .build();
    return vectorStore.similaritySearch(request);
  }
}
