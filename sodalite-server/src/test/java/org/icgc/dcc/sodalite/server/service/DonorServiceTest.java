package org.icgc.dcc.sodalite.server.service;

import org.icgc.dcc.sodalite.server.model.Donor;
import org.icgc.dcc.sodalite.server.model.DonorGender;
import org.icgc.dcc.sodalite.server.model.Specimen;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.val;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DonorServiceTest {
	@Autowired
	DonorService service;
	@Autowired
	SpecimenService specimenService;

	
	@Test
	public void testGetDonorFields() {
		Donor d=service.getById("DO1");
		assertThat(d != null);
		assertThat(d.getDonorId()).isEqualTo("DO1");
		assertThat(d.getDonorGender()).isEqualTo(DonorGender.MALE);
		assertThat(d.getDonorSubmitterId()).isEqualTo("Subject-X23Alpha7");
	}
	
	@Test
	public void testGotRightSpecimens() {
		Donor d=service.getById("DO1");
		assertThat(d.getSpecimens().size()).isEqualTo(2);
		// Ensure that the specimen that we got has the same contents as the one we get from the
		// specimen service. We'll check that those specimen contents are correct in the tests for the specimen
		// service, and that the samples are correct in the sample service.
		for(val specimen:d.getSpecimens()) {
			assertThat(specimen).isEqualToIgnoringGivenFields(getMatchingSpecimen(specimen),  "samples");
		}
	}
	
	Specimen getMatchingSpecimen(Specimen specimen) {
		return specimenService.getById(specimen.getSpecimenId());
	}
	

}
