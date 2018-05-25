package org.icgc.dcc.song.server.converter;

import org.icgc.dcc.song.server.model.legacy.Legacy;
import org.icgc.dcc.song.server.model.legacy.LegacyDto;
import org.icgc.dcc.song.server.model.legacy.LegacyEntity;
import org.mapstruct.Mapper;

@Mapper
public interface LegacyEntityConverter {

    LegacyDto convertToLegacyDto(Legacy legacy);
    LegacyEntity convertToLegacyEntity(Legacy legacy);

}
