/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.client.benchmark.monitor;

import bio.overture.song.client.benchmark.model.Stat;
import bio.overture.song.client.benchmark.model.StatComposite;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

import static java.lang.String.format;

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
        CounterMonitor.createCounterMonitor(format("%s-upload", studyId ), 100),
        CounterMonitor.createCounterMonitor(format("%s-status", studyId ), 100),
        CounterMonitor.createCounterMonitor(format("%s-save", studyId ), 100));
  }

}
