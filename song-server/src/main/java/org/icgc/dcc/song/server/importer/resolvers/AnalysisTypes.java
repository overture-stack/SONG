package org.icgc.dcc.song.server.importer.resolvers;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static org.icgc.dcc.song.server.importer.resolvers.FileTypes.BAM;
import static org.icgc.dcc.song.server.importer.resolvers.FileTypes.VCF;

@RequiredArgsConstructor
public enum AnalysisTypes {

  SEQUENCING_READ("sequencingRead", BAM ),
  VARIANT_CALL("variantCall", VCF);

  private static Map<FileTypes, AnalysisTypes> map = Maps.newEnumMap(FileTypes.class);
  static {
    for (val at : values()){
      map.put(at.getFileType(), at);
    }
  }

  @Getter private final String analysisTypeName;
  @Getter private final FileTypes fileType;

  public static AnalysisTypes resolve(FileTypes fileType){
    checkArgument(map.containsKey(fileType),
        "There is no AnalysisType for the fileType [%s]",
        fileType.name());
    return map.get(fileType);
  }


}

