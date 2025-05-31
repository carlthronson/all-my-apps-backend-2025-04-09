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

  public void addJobDescription(JobSearchJobListingEntity entity) {
    String description = entity.getDescription().getDescription();
    Map<String, Object> metaData = entity.getMetaData();
    System.out.println("Store vector: " + metaData);
    System.out.println("Desc length: " + description.length());
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
