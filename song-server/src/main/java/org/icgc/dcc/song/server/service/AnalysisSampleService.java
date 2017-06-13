package org.icgc.dcc.song.server.service;

import lombok.AllArgsConstructor;

import lombok.val;
import org.icgc.dcc.song.server.model.entity.composites.AnalysisSample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AnalysisSampleService {

    @Autowired
    private final SampleService sampleService;

    @Autowired
    private final SpecimenService specimenService;

    @Autowired
    private final DonorService donorService;

    public String save(String studyId, AnalysisSample s) {
        String id =sampleService.findByBusinessKey(studyId, s.getSampleSubmitterId());
        if (id == null) {
            val parentId = getSampleParent(studyId, s);
            id = sampleService.create(parentId, s);
        } else {
            sampleService.update(s);
        }

        return id;
    }

    private String getSampleParent(String studyId, AnalysisSample s) {
        val specimen = s.getSpecimen();
        String id = specimenService.findByBusinessKey(studyId, specimen.getSpecimenSubmitterId());
        if (id == null) {
            val parentId = getSpecimenParent(studyId, s);
            id = specimenService.create(parentId, specimen);
        } else {
            specimenService.update(specimen);
        }
        return id;
    }

    private String getSpecimenParent(String studyId, AnalysisSample s) {
       return donorService.save(studyId, s.getDonor());
    }

    public AnalysisSample read(String sampleId) {
        val sample = AnalysisSample.create(sampleService.read(sampleId));
        sample.setSpecimen(specimenService.read(sample.getSpecimenId()));
        sample.setDonor(donorService.read(sample.getSpecimen().getDonorId()));
        return sample;
    }

}
