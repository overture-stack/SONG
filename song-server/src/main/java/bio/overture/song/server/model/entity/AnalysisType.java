package bio.overture.song.server.model.entity;

import bio.overture.song.server.model.enums.TableNames;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.UUID;

import static bio.overture.song.server.model.enums.TableAttributeNames.ID;
import static bio.overture.song.server.model.enums.TableAttributeNames.NAME;
import static bio.overture.song.server.model.enums.TableAttributeNames.SCHEMA;
import static bio.overture.song.server.model.enums.TableAttributeNames.VERSION;
import static bio.overture.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = TableNames.ANALYSIS_TYPE2)
public class AnalysisType {

  @Id
  @Column(name = ID)
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @NotNull
  @Column(name = NAME)
  private String name;

  @NotNull
  @Column(name = SCHEMA)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private JsonNode schema;

  @Generated(GenerationTime.INSERT)
  @Column(name = VERSION, nullable = false, insertable = false)
  private Integer version;

}
