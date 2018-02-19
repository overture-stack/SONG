package org.icgc.dcc.song.importer.dao;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.importer.model.PcawgSampleBean;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class PcawgSampleSheetDao {

  /**
   * Dependencies
   */
  @NonNull private final Map<String, List<PcawgSampleBean>> donorMap;

  /**
   * State
   */

  /**
   * Note: This function is very pessimistic and constrains the search significantly to avoid duplicates or any
   * other unexpected collisions
   */
  public String findNormalSubmitterSampleId(
      @NonNull String icgc_donor_id,
      @NonNull String dcc_project_code,
      @NonNull String library_strategy){
    val list = donorMap.get(icgc_donor_id).stream()
        .filter(x -> x.getDcc_project_code().equals(dcc_project_code))
        .filter(x -> x.getLibrary_strategy().equals(library_strategy))
        .filter(x -> x.getDcc_specimen_type().toLowerCase().contains("normal"))
        .collect(toList());


    checkState(!list.isEmpty(),
        "No normal submitter sample id found for the query: icgc_donor_id=%s, dcc_procject_code=%s, "
            +"library_strategy=%s",
        icgc_donor_id, dcc_project_code, library_strategy);

    checkState(list.size() == 1,
        "There is more than 1 result (%s) for the query: "
            + "icgc_donor_id=%s, dcc_procject_code=%s, library_strategy=%s",
        list.size(), icgc_donor_id, dcc_project_code, library_strategy);
    return list.get(0).getSubmitter_sample_id();
  }


  public static PcawgSampleSheetDao createPcawgSampleSheetDao(List<PcawgSampleBean> beans) {
    return new PcawgSampleSheetDao(beans.stream().collect(groupingBy(PcawgSampleBean::getIcgc_donor_id)));
  }

}
