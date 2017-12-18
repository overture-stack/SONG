package org.icgc.dcc.song.client.benchmark.monitor;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toMap;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BenchmarkMonitor {

  @NonNull private final Map<String, StudyMonitor> map;

  public StudyMonitor getStudyMonitor(@NonNull String studyId){
    checkArgument(map.containsKey(studyId), "the study '%s' DNE", studyId);
    return map.get(studyId);
  }

  public Set<String> getStudies(){
    return map.keySet();
  }

  public List<CounterMonitor> getUploadMonitors(){
    return getMonitors(StudyMonitor::getUploadMonitor);
  }

  public List<CounterMonitor> getStatusMonitors(){
    return getMonitors(StudyMonitor::getStatusMonitor);
  }

  public List<CounterMonitor> getSaveMonitors(){
    return getMonitors(StudyMonitor::getSaveMonitor);
  }

  private List<CounterMonitor> getMonitors(Function<StudyMonitor,CounterMonitor> function){
    return map.entrySet().stream()
        .map(Map.Entry::getValue)
        .map(function)
        .collect(toImmutableList());
  }

  public static BenchmarkMonitor createBenchmarkMonitor(Iterable<String> studies) {
    val map = newHashSet(studies).stream()
        .map(StudyMonitor::createStudyMonitor)
        .collect(
            toMap(StudyMonitor::getStudyId, Function.identity()));
    return new BenchmarkMonitor(map);
  }

}
