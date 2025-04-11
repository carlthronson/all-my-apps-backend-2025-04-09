package personal.carl.thronson.jobsearch.data.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
   * Every Company needs a Phase
   * But a Phase does not need a Company
   */
  @ManyToOne
  /**
   * The Phase is created first
   * And then the Company is created and refers to the Phase
   * Meaning Company is the owner of the relationship
   * And the company table contains the phase_id column
   */
  @JoinColumn(name = "phase_id", nullable = true, unique = false)
  /**
   * For Json
   * Every Company should include the Phase
   */
  @JsonManagedReference(value = "company-phase")
  @Getter
  @Setter
  private JobSearchPhaseEntity phase;

  /**
   * A Company can haver zero or more Tasks
   * And a Task needs at least one Company
   * 
   * The Company is created first
   * And then the Task is created and refers to the Company
   * Meaning Task is the owner of the relationship
   * And the task table contains the company_id column
   */
  @OneToMany(mappedBy = "company")
  /**
   * For Json
   * Every Company should include the Tasks
   */
  @JsonManagedReference(value = "company-task")
  @Getter
  @Setter
  private List<JobSearchTaskEntity> tasks;
}
