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

package org.icgc.dcc.song.client.benchmark.monitor;

import com.google.common.base.Stopwatch;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.client.benchmark.counter.Counter;
import org.icgc.dcc.song.client.benchmark.counter.LongCounter;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class CounterMonitor implements Counter<Long> {

  private static final long DEFAULT_INITAL_COUNT = 0;

  private final String name;

  private final Counter<Long> counter;

  private final Stopwatch watch;

  private final Logger logger;

  private final long countInterval;

  @Getter
  private boolean isRunning = false;

  private long previousCount;
  private float previousTime;

  public CounterMonitor(String name, Counter<Long> counter, Stopwatch watch, Logger logger, long countInterval) {
    this.name = name;
    this.counter = counter;
    this.watch = watch;
    this.logger = logger;
    this.countInterval = countInterval;
    // Init
    reset();
  }

  public void displaySummary() {
    log.info("[{}] SUMMARY: {}", name, toString());
  }

  @Override
  public void reset() {
    watch.reset();
    counter.reset();
    setRunningState(false);
    previousCount = counter.getCount();
    previousTime = getElapsedTimeSeconds();
  }

  public void start() {
    if (!isRunning()){
      setRunningState(true);
      watch.start();
    }
  }

  public void stop() {
    if (isRunning()){
      watch.stop();
      setRunningState(false);
    }
  }

  public <R> R callIncr(@NonNull Supplier<R> function){
    start();
    val out = function.get();
    stop();
    postIncr();
    return out;
  }

  public void runIncr(@NonNull Runnable runnable){
    start();
    runnable.run();
    stop();
    postIncr();
  }

  public float getElapsedTimeSeconds() {
    return getElapsedTimeMicro() / 1000000;
  }

  public float getElapsedTimeMili() {
    return getElapsedTimeMicro() / 1000;
  }

  public float getElapsedTimeMicro() {
    return watch.elapsed(TimeUnit.MICROSECONDS);
  }

  private void setRunningState(boolean isRunning) {
    this.isRunning = isRunning;
  }

  private void monitor() {
    if (isRunning()) {
      val currentCount = counter.getCount();
      val currentIntervalCount = currentCount - previousCount;
      if (currentIntervalCount >= countInterval) {
        val totalTime =  getElapsedTimeSeconds();
        val intervalElapsedTime = totalTime - previousTime;
        val instRate = getInstRate();
        val avgRate = getAvgRate();
        logger.info(
            "[CounterMonitor-{}] -- CountInterval: {}   Count: {}   TotalElapsedTime(s): {}   IntervalElapsedTime(s): {}   InstantaeousRate(counter/s): {}  AvgRate(counter/s): {}",
            name,
            countInterval,
            currentCount,
            totalTime,
            intervalElapsedTime,
            instRate,
            avgRate);
        previousCount = currentCount;
        previousTime = totalTime;
      }
    }
  }

  public float getAvgRate() {
    val time = getElapsedTimeSeconds();
    return (float)counter.getCount()/time;
  }

  public long getInstCount(){
    return counter.getCount()-previousCount;
  }

  public float getInstRate() {
    val currentIntervalCount = getInstCount();
    val intervalElapsedTime = getElapsedTimeSeconds() - previousTime;
    return intervalElapsedTime == 0 ? 0 : (float)currentIntervalCount / intervalElapsedTime;
  }

  @Override
  public Long preIncr() {
    counter.preIncr();
    monitor();
    return counter.getCount();
  }

  @Override
  public Long preIncr(Long amount) {
    counter.preIncr(amount);
    monitor();
    return counter.getCount();
  }

  @Override
  public Long getCount() {
    return counter.getCount();
  }

  @Override
  public String toString() {
    val currentCount = counter.getCount();
    val instCount = getInstCount();
    val totalTime = getElapsedTimeSeconds();
    val intervalElapsedTime = totalTime - previousTime;
    val instRate = getInstRate();
    val avgRate = getAvgRate();
    return String.format(
        "[CounterMonitor-%s] -- CountInterval: %s   Count: %s   InstCount: %s  TotalElapsedTime(s): %s   IntervalElapsedTime(s): %s   InstRate(counter/sec): %s  AvgRate(counter/sec): %s",
        name,
        countInterval,
        currentCount,
        instCount,
        totalTime,
        intervalElapsedTime,
        instRate,
        avgRate);
  }

  @Override
  public Long postIncr() {
    val post = counter.getCount();
    counter.preIncr();
    return post;
  }

  @Override
  public Long postIncr(Long amount) {
    val post = counter.getCount();
    counter.postIncr(amount);
    return post;
  }

  public static CounterMonitor createCounterMonitor(String name, Logger logger, long intervalCount, long initCount) {
    return new CounterMonitor(name, LongCounter.createLongCounter(initCount), Stopwatch.createUnstarted(), logger,
        intervalCount);
  }

  public static CounterMonitor createCounterMonitor(String name, Logger logger, int intervalCount) {
    return createCounterMonitor(name, logger, intervalCount, DEFAULT_INITAL_COUNT);
  }

  public static CounterMonitor createCounterMonitor(String name, long intervalCount, long initCount) {
    return createCounterMonitor(name, log, intervalCount, initCount);
  }

  public static CounterMonitor createCounterMonitor(String name, int intervalCount) {
    return createCounterMonitor(name, log, intervalCount, DEFAULT_INITAL_COUNT);
  }

}
