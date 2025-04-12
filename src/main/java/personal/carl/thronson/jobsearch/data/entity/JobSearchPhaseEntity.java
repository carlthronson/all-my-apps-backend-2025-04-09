package personal.carl.thronson.jobsearch.data.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
  public static final String JOB_SEARCH_PHASE_ACCEPTED = "JOB_SEARCH_PHASE_APPLY";
  public static final String JOB_SEARCH_PHASE_CLOSED = "JOB_SEARCH_PHASE_CLOSED";

  /**
   * A Phase can haver zero or more Statuses
   * And a Status must have exactly one Phase
   * 
   * The Phase is created first
   * And then the Status is created and refers to the Phase
   * Meaning Status is the owner of the relationship
   * And the status table contains the phase_id column
   */
  @OneToMany(mappedBy = "phase", fetch = FetchType.EAGER)
  /**
   * For Json
   * The Phase should include the Statuses
   */
  @JsonBackReference(value = "phase-status")
//  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Getter
  @Setter
  private List<JobSearchStatusEntity> statuses;

}
