/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.server.model.entity;

import bio.overture.song.server.model.enums.TableAttributeNames;
import lombok.Data;
import lombok.NonNull;
import lombok.val;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class InfoPK implements Serializable{

  @Column(name = TableAttributeNames.ID, nullable = false)
  private String id;

  @Column(name = TableAttributeNames.ID_TYPE, nullable = false)
  private String idType;

  public static InfoPK createInfoPK(@NonNull String id, @NonNull String idType){
    val i = new InfoPK();
    i.setId(id);
    i.setIdType(idType);
    return i;
  }

}
