package personal.carl.thronson.ai.svc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobAnalysisEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobDescriptionEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchResponsibilityEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchSkillEntity;
import reactor.core.publisher.MonoSink;

public class JobAIAnalysisCallback implements Callback {
  private final MonoSink<JobSearchJobDescriptionEntity> sink;
  private final JobSearchJobDescriptionEntity jobDescription;

  public JobAIAnalysisCallback(MonoSink<JobSearchJobDescriptionEntity> sink, JobSearchJobDescriptionEntity jobDescription) {
      this.sink = sink;
      this.jobDescription = jobDescription;
  }

  @Override
  public void onFailure(Call call, IOException e) {
      sink.success(jobDescription); // fallback to original if error
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    try {
      System.out.println("Response: " + response);
      if (!response.isSuccessful()) {
        System.out.println("NOT SUCCESSFUL: " + response.toString());
          sink.success(jobDescription);
          return;
      }
//      System.out.println("Response code: " + response.code());
      String body = response.body().string();
      // Parse the AI's JSON response and update the job entity
      // For example, using Jackson:
//      System.out.println(body);
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(body);
      String aiContent = root.path("message").path("content").asText();
      // Parse aiContent (which should be JSON) and update job fields
      // (You may need to parse aiContent as JSON as well, depending on your prompt)
      // Example:
      System.out.println(aiContent);
      JsonNode aiJson = mapper.readTree(aiContent);
      JobSearchJobAnalysisEntity analysis = new JobSearchJobAnalysisEntity();
      analysis.setDescription(jobDescription);
      analysis.setExtractedTitle(getText(aiJson, "title"));
      analysis.setExtractedLocation(getText(aiJson, "location"));
      analysis.setExtractedSkills(getList(aiJson, "skills")
        .stream()
        .filter(item -> item.trim().length() > 0)
        .map(skill -> {
        JobSearchSkillEntity entity = new JobSearchSkillEntity();
        entity.setName(skill);
        return entity;
      }).toList());
      analysis.setExtractedResponsibilities(getList(aiJson, "responsibilities")
        .stream()
        .filter(item -> item.trim().length() > 0)
        .map(responsibility -> {
        JobSearchResponsibilityEntity entity = new JobSearchResponsibilityEntity();
        entity.setName(responsibility);
        return entity;
      }).toList());
      jobDescription.setAnalysis(analysis);
      sink.success(jobDescription);
    } catch (Exception ex) {
      sink.error(ex);
    } finally {
      response.close();
    }
  }

  private String getText(JsonNode aiJson, String path) {
    JsonNode node = aiJson.path(path);
    String text;
    if (node.isArray()) {
      StringBuilder sb = new StringBuilder();
      for (JsonNode item : node) {
        if (sb.length() > 0)
          sb.append(", ");
        sb.append(item.asText());
      }
      text = sb.toString();
    } else {
      text = node.asText();
    }
    System.out.println(path + ": " + text);
    if (text != null && text.length() > 0) {
      return text;
    }
    return null;
  }

  private List<String> getList(JsonNode aiJson, String path) {
    JsonNode node = aiJson.path(path);
    List<String> list = new ArrayList<>();
    if (node.isArray()) {
      for (JsonNode item : node) {
        list.add(item.asText());
      }
    } else {
      for (String item: node.asText().split(",")) {
        list.add(item);
      }
    }
    System.out.println(path + ": " + list);
    return list;
  }
}
