package personal.carl.thronson.jobsearch.rest;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;

public class JobGroup {

  @Getter @Setter
  private List<JobSearchJobListingEntity> jobs = new ArrayList<>();

  public void addJob(JobSearchJobListingEntity job) {
    jobs.add(job);
  }

  @Getter @Setter private String name;
  @Getter @Setter private String label;
  @Getter @Setter private String location;
}
