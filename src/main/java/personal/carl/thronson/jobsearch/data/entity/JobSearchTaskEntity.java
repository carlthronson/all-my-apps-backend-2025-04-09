package personal.carl.thronson.jobsearch.data.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.workflow.data.core.Task;

@Entity(name = "job_search_task")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JobSearchTaskEntity extends Task {

  /**
   * Every Task needs a Status
   * But a Status does not need a Task
   */
  @ManyToOne
  /**
   * The Status is created first
   * And then the Task is created and refers to the Status
   * Meaning Task is the owner of the relationship
   * And the task table contains the status_id column
   */
  @JoinColumn(name = "status_id", nullable = true, unique = false)
  /**
   * For Json
   * Every Task should include the Status
   */
  @JsonManagedReference(value = "task-status")
  @Getter
  @Setter
  private JobSearchStatusEntity status;

  /**
   * Every Task needs a Company
   * And every Company needs a Task
   */
  @OneToOne
  /**
   * The Company is created first
   * And then the Task is created and refers to the Company
   * Meaning Task is the owner of the relationship
   * And the task table contains the company_id column
   */
  @JoinColumn(name = "company_id", nullable = true, unique = false)
  /**
   * For Json
   * The Task should not include the Company
   */
  @JsonBackReference(value = "company-task")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Getter
  @Setter
  private JobSearchCompanyEntity company;

  /**
   * Every Task needs a Job
   * And every Job needs a Task
   */
  @OneToOne
  /**
   * The Job is created first
   * And then the Task is created and refers to the Job
   * Meaning Task is the owner of the relationship
   * And the task table contains the job_id column
   */
  @JoinColumn(name = "job_id", nullable = true, unique = false)
  /**
   * For Json
   * Every Task should include the Job
   */
  @JsonManagedReference(value = "task-job")
  @Getter
  @Setter
  private JobSearchJobListingEntity job;
}
