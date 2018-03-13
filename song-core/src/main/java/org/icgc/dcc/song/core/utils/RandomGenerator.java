package org.icgc.dcc.song.core.utils;

import com.fasterxml.uuid.impl.RandomBasedGenerator;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Random;
import java.util.UUID;

import static com.fasterxml.uuid.Generators.randomBasedGenerator;

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

  public int generateRandomInt(){
    log.info("Generating RandomInt for RandomGenerator[{}] with seed '{}' and callCount '{}'",
        id, seed, ++callCount);
    return random.nextInt();
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
