package org.icgc.dcc.song.server.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import org.hibernate.annotations.Type;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.icgc.dcc.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;

@Entity
@Table(name = TableNames.INFO)
@Data
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Info {

  @EmbeddedId
  private InfoPK infoPK;

  @Column(name = TableAttributeNames.INFO)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private Map<String, Object> info;

  public static Info createInfo(@NonNull InfoPK infoPK, Map<String, Object> info){
    val i = new Info();
    i.setInfo(info);
    i.setInfoPK(infoPK);
    return i;
  }

  @SneakyThrows
  public static Info createInfo(@NonNull InfoPK infoPK, String jsonInfo){
    Map<String, Object> map = null;
    if (!isNull(jsonInfo)){
      map = JsonUtils.toMap(jsonInfo);
    }
    return createInfo(infoPK, map);
  }

}
