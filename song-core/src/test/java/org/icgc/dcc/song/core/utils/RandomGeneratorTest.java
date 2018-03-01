package org.icgc.dcc.song.core.utils;

import lombok.val;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;

public class RandomGeneratorTest {

  private static final Random RANDOM = new Random();

  private static long generateRandomSeed(){
    return RANDOM.nextLong();
  }

  @Test
  public void testRandomInt(){
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    assertThat(randomGenerator1.generateRandomInt()).isEqualTo(randomGenerator2.generateRandomInt());

    val randomInt = randomGenerator3.generateRandomInt();
    assertThat(randomGenerator1.generateRandomInt()).isNotEqualTo(randomInt);
    assertThat(randomGenerator2.generateRandomInt()).isNotEqualTo(randomInt);
  }

  @Test
  public void testRandomAsciiString(){
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    val numChars = 100;
    assertThat(randomGenerator1.generateRandomAsciiString(numChars)).isEqualTo(randomGenerator2.generateRandomAsciiString(numChars));

    val randomAsciiString = randomGenerator3.generateRandomAsciiString(numChars);
    assertThat(randomGenerator1.generateRandomAsciiString(numChars)).isNotEqualTo(randomAsciiString);
    assertThat(randomGenerator2.generateRandomAsciiString(numChars)).isNotEqualTo(randomAsciiString);
  }

  @Test
  public void testRandomUUID(){
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    assertThat(randomGenerator1.generateRandomUUID()).isEqualTo(randomGenerator2.generateRandomUUID());

    val randomUUID = randomGenerator3.generateRandomUUID();
    assertThat(randomGenerator1.generateRandomUUID()).isNotEqualTo(randomUUID);
    assertThat(randomGenerator2.generateRandomUUID()).isNotEqualTo(randomUUID);
  }

}
