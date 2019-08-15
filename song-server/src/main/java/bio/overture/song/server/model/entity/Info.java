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

import static bio.overture.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;
import static java.util.Objects.isNull;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.enums.TableAttributeNames;
import bio.overture.song.server.model.enums.TableNames;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import org.hibernate.annotations.Type;

@Entity
@Table(name = TableNames.INFO)
@Data
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Info {

  @EmbeddedId private InfoPK infoPK;

  @Column(name = TableAttributeNames.INFO)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private Map<String, Object> info;

  public static Info createInfo(@NonNull InfoPK infoPK, Map<String, Object> info) {
    val i = new Info();
    i.setInfo(info);
    i.setInfoPK(infoPK);
    return i;
  }

  @SneakyThrows
  public static Info createInfo(@NonNull InfoPK infoPK, String jsonInfo) {
    Map<String, Object> map = null;
    if (!isNull(jsonInfo)) {
      map = JsonUtils.toMap(jsonInfo);
    }
    return createInfo(infoPK, map);
  }
}
