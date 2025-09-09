package personal.carl.thronson.jobsearch.data.entity;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
  private String employmentType;

  // Custom field
  @Getter
  @Setter
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
  private OffsetDateTime publishedAt;

  /**
   * Every Job needs a Company
   * But a Company does not need a Job
   */
  @ManyToOne
  /**
   * The Company is created first
   * And then the Job is created and refers to the Company
   * Meaning Job is the owner of the relationship
   * And the job table contains the company_id column
   */
  @JoinColumn(name = "company_id", nullable = true, unique = false)
  /**
   * For Json
   * Every Job should include the Company
   */
  @JsonBackReference(value = "job-company")
  @Getter
  @Setter
  private JobSearchCompanyEntity company;

  /**
   * Every Job needs exactly one Task
   * And every Task needs exactly one Job
   */
  @OneToOne
  /**
   * The Task is created first
   * And then the Job is created and refers to the Task
   * Meaning Job is the owner of the relationship
   * And the job table contains the task_id column
   */
  @JoinColumn(name = "task_id", nullable = true, unique = false)
  /**
   * For Json
   * Every Job should not include the Task
   */
  @JsonManagedReference(value = "task-job")
  @Getter
  @Setter
  private JobSearchTaskEntity task;

  @Getter
  @Setter
  private List<String> keywords;

  /**
   * Every description needs exactly one listing
   * And every listing needs exactly one description
   */
  @OneToOne(mappedBy = "listing", cascade = CascadeType.ALL)
  /**
   * The listing is created first
   * And then the description is created and refers to the listing
   * Meaning description is the owner of the relationship
   * And the description table contains the listing_id column
   */
  /**
   * For Json
   * The Listing should include the Description
   */
  @JsonBackReference(value = "listing-description")
  @Getter
  @Setter
  private JobSearchJobDescriptionEntity description;

  @Column(columnDefinition = "boolean default false")
  @Getter
  @Setter
  private boolean hasTitleVector;

  @Override
  public Map<String, Object> getMetaData() throws Exception {
    Map<String, Object> map = super.getMetaData();
    map.put("company", this.getCompany());
    map.put("companyName", companyName);
    map.put("contracttype", contracttype);
    map.put("employmentType", employmentType);
    map.put("experiencelevel", experiencelevel);
    map.put("keywords", keywords);
    map.put("linkedinid", linkedinid);
    map.put("linkedinurl", linkedinurl);
    map.put("location", location);
    map.put("publishedAt", publishedAt);
    map.put("salary", salary);
    map.put("sector", sector);
    map.put("task", this.getTask());
//    System.out.println("metadata: " + map);
    return map;
  }

  public static JobSearchJobListingEntity fromMetaData(Map<String, Object> map) {
    JobSearchJobListingEntity entity = new JobSearchJobListingEntity();
    getProcessElement(map, entity);

    // For nested objects like 'company':
    Map<String,Object> companyObj = (Map<String, Object>) map.get("company");
    if (companyObj != null) {
      JobSearchCompanyEntity company = new JobSearchCompanyEntity();
      getProcessElement(companyObj, company);
      company.setLocation(companyObj.get("location").toString());
      entity.setCompany(company);
    }
    entity.setCompanyName((String) map.get("companyName"));
    entity.setContracttype((String) map.get("contracttype"));
    entity.setEmploymentType((String) map.get("employmentType"));
    entity.setExperiencelevel((String) map.get("experiencelevel"));
    entity.setKeywords((List<String>) map.get("keywords"));
    entity.setLinkedinid((long) map.get("linkedinid"));
    entity.setLinkedinurl((String) map.get("linkedinurl"));
    entity.setLocation((String) map.get("location"));
//    if (map.containsKey("createdAt")) {
//      System.out.println("************* createdAt: " + map.get("createdAt"));
//    } else {
//      System.out.println("************* createdAt: NOT FOUND");
//    }
//    if (map.containsKey("publishedAt")) {
//      System.out.println("************* publishedAt: " + map.get("publishedAt"));
//    } else {
//      System.out.println("************* publishedAt: NOT FOUND");
//    }
    Object publishedAtObj = map.get("publishedAt");
    if (publishedAtObj instanceof OffsetDateTime) {
        OffsetDateTime dt = (OffsetDateTime) publishedAtObj;
        entity.setPublishedAt(dt);
        // use dt
    } else if (publishedAtObj instanceof Number) {
        double epoch = ((Number) publishedAtObj).doubleValue();
        long seconds = (long) epoch;
        int nanos = (int)((epoch - seconds) * 1_000_000_000);
        OffsetDateTime odt = OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanos), ZoneOffset.UTC);
        entity.setPublishedAt(odt);
        // convert epoch float to OffsetDateTime
    } else if (publishedAtObj instanceof String) {
        OffsetDateTime dt = OffsetDateTime.parse((String) publishedAtObj);
        entity.setPublishedAt(dt);
        // use dt
    }
    entity.setSalary((String) map.get("salary"));
    entity.setSector((String) map.get("sector"));
//    entity.setTask((JobSearchTaskEntity) map.get("task"));

    Map<String,Object> taskObj = (Map<String, Object>) map.get("task");
    if (taskObj != null) {
      JobSearchTaskEntity taskEntity = new JobSearchTaskEntity();
      getProcessElement(taskObj, taskEntity);

      Map<String,Object> statusObj = (Map<String, Object>) taskObj.get("status");
      if (statusObj != null) {
        JobSearchStatusEntity statusEntity = new JobSearchStatusEntity();
        getProcessElement(statusObj, statusEntity);                
        Map<String,Object> phaseObj = (Map<String, Object>) statusObj.get("phase");
        if (phaseObj != null) {
          JobSearchPhaseEntity phaseEntity = new JobSearchPhaseEntity();
          getProcessElement(phaseObj, phaseEntity);
          statusEntity.setPhase(phaseEntity);
        }
        taskEntity.setStatus(statusEntity);
      }
      entity.setTask(taskEntity);
    }
    
    return entity;
  }

}
