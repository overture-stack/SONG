package org.icgc.dcc.song.client.benchmark.monitor;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.icgc.dcc.song.client.benchmark.model.Stat;
import org.icgc.dcc.song.client.benchmark.model.StatComposite;

import static java.lang.String.format;
import static org.icgc.dcc.song.client.benchmark.monitor.CounterMonitor.createCounterMonitor;

@Value
public class StudyMonitor {

  @NonNull @Getter private final String studyId;
  @NonNull private final CounterMonitor uploadMonitor;
  @NonNull private final CounterMonitor statusMonitor;
  @NonNull private final CounterMonitor saveMonitor;

  public Stat getUploadStat(){
    return getStat( uploadMonitor);
  }

  public Stat getStatusStat(){
    return getStat( statusMonitor);
  }

  public Stat getSaveStat(){
    return getStat( saveMonitor);
  }

  private Stat getStat(CounterMonitor cm){
    return Stat.builder()
        .numFiles(cm.getCount())
        .totalSize(-1)
        .studyId(studyId)
        .speed(cm.getAvgRate())
        .totalTimeMs(cm.getElapsedTimeMili())
        .build();
  }

  public StatComposite getStatComposite(){
    return StatComposite.builder()
        .saveStat(getSaveStat())
        .uploadStat(getUploadStat())
        .studyId(getStudyId())
        .statusStat(getStatusStat())
        .build();
  }

  public static StudyMonitor createStudyMonitor(@NonNull String studyId) {
    return new StudyMonitor(
        studyId,
        createCounterMonitor(format("%s-upload", studyId ), 100),
        createCounterMonitor(format("%s-status", studyId ), 100),
        createCounterMonitor(format("%s-save", studyId ), 100));
  }

}
