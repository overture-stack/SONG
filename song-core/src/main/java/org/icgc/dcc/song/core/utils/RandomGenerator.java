package org.icgc.dcc.song.core.utils;

import com.fasterxml.uuid.impl.RandomBasedGenerator;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.fasterxml.uuid.Generators.randomBasedGenerator;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.MAX_VALUE;
import static java.util.stream.Collectors.toList;

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

  public String generateRandomAsciiString(int numCharacters){
    log.info("Generating RandomAsciiString for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id, seed, ++callCount);
    val total = STRING_FOR_GENERATION.length();
    val sb = new StringBuilder();
    for (int i=0; i<numCharacters; i++){
      val pos = random.nextInt(total);
      sb.append(STRING_FOR_GENERATION.charAt(pos));
    }
    return sb.toString();
  }

  public UUID generateRandomUUID(){
    log.info("Generating RandomUUID for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id, seed, ++callCount);
    return randomBasedUUIDGenerator.generate();
  }

  public String generateRandomMD5(){
    log.info("Generating RandomMD5 for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id, seed, ++callCount);
    return Hashing.md5().hashBytes(generateRandomUUID().toString().getBytes()).toString();
  }

  public int generateRandomInt(){
    log.info("Generating RandomInt for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id, seed, ++callCount);
    return random.nextInt();
  }

  public int generateRandomIntRange(int min, int max){
    log.info("Generating RandomIntRange for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id, seed, callCount);
    checkArgument(min<max,"The min(%s) must be LESS THAN max(%s)", min, max);
    return generateRandomInt(min, max-min);
  }

  public int generateRandomInt(int offset, int length){
    long maxPossibleValue = offset + (long)length;

    checkArgument(length > 0, "The length(%s) must be GREATER THAN 0", length);
    checkArgument( maxPossibleValue <= (long) MAX_VALUE,
        "The offset(%s) + length (%s) = %s must be less than the max integer value (%s)" ,
    offset, length, maxPossibleValue, MAX_VALUE);
    log.info("Generating RandomInt for RandomGenerator[{}] with seed '{}', callCount '{}', offset '{}' and length '{}'",
        id, seed, ++callCount, offset, length);
    return offset+random.nextInt(length);
  }

  public <E extends Enum<E>> E randomEnum(Class<E> enumClass){
    val enumList = newArrayList(EnumSet.allOf(enumClass));
    log.info("Selecting random enum for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id, seed, callCount);
    return randomElement(enumList);
  }

  public <T> T randomElement(List<T> list){
    log.info("Selecting random element for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id, seed, callCount);
    return list.get(generateRandomIntRange(0, list.size()));
  }

  public static <T> Stream<T> randomStream(Supplier<T> randomSupplier, int streamSize){
    return IntStream.range(0, streamSize).boxed().map(x -> randomSupplier.get());
  }

  public static <T> List<T> randomList(Supplier<T> randomSupplier, int size){
    return randomStream(randomSupplier, size).collect(toList());
  }

  public static RandomGenerator createRandomGenerator(String id, long seed) {
    log.debug("Creating RandomGenerator[{}] for seed '{}'", id ,seed);
    return new RandomGenerator(id, new Random(seed), seed);
  }

  public static RandomGenerator createRandomGenerator(String id) {
    val seed = System.currentTimeMillis();
    return createRandomGenerator(id, seed);
  }


}
