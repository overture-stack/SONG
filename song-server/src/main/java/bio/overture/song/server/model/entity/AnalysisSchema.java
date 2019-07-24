package bio.overture.song.server.model.entity;

import bio.overture.song.server.model.enums.TableNames;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.UUID;

import static org.hibernate.annotations.GenerationTime.INSERT;
import static bio.overture.song.server.model.enums.TableAttributeNames.ID;
import static bio.overture.song.server.model.enums.TableAttributeNames.NAME;
import static bio.overture.song.server.model.enums.TableAttributeNames.ORDER_ID;
import static bio.overture.song.server.model.enums.TableAttributeNames.SCHEMA;
import static bio.overture.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = TableNames.ANALYSIS_SCHEMA)
public class AnalysisSchema {

  @Id
  @Column(name = ORDER_ID)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer orderId;

  @Generated(INSERT)
  @Column(name = ID, insertable = false)
  private UUID id;

  @NotNull
  @Column(name = NAME)
  private String name;

  @NotNull
  @Column(name = SCHEMA)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private JsonNode schema;

}
