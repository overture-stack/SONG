package org.icgc.dcc.song.server.model.entity.composites;

import lombok.Data;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;

import java.util.List;
@Data
public class AnalysisSample extends Sample {
    private String sampleSubmitterId;
    private String sampleType;
    private Specimen specimen;
    private Donor donor;
}
