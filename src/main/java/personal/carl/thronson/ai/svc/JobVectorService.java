package personal.carl.thronson.ai.svc;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;

@Service
public class JobVectorService {

  @Autowired
  private VectorStore vectorStore;

  public void addJobDescription(JobSearchJobListingEntity entity) throws Exception {
    String description = entity.getName();
    Map<String, Object> metaData = entity.getMetaData();
    metaData.entrySet().removeIf(entry -> entry.getValue() == null);
    metaData.put("fieldName", "title");
//    System.out.println("Store vector: " + metaData);
    Document doc = new Document(description, metaData);
    vectorStore.add(List.of(doc));
  }

  public List<Document> findSimilarJobs(String query, int topK) {
    SearchRequest request = SearchRequest.builder()
      .query(query)
      .topK(topK)
      .build();
    return vectorStore.similaritySearch(request);
  }
}
