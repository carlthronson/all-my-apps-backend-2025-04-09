package personal.carl.thronson.jobsearch.data.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.workflow.data.core.ProcessElement;

@Entity(name = "job_search_responsibilities")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JobSearchResponsibilityEntity extends ProcessElement {

  @ManyToMany(mappedBy = "extractedResponsibilities")
  @Getter @Setter
  private List<JobSearchJobAnalysisEntity> analysis;
}
