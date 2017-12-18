/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
