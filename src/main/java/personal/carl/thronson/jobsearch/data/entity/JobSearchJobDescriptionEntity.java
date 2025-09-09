package personal.carl.thronson.jobsearch.data.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.workflow.data.core.ProcessElement;

@Entity(name = "job_search_job_description")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JobSearchJobDescriptionEntity extends ProcessElement {

  /**
   * Every description needs exactly one listing
   * And every listing needs exactly one description
   */
  @OneToOne
  /**
   * The listing is created first
   * And then the description is created and refers to the listing
   * Meaning description is the owner of the relationship
   * And the description table contains the listing_id column
   */
  @JoinColumn(name = "listing_id", nullable = false, unique = true)
  /**
   * For Json
   * Every description should not include the listing
   */
  @JsonManagedReference(value = "listing-description")
  @Getter
  @Setter
  private JobSearchJobListingEntity listing;

  @Column(columnDefinition = "TEXT")
  @Getter @Setter private String description;

  @Getter @Setter private String employmentType;

  /**
   * Every analysis needs exactly one description
   * And every description needs exactly one analysis
   */
  @OneToOne(mappedBy = "description", cascade = CascadeType.ALL)
  /**
   * The description is created first
   * And then the analysis is created and refers to the description
   * Meaning analysis is the owner of the relationship
   * And the analysis table contains the description_id column
   */
  /**
   * For Json
   * The Description should include the Analysis
   */
  @JsonBackReference(value = "description-analysis")
  @Getter
  @Setter
  private JobSearchJobAnalysisEntity analysis;
}
