package personal.carl.thronson.jobsearch.data.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.workflow.data.core.Story;

@Entity(name = "job_search_company")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JobSearchCompanyEntity extends Story {

  @Getter
  @Setter
  private String link;

  @Getter
  @Setter
  private String location;

  /**
   * A Company can haver zero or more Jobs
   * And a Job must have exactly one Company
   * 
   * The Company is created first
   * And then the Job is created and refers to the Company
   * Meaning Job is the owner of the relationship
   * And the job table contains the company_id column
   */
  @OneToMany(mappedBy = "company")
  /**
   * For Json
   * The Company should not include the Jobs
   */
  @JsonManagedReference(value = "job-company")
//  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Getter
  @Setter
  private List<JobSearchJobListingEntity> jobs;
}
