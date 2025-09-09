package personal.carl.thronson.workflow.data.core;

import java.util.Map;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.core.BaseObject;

@MappedSuperclass
public class ProcessElement extends BaseObject {

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String label;

  @Override
  public String toString() {
    return this.name;
  }

  public static ProcessElement getProcessElement(Map<String, Object> map, ProcessElement element) {
    if (map.containsKey("id") && map.get("id") != null)
      element.setId(Long.parseLong(map.get("id").toString()));
    if (map.containsKey("name") && map.get("name") != null)
      element.setName(map.get("name").toString());
    if (map.containsKey("label") && map.get("label") != null)
      element.setLabel(map.get("label").toString());
    return element;
  }

  @Override
  public Map<String, Object> getMetaData() throws Exception {
    Map<String, Object> map = super.getMetaData();
//    System.out.println("base metadata: " + map);
    map.put("name", name);
    map.put("label", label);
    return map;
  }
}
