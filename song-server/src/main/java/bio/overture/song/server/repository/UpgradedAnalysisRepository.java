package bio.overture.song.server.repository;

import bio.overture.song.server.model.analysis.AnalysisDataJoin;
import bio.overture.song.server.model.analysis.AnalysisSchemaJoin;
import bio.overture.song.server.model.analysis.AnalysisStateChangeJoin;
import bio.overture.song.server.model.analysis.DataEntity;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;

@Slf4j
@RequiredArgsConstructor
@Transactional
public class UpgradedAnalysisRepository {
  private final EntityManager em;

  /** Calls DB function 'get_analysis' to fetch analysis, files, samples, specimen, donor. */
  public List<DataEntity> getAnalysisFromDB(
      String studyId, Set<String> analysisStates, int limit, int offset) {
    val session = em.unwrap(Session.class);
    val joined = StringUtils.join(analysisStates, "\", \"");
    val states = StringUtils.wrap(joined, "\"");
    val query =
        session
            .createSQLQuery(
                "select * from get_analysis(:studyId, :analysisState, :limit, :offset);")
            .addEntity(DataEntity.class)
            .setParameter("studyId", studyId)
            .setParameter("analysisState", "{" + states + "}")
            .setParameter("limit", limit)
            .setParameter("offset", offset);
    val list = query.list();
    return list;
  }

  /** Calls GB function get_analysis_state_change to get analysis and state change join results. */
  public List<AnalysisStateChangeJoin> getAnalysisStateChange(
      String studyId, Set<String> analysisStates, int limit, int offset) {
    val session = em.unwrap(Session.class);
    val joined = StringUtils.join(analysisStates, "\", \"");
    val states = StringUtils.wrap(joined, "\"");
    val query =
        session
            .createSQLQuery(
                "select * from get_analysis_state_change(:studyId, :analysisState, :limit, :offset);")
            .addEntity(AnalysisStateChangeJoin.class)
            .setParameter("studyId", studyId)
            .setParameter("analysisState", "{" + states + "}")
            .setParameter("limit", limit)
            .setParameter("offset", offset);
    val list = query.list();
    return list;
  }

  /** Calls DB function 'get_analysis_schema' to fetch analysis schema. */
  public List<AnalysisSchemaJoin> getAnalysisSchema(
      String studyId, Set<String> analysisStates, int limit, int offset) {
    val session = em.unwrap(Session.class);
    val joined = StringUtils.join(analysisStates, "\", \"");
    val states = StringUtils.wrap(joined, "\"");
    val query =
        session
            .createSQLQuery(
                "select * from get_analysis_schema(:studyId, :analysisState, :limit, :offset);")
            .addEntity(AnalysisSchemaJoin.class)
            .setParameter("studyId", studyId)
            .setParameter("analysisState", "{" + states + "}")
            .setParameter("limit", limit)
            .setParameter("offset", offset);
    val list = query.list();
    return list;
  }

  /** Calls DB function get_analysis_data to get analysis data. */
  public List<AnalysisDataJoin> getAnalysisData(
      String studyId, Set<String> analysisStates, int limit, int offset) {
    val session = em.unwrap(Session.class);
    val joined = StringUtils.join(analysisStates, "\", \"");
    val states = StringUtils.wrap(joined, "\"");
    val query =
        session
            .createSQLQuery(
                "select * from get_analysis_data(:studyId, :analysisState, :limit, :offset);")
            .addEntity(AnalysisDataJoin.class)
            .setParameter("studyId", studyId)
            .setParameter("analysisState", "{" + states + "}")
            .setParameter("limit", limit)
            .setParameter("offset", offset);
    val list = query.list();
    return list;
  }

  public BigInteger getTotalAnalysisCount(String studyId, Set<String> analysisStates) {
    val session = em.unwrap(Session.class);
    val joined = StringUtils.join(analysisStates, "\", \"");
    val states = StringUtils.wrap(joined, "\"");
    val query =
        session
            .createSQLQuery(
                "select count(*) from analysis where study_id = :studyId and state = ANY(:analysisStates);")
            .setParameter("studyId", studyId)
            .setParameter("analysisStates", "{" + states + "}");
    val result = (BigInteger) query.uniqueResult();
    return result;
  }
}
