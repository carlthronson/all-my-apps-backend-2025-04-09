package personal.carl.thronson.jobsearch.data.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
   * Every Task should not include the Status
   */
  @JsonManagedReference(value = "status-task")
  @Getter
  @Setter
  private JobSearchStatusEntity status;

  /**
   *   WRONG A Task can haver zero or more Jobs
   *   WRONG And a Job must have exactly one Task
   * 
   * The Task is created first
   * And then the Job is created and refers to the Task
   * Meaning Job is the owner of the relationship
   * And the job table contains the task_id column
   */
  @OneToOne(mappedBy = "task")
  /**
   * For Json
   * The Task should include the Job
   */
  @JsonBackReference(value = "task-job")
//  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Getter
  @Setter
  private JobSearchJobListingEntity job;
}
