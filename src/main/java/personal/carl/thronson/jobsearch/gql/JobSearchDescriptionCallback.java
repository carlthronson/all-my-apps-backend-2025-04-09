package personal.carl.thronson.jobsearch.gql;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import personal.carl.thronson.http.JobSummaryReader;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobDescriptionEntity;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;
import reactor.core.publisher.MonoSink;

public class JobSearchDescriptionCallback implements Callback {

  private static final String[] patterns = new String[]{
    "LinkedIn",
    "Skip to main content",
    "Expand search",
    "This button displays the currently selected search type.",
    "When expanded it provides a list of search options that will switch the search inputs to match the current selection",
    "Jobs People Learning",
    "Clear text",
    "Join now",
    "Sign in",
    "Apply ",
    "Join",
    "sign in",
    "find your next job",
    "Join",
    "apply for the",
    "Remove photo"
  };

  Logger logger = Logger.getLogger(getClass().getName());

  MonoSink<JobSearchJobListingEntity> sink;
  JobSearchJobListingEntity jobMetaData;

  public JobSearchDescriptionCallback(MonoSink<JobSearchJobListingEntity> sink, JobSearchJobListingEntity jobMetaData) {
    this.sink = sink;
    this.jobMetaData = jobMetaData;
  }

  @Override
  public void onFailure(Call call, IOException e) {
    sink.error(e);
  }

//  @Override
  public void _onResponse(Call call, Response response) throws IOException {
    if (response.isSuccessful()) {
      try (ResponseBody responseBody = response.body()) {
        String body = responseBody.string();
        Document doc = Jsoup.parse(body);
        Elements jobCriteriaItems = doc.getElementsByClass("description__job-criteria-item");
        jobCriteriaItems.forEach(jobCriteriaItem -> {
          Elements subheaders = jobCriteriaItem.getElementsByClass("description__job-criteria-subheader");
          subheaders.forEach(subheader -> {
            if (subheader.text().contains("Employment type")) {
              Elements subText = jobCriteriaItem.getElementsByClass("description__job-criteria-text");
              String employmentType = subText.text();
              jobMetaData.setEmploymentType(employmentType);
            }
          });
        });
        Elements hiddenElements = doc.getElementsByClass("show-more-less-html__markup");
        String description = hiddenElements.text();
        if (description != null && description.trim().length() > 0) {
          JobSearchJobDescriptionEntity descriptionEntity = new JobSearchJobDescriptionEntity();
          descriptionEntity.setListing(jobMetaData);
          descriptionEntity.setDescription(description);
          jobMetaData.setDescription(descriptionEntity);
        }
        sink.success(jobMetaData);
      } catch (Exception ex) {
        ex.printStackTrace();
//        sink.error(new IOException("Error parsing response body", ex));
        sink.success(jobMetaData);
      }
    } else {
      System.out.println("Response was unsuccessful: " + response.code());
//      sink.error(new IOException("Unsuccessful response: " + response));
      sink.success(jobMetaData);
    }
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
//    logger.info("Job search list callback onResponse: " + response);
    try (ResponseBody responseBody = response.body()) {
      if (!response.isSuccessful()) {
        sink.error(new IOException("Unexpected code " + response));
      } else {
        if (responseBody != null) {
          String body = responseBody.string();
          Document doc = Jsoup.parse(body);
          Elements jobCriteriaItems = doc.getElementsByClass("description__job-criteria-item");
          jobCriteriaItems.forEach(jobCriteriaItem -> {
            Elements subheaders = jobCriteriaItem.getElementsByClass("description__job-criteria-subheader");
            subheaders.forEach(subheader -> {
              if (subheader.text().contains("Employment type")) {
                Elements subText = jobCriteriaItem.getElementsByClass("description__job-criteria-text");
                String employmentType = subText.text();
                jobMetaData.setEmploymentType(employmentType);
              }
            });
          });
          Elements hiddenElements = doc.getElementsByClass("show-more-less-html__markup");
          String description = hiddenElements.text();
          if (description != null && description.trim().length() > 0) {
            JobSearchJobDescriptionEntity descriptionEntity = new JobSearchJobDescriptionEntity();
            descriptionEntity.setListing(jobMetaData);
            descriptionEntity.setDescription(description);
            jobMetaData.setDescription(descriptionEntity);
          }
          sink.success(jobMetaData);
        } else {
          sink.success(jobMetaData);
        }
      }
    }
  }
}
