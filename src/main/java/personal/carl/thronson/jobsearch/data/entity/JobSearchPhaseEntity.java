package personal.carl.thronson.jobsearch.data.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.workflow.data.core.Phase;

@Entity(name = "job_search_phase")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JobSearchPhaseEntity extends Phase {

  public static final String JOB_SEARCH_PHASE_SEARCH = "JOB_SEARCH_PHASE_SEARCH";

  /**
   * A Phase can haver zero or more Statuses
   * And a Status must have exactly one Phase
   * 
   * The Phase is created first
   * And then the Status is created and refers to the Phase
   * Meaning Status is the owner of the relationship
   * And the status table contains the phase_id column
   */
  @OneToMany(mappedBy = "phase")
  /**
   * For Json
   * The Phase should not include the Statuses
   */
  @JsonBackReference(value = "status-phase")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Getter
  @Setter
  private List<JobSearchStatusEntity> statuses;

  /**
   * A Phase can haver zero or more Stories
   * And a Company must have exactly one Phase
   * 
   * The Phase is created first
   * And then the Company is created and refers to the Phase
   * Meaning Company is the owner of the relationship
   * And the company table contains the phase_id column
   */
  @OneToMany(mappedBy = "phase")
  /**
   * For Json
   * The Phase should not include the Stories
   */
  @JsonBackReference(value = "company-phase")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Getter
  @Setter
  private List<JobSearchCompanyEntity> stories;
}
