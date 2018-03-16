package org.icgc.dcc.song.core.utils;

import lombok.val;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.core.utils.RandomGenerator.randomList;

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
  public void testRandomMd5(){
    val randomGenerator1 = createRandomGenerator("rand1-seed1", 1);
    val md5 = randomGenerator1.generateRandomMD5();
    assertThat(md5).isEqualTo("bb1ad573-19b8-4cd8-a8fb-0e6f684df992");
    val randomGenerator2 = createRandomGenerator("rand1-seed1", 100);
    assertThat(randomGenerator2.generateRandomMD5()).isNotEqualTo(md5);


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

  @Test
  public void testRandomIntOffset(){
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    val offset = 100;
    val length =  31;
    assertThat(randomGenerator1.generateRandomInt(offset, length))
        .isEqualTo(randomGenerator2.generateRandomInt (offset, length));

    val randomInt = randomGenerator3.generateRandomInt(offset, length);
    assertThat(randomGenerator1.generateRandomInt(offset, length)).isNotEqualTo(randomInt);
    assertThat(randomGenerator2.generateRandomInt(offset, length)).isNotEqualTo(randomInt);
  }

  @Test
  public void testRandomIntOffsetErrors(){
    val randomGenerator1 = createRandomGenerator("rand1-seed1");
    val throwable1 = catchThrowable( () -> randomGenerator1.generateRandomInt(MAX_VALUE, 100));
    assertThat(throwable1)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            format("The offset(%s) + length (%s) = %s must be less than the max integer value (%s)" ,
                MAX_VALUE, 100, 100+ (long)MAX_VALUE, MAX_VALUE));

    val throwable2 = catchThrowable( () -> randomGenerator1.generateRandomInt(101, 0));
    assertThat(throwable2)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            format( "The length(%s) must be GREATER THAN 0", 0));

    val throwable3 = catchThrowable( () -> randomGenerator1.generateRandomInt(101, -1));
    assertThat(throwable3)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            format( "The length(%s) must be GREATER THAN 0", -1));
  }

  @Test
  public void testRandomIntRange(){
    runRandomIntRangeTest(0, 101);
    runRandomIntRangeTest(-101, 0);
    runRandomIntRangeTest(-101, 707);
    runRandomIntRangeTest(-701, -101);
    runRandomIntRangeTest(101, 701);
  }

  public void runRandomIntRangeTest(int min, int max){
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    assertThat(randomGenerator1.generateRandomIntRange(min, max)).isEqualTo(randomGenerator2.generateRandomIntRange
        (min, max));

    val randomInt = randomGenerator3.generateRandomIntRange(min, max);
    assertThat(randomGenerator1.generateRandomIntRange(min, max)).isNotEqualTo(randomInt);
    assertThat(randomGenerator2.generateRandomIntRange(min, max)).isNotEqualTo(randomInt);

    val randomInt1 = randomGenerator3.generateRandomIntRange(min, min+1);
    assertThat(randomGenerator1.generateRandomIntRange(min, min+1)).isEqualTo(randomInt1);
    assertThat(randomGenerator2.generateRandomIntRange(min, min+1)).isEqualTo(randomInt1);

    val throwable1 = catchThrowable( () -> randomGenerator1.generateRandomIntRange(max, min));
    assertThat(throwable1)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            format("The min(%s) must be LESS THAN max(%s)", max, min));
  }

  @Test
  public void testRandomElement(){
    val r = new Random();
    val intArray = r.ints(100).toArray();
    val intList = Arrays.stream(intArray).boxed().collect(toList());
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    assertThat(randomList( () -> randomGenerator1.randomElement(intList), 111))
        .containsExactlyElementsOf(randomList(() ->randomGenerator2.randomElement(intList), 111));

    val randomInt = randomList(()-> randomGenerator3.randomElement(intList), 111);
    assertThat(randomList(() -> randomGenerator1.randomElement(intList), 111)).isNotEqualTo(randomInt);
    assertThat(randomList(() ->randomGenerator2.randomElement(intList), 111)).isNotEqualTo(randomInt);
  }

  enum TestEnum{
    A,B,C,D,E,F;
  }

  @Test
  public void testRandomEnum(){
    val enumClass = TestEnum.class;

    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    assertThat(randomList(() -> randomGenerator1.randomEnum(enumClass), 100))
        .hasSameElementsAs( randomList(() -> randomGenerator2.randomEnum(enumClass), 100));

    val randomEnums = randomList(() -> randomGenerator3.randomEnum(enumClass), 102);
    assertThat(randomList(() -> randomGenerator1.randomEnum(enumClass), 102)).isNotEqualTo(randomEnums);
    assertThat(randomList(() -> randomGenerator2.randomEnum(enumClass), 102)).isNotEqualTo(randomEnums);
  }


}
