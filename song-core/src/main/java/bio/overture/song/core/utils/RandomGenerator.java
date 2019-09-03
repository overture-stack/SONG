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

package bio.overture.song.core.utils;

import static com.fasterxml.uuid.Generators.randomBasedGenerator;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

import com.fasterxml.uuid.impl.RandomBasedGenerator;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Utility for generating random integers, strings and UUIDs using seed values while recording call
 * counts to aid in replication. The underlying randomizer is the {@link Random} class and every
 * time it is called, the {@link RandomGenerator#callCount} parameter is incremented, and the value
 * is logged as well as the seed number and generator id to allow replication of specific random
 * values. Having the seed value logged allows developers to easily replicate, debug and fix failing
 * tests.
 */
@Slf4j
public class RandomGenerator {

  private static final String STRING_FOR_GENERATION = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  @Getter private final String id;
  private final Random random;
  @Getter private final long seed;
  private long callCount = 0;

  private final RandomBasedGenerator randomBasedUUIDGenerator;

  private RandomGenerator(@NonNull String id, @NonNull Random random, long seed) {
    this.id = id;
    this.random = random;
    this.seed = seed;
    this.randomBasedUUIDGenerator = randomBasedGenerator(random);
  }

  /**
   * Generate a random string with {@code numCharacters} number of ASCII characters
   *
   * @param numCharacters to generate
   */
  public String generateRandomAsciiString(int numCharacters) {
    log.info(
        "Generating RandomAsciiString for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id,
        seed,
        ++callCount);
    val total = STRING_FOR_GENERATION.length();
    val sb = new StringBuilder();
    for (int i = 0; i < numCharacters; i++) {
      val pos = random.nextInt(total);
      sb.append(STRING_FOR_GENERATION.charAt(pos));
    }
    return sb.toString();
  }

  /** Generate a random {@link UUID} */
  public UUID generateRandomUUID() {
    log.info(
        "Generating RandomUUID for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id,
        seed,
        ++callCount);
    return randomBasedUUIDGenerator.generate();
  }

  /** Generate a random {@link UUID} String */
  public String generateRandomUUIDAsString() {
    return generateRandomUUID().toString();
  }

  /** Generate a random MD5 string */
  public String generateRandomMD5() {
    log.info(
        "Generating RandomMD5 for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id,
        seed,
        ++callCount);
    return Hashing.md5().hashBytes(generateRandomUUID().toString().getBytes()).toString();
  }

  /** Generate a random integer */
  public int generateRandomInt() {
    log.info(
        "Generating RandomInt for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id,
        seed,
        ++callCount);
    return random.nextInt();
  }

  /**
   * Generate a random integer between the interval [inclusiveMin, exlusiveMax)
   *
   * @param inclusiveMin inclusive lower bound
   * @param exlusiveMax exclusive upper bound
   */
  public int generateRandomIntRange(int inclusiveMin, int exlusiveMax) {
    log.info(
        "Generating RandomIntRange for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id,
        seed,
        callCount);
    checkArgument(
        inclusiveMin < exlusiveMax,
        "The inclusiveMin(%s) must be LESS THAN exclusiveMax(%s)",
        inclusiveMin,
        exlusiveMax);
    val difference = (long) exlusiveMax - inclusiveMin;
    checkArgument(
        difference <= MAX_VALUE,
        "The difference (%s) between exclusiveMax (%s) and (%s) must not exceed the integer exclusiveMax (%s)",
        difference,
        exlusiveMax,
        inclusiveMin,
        MAX_VALUE);
    return generateRandomInt(inclusiveMin, exlusiveMax - inclusiveMin);
  }

  /**
   * Generate a random integer between the interval [offset, offset+length]
   *
   * @param offset inclusive lower bound
   * @param length number of integers to randomize
   */
  public int generateRandomInt(int offset, int length) {
    long maxPossibleValue = offset + (long) length;

    checkArgument(length > 0, "The length(%s) must be GREATER THAN 0", length);
    checkArgument(
        maxPossibleValue <= (long) MAX_VALUE,
        "The offset(%s) + length (%s) = %s must be less than the max integer value (%s)",
        offset,
        length,
        maxPossibleValue,
        MAX_VALUE);
    log.info(
        "Generating RandomInt for RandomGenerator[{}] with seed '{}', callCount '{}', offset '{}' and length '{}'",
        id,
        seed,
        ++callCount,
        offset,
        length);
    return offset + random.nextInt(length);
  }

  /**
   * Select a random Enum
   *
   * @param enumClass Enumeration class to use
   */
  public <E extends Enum<E>> E randomEnum(Class<E> enumClass) {
    val enumList = newArrayList(EnumSet.allOf(enumClass));
    log.info(
        "Selecting random enum for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id,
        seed,
        callCount);
    return randomElement(enumList);
  }

  /**
   * Creates a new shuffled list. Assumes the input list is immutable
   *
   * @param list input List
   * @return shuffles list
   */
  public <T> List<T> shuffleList(List<T> list) {
    log.info(
        "Shuffling list for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id,
        seed,
        callCount++);
    val mutableList = newArrayList(list);
    shuffle(mutableList, random);
    return ImmutableList.copyOf(mutableList);
  }

  /** Creates a random sublist of size {@code size}, from an input list */
  public <T> List<T> randomSublist(List<T> list, int size) {
    checkArgument(
        size <= list.size(),
        "The input sublist size (%s) is greater than the list size (%s)",
        size,
        list.size());
    val shuffledList = shuffleList(IntStream.range(0, list.size()).boxed().collect(toList()));
    return IntStream.range(0, size)
        .boxed()
        .map(shuffledList::get)
        .map(list::get)
        .collect(toImmutableList());
  }

  /**
   * Take an input list of size {@code inputSize}, and generate a random sublist of size {@code
   * outputSize} where {@code outputSize} is in the range [1, inputSize )
   */
  public <T> List<T> randomSublist(List<T> list) {
    val size = generateRandomIntRange(1, list.size());
    return randomSublist(list, size);
  }

  /**
   * Select a random element from a list
   *
   * @param list input list to select from
   */
  public <T> T randomElement(List<T> list) {
    log.info(
        "Selecting RandomElement for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id,
        seed,
        callCount);
    return list.get(generateRandomIntRange(0, list.size()));
  }

  /**
   * Call the randomSupplier streamSize amount of times and stream it
   *
   * @param randomSupplier generates the random object of type T
   * @param streamSize number of random objects to generate
   * @param <T> output object type
   */
  public static <T> Stream<T> randomStream(Supplier<T> randomSupplier, int streamSize) {
    return IntStream.range(0, streamSize).boxed().map(x -> randomSupplier.get());
  }

  /**
   * Call the randomSupplier {@code size} amount of times and put it in a list
   *
   * @param randomSupplier generates the random object of type T
   * @param size number of random objects to generate
   * @param <T> output object type
   */
  public static <T> List<T> randomList(Supplier<T> randomSupplier, int size) {
    return randomStream(randomSupplier, size).collect(toList());
  }

  /**
   * Create a {@link RandomGenerator} with an id, and specific seed value
   *
   * @param id unique name describing the generator
   * @param seed
   */
  public static RandomGenerator createRandomGenerator(String id, long seed) {
    log.debug("Creating RandomGenerator[{}] for seed '{}'", id, seed);
    return new RandomGenerator(id, new Random(seed), seed);
  }

  /**
   * Create a {@link RandomGenerator} with an id and randomly seed
   *
   * @param id unique name describing the generator
   */
  public static RandomGenerator createRandomGenerator(String id) {
    val seed = System.currentTimeMillis();
    return createRandomGenerator(id, seed);
  }
}
