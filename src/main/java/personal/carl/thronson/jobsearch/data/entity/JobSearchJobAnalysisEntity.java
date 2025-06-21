package personal.carl.thronson.jobsearch.data.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.workflow.data.core.ProcessElement;

@Entity(name = "job_search_job_analysis")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JobSearchJobAnalysisEntity extends ProcessElement {

  @Getter @Setter private String extractedTitle;
  @Getter @Setter private String extractedLocation;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
      name = "job_analysis_skill",
      joinColumns = @JoinColumn(name = "job_analysis_id"),
      inverseJoinColumns = @JoinColumn(name = "skill_id")
  )
  @Getter @Setter private List<JobSearchSkillEntity> extractedSkills = new ArrayList<>();

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
      name = "job_analysis_responsibility",
      joinColumns = @JoinColumn(name = "job_analysis_id"),
      inverseJoinColumns = @JoinColumn(name = "responsibility_id")
  )
  @Getter @Setter private List<JobSearchResponsibilityEntity> extractedResponsibilities = new ArrayList<>();
  @Getter @Setter private double[] vector;

  /**
   * Every analysis needs exactly one description
   * And every description needs exactly one analysis
   */
  @OneToOne
  /**
   * The description is created first
   * And then the analysis is created and refers to the description
   * Meaning analysis is the owner of the relationship
   * And the analysis table contains the description_id column
   */
  @JoinColumn(name = "description_id", nullable = false, unique = true)
  /**
   * For Json
   * Every analysis should not include the description
   */
  @JsonManagedReference(value = "description-analysis")
  @Getter
  @Setter
  private JobSearchJobDescriptionEntity description;
}
