package org.icgc.dcc.song.server.importer.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static org.icgc.dcc.song.server.importer.convert.DonorConverter.getDonorId;

@RequiredArgsConstructor
public class DonorDao  {

  @NonNull private final Map<String,PortalDonorMetadata> portalDonorMetadataMap;

  public PortalDonorMetadata getPortalDonorMetadata(String donorId){
    checkArgument(portalDonorMetadataMap.containsKey(donorId),
        "The donorId [%s] DNE", donorId);
    return portalDonorMetadataMap.get(donorId);
  }

  public static DonorDao createDonorDao(
      Map<String, PortalDonorMetadata> portalDonorMetadataMap) {
    return new DonorDao(portalDonorMetadataMap);
  }

  public static DonorDao createDonorDao(Iterable<PortalDonorMetadata> portalDonorMetadataIterable){
    val map = Maps.<String, PortalDonorMetadata>newHashMap();
    for (val portalDonorMetadata : portalDonorMetadataIterable){
      val donorId = getDonorId(portalDonorMetadata);
      checkArgument(!map.containsKey(donorId), "Map cannot have duplicates for donorId [%s]", donorId);
      map.put(donorId, portalDonorMetadata);
    }
    return createDonorDao(ImmutableMap.copyOf(map));
  }

}
