package org.icgc.dcc.song.server.utils;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Random;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@RequiredArgsConstructor(access = PRIVATE)
public class RandomGenerator {

  private static final String STRING_FOR_GENERATION = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  @NonNull private final Random random;
  @Getter private final long seed;

  public String generateRandomAsciiString(int numCharacters){
    val total = STRING_FOR_GENERATION.length();
    val sb = new StringBuilder();
    for (int i=0; i<numCharacters; i++){
      val pos = random.nextInt(total);
      sb.append(STRING_FOR_GENERATION.charAt(pos));
    }
    return sb.toString();
  }

  public int getRandomInt(){
    return random.nextInt();
  }

  public static RandomGenerator createRandomGenerator(long seed) {
    return new RandomGenerator(new Random(seed), seed);
  }

  public static RandomGenerator createRandomGenerator() {
    val seed = System.currentTimeMillis();
    log.info("Random seed for RandomGenerator: {}", seed);
    return createRandomGenerator(seed);
  }


}
