/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

package bio.overture.song.server.repository;

import bio.overture.song.server.model.entity.AnalysisSchema;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface AnalysisSchemaRepository
    extends JpaRepository<AnalysisSchema, Integer>, JpaSpecificationExecutor<AnalysisSchema> {

  Integer countAllByName(String name);

  Integer countAllByNameAndIdLessThanEqual(String name, Integer id);

  Optional<AnalysisSchema> findByNameAndVersion(String name, Integer version);

  List<AnalysisSchema> findAllByName(String name);
}
