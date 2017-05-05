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

import java.util.ArrayList;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DonorServiceTest {
	@Autowired
	DonorService service;
	@Autowired
	SpecimenService specimenService;

	
	@Test
	public void testReadDonor() {
		// check for data that we know exists in the H2 database already
		Donor d=service.getById("DO1");
		assertThat(d != null);
		assertThat(d.getDonorId()).isEqualTo("DO1");
		assertThat(d.getDonorGender()).isEqualTo(DonorGender.MALE);
		assertThat(d.getDonorSubmitterId()).isEqualTo("Subject-X23Alpha7");
		assertThat(d.getSpecimens().size()).isEqualTo(2);
		
		// Just check that each specimen object that we get is the same as the one we get from the
		// specimen service. Let the specimen service tests verify that the contents are right.
		for(val specimen:d.getSpecimens()) {
			assertThat(specimen.equals(getMatchingSpecimen(specimen)));
		}
		
	}
	
	Specimen getMatchingSpecimen(Specimen specimen) {
		return specimenService.getById(specimen.getSpecimenId());
	}
	
	@Test
	public void testCreateAndDeleteDonor()  {
		Donor d = new Donor()
				.withDonorGender(DonorGender.UNSPECIFIED)
				.withDonorSubmitterId("Subject X21-Alpha")
				.withSpecimens(new ArrayList<Specimen>());
		assertThat(d.getDonorId()).isNull();
		
		String status = service.create("XYZ234", d);
		val id = d.getDonorId();
		
		assertThat(id).startsWith("DO");		
		assertThat(status).isEqualTo("ok:" + id);
	
		Donor check = service.getById(id);
		assertThat(d).isEqualToComparingFieldByField(check);
		
		service.delete(id);
		Donor check2 = service.getById(id);
		assertThat(check2).isNull();
	}
	
	@Test
	public void testUpdateDonor() {
		Donor d = new Donor()
			 	.withDonorGender(DonorGender.MALE)
				.withDonorSubmitterId("Triangle-Arrow-S")
				.withSpecimens(new ArrayList<Specimen>());
		
		service.create("ABC123",d);
		
		val id = d.getDonorId();
		
		Donor d2 = new Donor()
				.withDonorId(id)
				.withDonorGender(DonorGender.FEMALE)
				.withDonorSubmitterId("X21-Beta-17")
				.withSpecimens(new ArrayList<Specimen>());
		
		service.update(d2);
		
		Donor d3 = service.getById(id);
		assertThat(d3).isEqualToComparingFieldByField(d2);	
	}
	

}
