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

package bio.overture.song.server.model.legacy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Immutable;
import bio.overture.song.server.model.enums.TableAttributeNames;
import bio.overture.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.FILE)
@Immutable
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegacyEntity implements Legacy {

  @Id
  @Column(name = TableAttributeNames.ID, unique = true, nullable = false,
      insertable = false, updatable = false)
  private String id;

  @Column(name = TableAttributeNames.ANALYSIS_ID, unique = false, nullable = false,
      insertable = false, updatable = false)
  private String gnosId;

  @Column(name = TableAttributeNames.NAME, unique = false, nullable = false,
      insertable = false, updatable = false)
  private String fileName;

  @Column(name = TableAttributeNames.STUDY_ID, unique = false, nullable = false,
      insertable = false, updatable = false)
  private String projectCode;

  @Column(name = TableAttributeNames.ACCESS, unique = false, nullable = false,
      insertable = false, updatable = false)
  private String access;

}
