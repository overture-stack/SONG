package org.icgc.dcc.song.server.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import org.hibernate.annotations.Type;
import org.icgc.dcc.song.core.utils.JsonUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.icgc.dcc.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;

@ToString(callSuper = true)
@Data
@Entity
@Table(name = "Info")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Info {

  @Id
  @Column(name = "id", updatable = false, unique = true, nullable = false)
  private String id;


  @Column(name = "id_type", nullable = false)
  private String idType;

//  @Type(type = "com.marvinformatics.hibernate.json.JsonUserType")
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private Map<String, Object> info;

  public static Info createInfo(@NonNull String id, @NonNull String idType, Map<String, Object> info){
    val i = new Info();
    i.setId(id);
    i.setIdType(idType);
    i.setInfo(info);
    return i;
  }

  @SneakyThrows
  public static Info createInfo(@NonNull String id, @NonNull String idType, String jsonInfo){

    Map<String, Object> map = null;
    if (!isNull(jsonInfo)){
      map = JsonUtils.toMap(jsonInfo);
    }
    return createInfo(id, idType, map);
  }


}
