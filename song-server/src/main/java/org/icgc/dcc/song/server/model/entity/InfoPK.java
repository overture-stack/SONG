package org.icgc.dcc.song.server.model.entity;

import lombok.Data;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class InfoPK implements Serializable{

//  @Id
  @Column(name = TableAttributeNames.ID, nullable = false)
  private String id;

//  @Id
  @Column(name = TableAttributeNames.ID_TYPE, nullable = false)
  private String idType;

  public static InfoPK createInfoPK(@NonNull String id, @NonNull String idType){
    val i = new InfoPK();
    i.setId(id);
    i.setIdType(idType);
    return i;
  }

}
