package personal.carl.thronson.jobsearch.data.entity;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
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
  private String worktype;

  // Custom field
  @Getter
  @Setter
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
  private OffsetDateTime publishedAt;

  /**
   * A Job must have exactly one Task
   * And a Task must have exactly one Job
   * 
   * The Job is created first
   * And then the Task is created and refers to the Job
   * Meaning Task is the owner of the relationship
   * And the task table contains the job_id column
   */
  @OneToOne(mappedBy = "job")
  /**
   * For Json
   * The Job should not include the Task
   */
  @JsonBackReference(value = "task-job")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Getter
  @Setter
  private JobSearchTaskEntity task;
}
