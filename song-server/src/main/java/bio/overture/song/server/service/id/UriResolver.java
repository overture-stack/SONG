package bio.overture.song.server.service.id;

import bio.overture.song.server.properties.IdProperties;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.difference;
import static lombok.AccessLevel.PRIVATE;
import static bio.overture.song.core.utils.Joiners.COMMA;

@Builder
@RequiredArgsConstructor(access = PRIVATE)
public class UriResolver {

  /**
   * Constants
   */
  private static final String ANALYSIS_ID = "analysisId";
  private static final String FILE_NAME= "fileName";
  private static final String SUBMITTER_ID = "submitterId";
  private static final String STUDY_ID = "studyId";

  /**
   * Dependencies
   */
  @NonNull private final UriTemplate fileUriTemplate;
  @NonNull private final UriTemplate donorUriTemplate;
  @NonNull private final UriTemplate specimenUriTemplate;
  @NonNull private final UriTemplate sampleUriTemplate;
  @NonNull private final UriTemplate analysisExistenceUriTemplate;
  @NonNull private final UriTemplate analysisGenerateUriTemplate;
  @NonNull private final UriTemplate analysisSaveUriTemplate;

  public String expandFileUri(@NonNull String analysisId, @NonNull String fileName){
    return fileUriTemplate.expand(Map.of(ANALYSIS_ID, analysisId, FILE_NAME, fileName)).toString();
  }

  public String expandDonorUri(@NonNull String studyId, @NonNull String submitterId){
    return donorUriTemplate.expand(Map.of(STUDY_ID, studyId, SUBMITTER_ID, submitterId)).toString();
  }

  public String expandSpecimenUri(@NonNull String studyId, @NonNull String submitterId){
    return specimenUriTemplate.expand(Map.of(STUDY_ID, studyId, SUBMITTER_ID, submitterId)).toString();
  }

  public String expandSampleUri(@NonNull String studyId, @NonNull String submitterId){
    return sampleUriTemplate.expand(Map.of(STUDY_ID, studyId, SUBMITTER_ID, submitterId)).toString();
  }

  public String expandAnalysisExistenceUri(@NonNull String analysisId){
    return analysisExistenceUriTemplate.expand(Map.of(ANALYSIS_ID, analysisId)).toString();
  }

  public String expandAnalysisGenerateUri(){
    return analysisGenerateUriTemplate.expand().toString();
  }

  public String expandAnalysisSaveUri(@NonNull String analysisId){
    return analysisSaveUriTemplate.expand(Map.of(ANALYSIS_ID, analysisId)).toString();
  }

  public static UriResolver createUriResolver(@NonNull IdProperties.FederatedProperties.UriTemplateProperties uriTemplateProperties  ){
    return UriResolver.builder()
        .fileUriTemplate(processTemplate(uriTemplateProperties.getFile(), ANALYSIS_ID, FILE_NAME))
        .donorUriTemplate(processTemplate(uriTemplateProperties.getDonor(), STUDY_ID, SUBMITTER_ID))
        .specimenUriTemplate(processTemplate(uriTemplateProperties.getSpecimen(), STUDY_ID, SUBMITTER_ID))
        .sampleUriTemplate(processTemplate(uriTemplateProperties.getSample(), STUDY_ID, SUBMITTER_ID))
        .analysisExistenceUriTemplate(processTemplate(uriTemplateProperties.getAnalysis().getExistence(), ANALYSIS_ID))
        .analysisGenerateUriTemplate(processTemplate(uriTemplateProperties.getAnalysis().getGenerate()))
        .analysisSaveUriTemplate(processTemplate(uriTemplateProperties.getAnalysis().getSave(), ANALYSIS_ID))
        .build();
  }

  private static UriTemplate processTemplate(String templateString, String ... requiredTemplateVariables){
    val uriTemplate = new UriTemplate(templateString);
    val requiredSet = Set.of(requiredTemplateVariables);
    val actualSet = Set.copyOf(uriTemplate.getVariableNames());
    val missingSet = difference(requiredSet, actualSet);
    val unknownSet = difference(actualSet, requiredSet);
    checkArgument(actualSet.equals(requiredSet),
        "Error processing URI template string: '%s'. %s%s",
        templateString,
        missingSet.isEmpty() ? "" : "Missing template variables: ["+COMMA.join(missingSet)+"]. ",
        unknownSet.isEmpty() ? "" : "Unknown template variables: ["+COMMA.join(unknownSet)+"]. ");
    return uriTemplate;
  }

}
