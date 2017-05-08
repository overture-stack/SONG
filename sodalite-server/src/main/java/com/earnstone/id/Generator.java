/*
 * Copyright 2017 Corey Hulen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
 * or implied. See the License for the specific language governing permissions and limitations under 
 * the License.
 * 
 * Modifications Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 */
package com.earnstone.id;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Generator {

  public static final long twepoch = 1288834974657L;
  public static final long workerIdBits = 3L;
  public static final long dataCenterIdBits = 5L;
  public static final long maxWorkerId = -1L ^ (-1L << workerIdBits);
  public static final long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits);
  public static final long sequenceBits = 8L;

  public static final long workerIdShift = sequenceBits;
  public static final long dataCenterIdShift = sequenceBits + workerIdBits;
  public static final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
  public static final long sequenceMask = -1L ^ (-1L << sequenceBits);

  private long lastTimestamp = -1L;
  private long dataCenterId;
  private long workerId;
  private long sequence = 0L;

  public Generator(long dataCenterId, long workerId) {

    if (workerId > maxWorkerId || workerId < 0) {
      throw new IllegalArgumentException(
          String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
    }

    if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
      throw new IllegalArgumentException(
          String.format("dataCenter Id can't be greater than %d or less than 0", maxDataCenterId));
    }

    log.info("Id Generator instance running as server {} with worker {}", dataCenterId, workerId);
    this.dataCenterId = dataCenterId;
    this.workerId = workerId;
  }

  public Generator(long dataCenterId, long workerId, long sequence) {
    this(dataCenterId, workerId);
    this.sequence = sequence;
  }

  public synchronized long nextId() {

    long timestamp = System.currentTimeMillis();

    if (lastTimestamp == timestamp) {
      sequence = (sequence + 1) & sequenceMask;
      if (sequence == 0) {
        timestamp = tilNextMillis(lastTimestamp);
      }
    } else {
      sequence = 0;
    }

    if (timestamp < lastTimestamp) {
      throw new IllegalStateException(String
          .format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
    }

    lastTimestamp = timestamp;

    return ((timestamp - twepoch) << timestampLeftShift) | (dataCenterId << dataCenterIdShift)
        | (workerId << workerIdShift) | sequence;
  }

  public long getTimeStamp() {
    return lastTimestamp;
  }

  public long getDataCenterId() {
    return dataCenterId;
  }

  public long getWorkerId() {
    return workerId;
  }

  protected long tilNextMillis(long lastTimestamp) {
    long timestamp = System.currentTimeMillis();

    while (timestamp <= lastTimestamp) {
      timestamp = System.currentTimeMillis();
    }

    return timestamp;
  }
}