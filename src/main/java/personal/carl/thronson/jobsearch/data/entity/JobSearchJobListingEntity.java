package personal.carl.thronson.jobsearch.data.entity;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.workflow.data.core.ProcessElement;

@Entity(name = "job_search_job_listing")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JobSearchJobListingEntity extends ProcessElement {

  @Getter
  @Setter
  private String companyName;

  @Getter
  @Setter
  private String location;

  @Getter
  @Setter
  @Column(unique = true)
  private long linkedinid;

  // Custom field
  @Getter
  @Setter
  private String linkedinurl;

  // Custom field
  @Getter
  @Setter
  private String contracttype;

  // Custom field
  @Getter
  @Setter
  private String experiencelevel;

  // Custom field
  @Getter
  @Setter
  private String salary;

  // Custom field
  @Getter
  @Setter
  private String sector;

  // Custom field
  @Getter
  @Setter
  private String employmentType;

  // Custom field
  @Getter
  @Setter
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
  private OffsetDateTime publishedAt;

  /**
   * Every Job needs a Company
   * But a Company does not need a Job
   */
  @ManyToOne
  /**
   * The Company is created first
   * And then the Job is created and refers to the Company
   * Meaning Job is the owner of the relationship
   * And the job table contains the company_id column
   */
  @JoinColumn(name = "company_id", nullable = true, unique = false)
  /**
   * For Json
   * Every Job should include the Company
   */
  @JsonBackReference(value = "job-company")
  @Getter
  @Setter
  private JobSearchCompanyEntity company;

  /**
   * Every Job needs exactly one Task
   * And every Task needs exactly one Job
   */
  @OneToOne
  /**
   * The Task is created first
   * And then the Job is created and refers to the Task
   * Meaning Job is the owner of the relationship
   * And the job table contains the task_id column
   */
  @JoinColumn(name = "task_id", nullable = true, unique = false)
  /**
   * For Json
   * Every Job should not include the Task
   */
  @JsonManagedReference(value = "task-job")
  @Getter
  @Setter
  private JobSearchTaskEntity task;

  @Getter
  @Setter
  private List<String> keywords;

  /**
   * Every description needs exactly one listing
   * And every listing needs exactly one description
   */
  @OneToOne(mappedBy = "listing", cascade = CascadeType.ALL)
  /**
   * The listing is created first
   * And then the description is created and refers to the listing
   * Meaning description is the owner of the relationship
   * And the description table contains the listing_id column
   */
  /**
   * For Json
   * The Listing should include the Description
   */
  @JsonBackReference(value = "listing-description")
  @Getter
  @Setter
  private JobSearchJobDescriptionEntity description;

  @Column(columnDefinition = "boolean default false")
  @Getter
  @Setter
  private boolean hasTitleVector;

  public Map<String, Object> getMetaData() {
    Map<String, Object> map = new HashMap<>();
    map.put("name", this.getName());
    map.put("linkedinid", this.getLinkedinid());
    return map;
  }
}
