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

import lombok.val;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.core.utils.RandomGenerator.randomList;
import static bio.overture.song.core.utils.RandomGenerator.randomStream;

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
    assertThat(md5).isEqualTo("953a2fb1afb52dc0ef6a95ec5cac8680");
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
    val listSize = 41;

    val randList1 = randomList(
        () -> randomGenerator1.generateRandomInt(offset, length), listSize );
    val randList2 = randomList(
        () -> randomGenerator2.generateRandomInt(offset, length), listSize );

    assertThat(randList1).containsExactlyElementsOf(randList2);

    val randList3 = randomList(
        () -> randomGenerator3.generateRandomInt(offset, length), listSize );

    assertThat(randList3).isNotEqualTo(randList1);
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
    val min = MIN_VALUE+1;
    val max = MAX_VALUE;
    runRandomIntRangeTest(0, max);
    runRandomIntRangeTest(min, 0);
    runRandomIntRangeTest(min/2, max/2);
    runRandomIntRangeTest(min, -1);
    runRandomIntRangeTest(1, max);
    assert(true);
  }

  public void runRandomIntRangeTest(int min, int max){
    val seqSize = 100;
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    assertThat(randomList(() -> randomGenerator1.generateRandomIntRange(min, max), seqSize))
        .containsExactlyElementsOf(randomList(() -> randomGenerator2.generateRandomIntRange (min, max), seqSize));

    val randomIntSequence = randomList(() -> randomGenerator3.generateRandomIntRange(min, max), seqSize);
    assertThat(randomList(() -> randomGenerator1.generateRandomIntRange(min, max), seqSize)).isNotEqualTo(randomIntSequence);
    assertThat(randomList(() -> randomGenerator2.generateRandomIntRange(min, max), seqSize)).isNotEqualTo(randomIntSequence);

    val randomInt1Sequence = randomList(() -> randomGenerator3.generateRandomIntRange(min, min+1), seqSize);
    assertThat(randomList(() -> randomGenerator1.generateRandomIntRange(min, min+1), seqSize)).containsExactlyElementsOf(randomInt1Sequence);
    assertThat(randomList(() -> randomGenerator2.generateRandomIntRange(min, min+1), seqSize)).containsExactlyElementsOf(randomInt1Sequence);

    val throwable1 = catchThrowable( () -> randomGenerator1.generateRandomIntRange(max, min));
    assertThat(throwable1)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            format("The inclusiveMin(%s) must be LESS THAN exclusiveMax(%s)", max, min));

    val throwable2 = catchThrowable( () -> randomGenerator1.generateRandomIntRange(MIN_VALUE, MAX_VALUE));
    assertThat(throwable2)
        .isInstanceOf(IllegalArgumentException.class);
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

  @Test
  public void testRandomList(){
    val seed1 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val list1 = randomList(() -> randomGenerator1.generateRandomIntRange(0, 100), 9000);
    val list2 = randomList(() -> randomGenerator2.generateRandomIntRange(0, 100), 9000);
    assertThat(list1).containsExactlyElementsOf(list2);
  }

  @Test
  public void testRandomStream(){
    val seed1 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val map1 =  randomStream(() ->
        randomGenerator1.generateRandomIntRange(1, 100), 9000).collect(groupingBy(x -> x));
    val map2 =  randomStream(() ->
        randomGenerator2.generateRandomIntRange(1, 100), 9000).collect(groupingBy(x -> x));
    assertThat(map1).containsAllEntriesOf(map2);
    assertThat(map2).containsAllEntriesOf(map1);
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

  @Test
  public void testSeed(){
    val seed1 = generateRandomSeed();
    val numCalls = 20;
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    for (int i=0; i<numCalls; i++){

      randomGenerator1.generateRandomUUID();
      randomGenerator1.generateRandomInt();
      randomGenerator1.generateRandomAsciiString(6);

      // Repeat with randomGenerator 2 which has same seed
      randomGenerator2.generateRandomUUID();
      randomGenerator2.generateRandomInt();
      randomGenerator2.generateRandomAsciiString(6);
    }
    assertThat(randomGenerator1.generateRandomInt()).isEqualTo(randomGenerator2.generateRandomInt());
    assertThat(randomGenerator1.generateRandomUUIDAsString()).isEqualTo(randomGenerator2.generateRandomUUIDAsString());
    assertThat(randomGenerator1.generateRandomAsciiString(10))
        .isEqualTo(randomGenerator2.generateRandomAsciiString (10));
  }




}
