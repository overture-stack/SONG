package bio.overture.song.server.converter;

import bio.overture.song.server.config.ConverterConfig;
import bio.overture.song.server.model.dto.RegisterAnalysisTypeResponse;
import bio.overture.song.server.model.entity.AnalysisType;
import org.mapstruct.Mapper;

@Mapper(config = ConverterConfig.class)
public interface StrictConverters {

  RegisterAnalysisTypeResponse analysisTypeEntityToRegisterAnalysusTypeResponse(AnalysisType a);

}
