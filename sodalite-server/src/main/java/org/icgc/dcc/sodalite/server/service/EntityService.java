package org.icgc.dcc.sodalite.server.service;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.icgc.dcc.sodalite.server.model.Donor;
import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.model.Specimen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntityService {

  public static final String DONOR_ID_PREFIX = "DO";
  public static final String SPECIMEN_ID_PREFIX = "SP";
  public static final String SAMPLE_ID_PREFIX = "SA";
  public static final String MUTATION_ID_PREFIX = "MU";
  public static final String FILE_ID_PREFIX = "FI";
  public static final int SUCCESS = 1; // sql functions return 1 for success, 0 for failure
  ObjectMapper mapper = new ObjectMapper();

  /*
   * Dependencies
   */
  @Autowired
  IdService idService;

  @Autowired
  StudyService studyService;
  @Autowired
  DonorService donorService;
  @Autowired
  SpecimenService specimenService;
  @Autowired
  SampleService sampleService;
  @Autowired
  FileService fileService;

  private void info(String fmt, Object... args) {
    log.info(format(fmt, args));
  }

  @SneakyThrows
  public String create(String studyId, String json) {
    long start = System.currentTimeMillis();

    info("Creating a new entity for study '%s', with json '%s'", studyId, json);

    val study = studyService.getStudy(studyId);
    if (study == null) {
      return "{\"status\": \"Study " + studyId + " does not exist: please create it first.\"}";
    }

    @NonNull
    val node = mapper.readTree(json);

    @NonNull
    val create = node.path("createEntity");
    info("Got createEntity node '%s", create.toString());

    val donors = create.path("donors");
    assert (donors.isContainerNode());
    info("donors is '%s", donors.toString());
    val donorList = new ArrayList<Donor>();
    for (val n : donors) {
      info("Converting this JSON to a DONOR object '%s'", n.toString());
      val donor = mapper.treeToValue(n, Donor.class);
      donorList.add(donor);
      donorService.create(studyId, donor);
    }
    study.setDonors(donorList);
    long end = System.currentTimeMillis();
    info("Elapsed time = %d seconds", (end - start) / 1000);
    return mapper.writeValueAsString(study);
  }

  public String getEntityById(String id) {
    String type = id.substring(0, 2);

    switch (type) {
    case DONOR_ID_PREFIX:
      return json(donorService.getById(id));
    case SPECIMEN_ID_PREFIX:
      return json(specimenService.getById(id));
    case SAMPLE_ID_PREFIX:
      return json(sampleService.getById(id));
    case FILE_ID_PREFIX:
      return json(fileService.getById(id));
    default:
      return "Error: Unknown ID type" + type;
    }
  }

  @SneakyThrows
  private String json(Object o) {
    return mapper.writeValueAsString(o);
  }

  @SneakyThrows
  public String update(String studyId, String json) {
    info("Updating entities for study '%s', with json '%s'", studyId, json);

    @NonNull
    val root = mapper.readTree(json);
    assert (root.isContainerNode()); // array of entities

    int i = 0;
    for (JsonNode n : root) {
      i = i + 1;
      val id = findId(n);
      if ("".equals(id)) {
        return "{\"error\": \"Can't find an id field for item " + json(n) + "\"}";
      }
      info("Looking up type of '%s'", id);
      val type = id.substring(0, 2);

      switch (type) {
      case DONOR_ID_PREFIX:
        donorService.update(mapper.convertValue(n, Donor.class));
        break;
      case SPECIMEN_ID_PREFIX:
        specimenService.update(mapper.convertValue(n, Specimen.class));
        break;
      case SAMPLE_ID_PREFIX:
        sampleService.update(mapper.convertValue(n, Sample.class));
        break;
      case FILE_ID_PREFIX:
        fileService.update(mapper.convertValue(n, File.class));
        break;
      default:
        return "Error: Unknown ID type" + type;
      }

    }
    return "OK";
  }

  String findId(JsonNode n) {
    String[] names = { "studyId", "donorId", "specimenId", "sampleId", "objectId" };
    info("Looking for id for %s", n.toString());
    for (val name : names) {
      val idNode = n.get(name);
      if (idNode == null || idNode.isMissingNode()) {
        info("Couldn't find node type '%s'", name);
      } else {
        info("Returning value for '%s',value=%s", name, idNode.asText());
        return idNode.asText();
      }
    }
    return "";
  }

  public String delete(List<String> ids) {
    val results = new HashMap<String, String>();
    ids.forEach(id -> results.put(id, deleteId(id)));
    return json(results);
  }

  String statusMsg(int status) {
    if (status == SUCCESS) {
      return "OK";
    } else {
      return "FAILED";
    }
  }

  String deleteId(String id) {
    String type = id.substring(0, 2);

    switch (type) {
    case DONOR_ID_PREFIX:
      return donorService.delete(id);
    case SPECIMEN_ID_PREFIX:
      return specimenService.delete(id);
    case SAMPLE_ID_PREFIX:
      return sampleService.delete(id);
    case FILE_ID_PREFIX:
      return fileService.delete(id);
    default:
      return "Error: Unknown ID type" + type;
    }
  }
}
