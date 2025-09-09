package personal.carl.thronson.ai.data;

import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;

public class JobVector {

  @Getter @Setter private String text;
  @Getter @Setter private Double score;
  @Getter @Setter private String metadata;
  @Getter @Setter private JobSearchJobListingEntity jobListing;
}
