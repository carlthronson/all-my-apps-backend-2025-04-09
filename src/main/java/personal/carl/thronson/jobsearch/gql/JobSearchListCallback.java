package personal.carl.thronson.jobsearch.gql;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import personal.carl.thronson.http.JobSummaryReader;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;
import reactor.core.publisher.MonoSink;

public class JobSearchListCallback implements Callback {

  Logger logger = Logger.getLogger(getClass().getName());

  MonoSink<List<JobSearchJobListingEntity>> sink;
  public JobSearchListCallback(MonoSink<List<JobSearchJobListingEntity>> sink) {
    this.sink = sink;
  }

  @Override
  public void onFailure(Call call, IOException e) {
    sink.error(e);
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
          try {
            Document doc = Jsoup.parse(body);
            JobSummaryReader jobSummaryReader = new JobSummaryReader();
            List<JobSearchJobListingEntity> list = jobSummaryReader.readDoc(doc);
//            logger.info("Job search results: " + list.size());
            sink.success(list);
          } catch (Exception e) {
            sink.error(new IOException("Error parsing response body", e));
          }
        } else {
          sink.success(Collections.emptyList()); // FIX: Do not emit null!
        }
      }
    }
  }
}
