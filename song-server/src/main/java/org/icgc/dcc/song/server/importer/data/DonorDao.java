package org.icgc.dcc.song.server.importer.data;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.val;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;

@RequiredArgsConstructor
public class DonorDao {

  private final List<PortalDonorMetadata> donors;

  private Map<String, PortalDonorMetadata> map = newHashMap();

  @NonFinal private boolean init = false;

  public void init(){
    for (val donor : donors){
      checkState(map.containsKey(donor.getId()), "The donorId [%s] already exists. The dao list "
          + "should have unique entries", donor.getId());
      map.put(donor.getId(), donor);
    }
    this.init = true;
  }

  public List<PortalDonorMetadata> findAll(){
    return donors;
  }

  public PortalDonorMetadata find(String donorId){
    checkState(init, "this object has not been initialized");
    checkArgument(map.containsKey(donorId), "The donorId [%s] DNE", donorId);
    return map.get(donorId);
  }

}
