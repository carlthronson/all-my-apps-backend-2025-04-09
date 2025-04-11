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
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.workflow.data.core.Status;

@Entity(name = "job_search_status")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JobSearchStatusEntity extends Status {

  public static final String JOB_SEARCH_STATUS_FOUND = "JOB_SEARCH_STATUS_FOUND";

  /**
   * Every Status needs a Phase
   * But a Phase does not need a Status
   */
  @ManyToOne
  /**
   * The Phase is created first
   * And then the Status is created and refers to the Phase
   * Meaning Status is the owner of the relationship
   * And the status table contains the phase_id column
   */
  @JoinColumn(name = "phase_id", nullable = true, unique = false)
  /**
   * For Json
   * Every Status should include the Phase
   */
  @JsonManagedReference(value = "status-phase")
  @Getter
  @Setter
  private JobSearchPhaseEntity phase;

  /**
   * A Status can haver zero or more Tasks
   * And a Task must have exactly one Status
   * 
   * The Status is created first
   * And then the Task is created and refers to the Status
   * Meaning Task is the owner of the relationship
   * And the task table contains the status_id column
   */
  @OneToMany(mappedBy = "status")
  /**
   * For Json
   * The Status should not include the Tasks
   */
  @JsonBackReference(value = "task-status")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Getter
  @Setter
  private List<JobSearchTaskEntity> tasks;
}
