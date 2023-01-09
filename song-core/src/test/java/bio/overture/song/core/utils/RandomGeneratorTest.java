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

import static bio.overture.song.core.testing.SongErrorAssertions.assertCollectionsMatchExactly;
import static bio.overture.song.core.testing.SongErrorAssertions.assertExceptionThrownBy;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.core.utils.RandomGenerator.randomList;
import static bio.overture.song.core.utils.RandomGenerator.randomStream;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import lombok.val;
import org.junit.Test;

public class RandomGeneratorTest {

  private static final Random RANDOM = new Random();

  private static long generateRandomSeed() {
    return RANDOM.nextLong();
  }

  @Test
  public void testRandomElementIgnoring() {
    val input = IntStream.range(-1000, 1000).boxed().collect(toUnmodifiableList());
    val randomGenerator = createRandomGenerator("rand1-seed1", generateRandomSeed());
    for (val x : input) {
      val ignoreSet = new HashSet<>(input);
      ignoreSet.remove(x);
      val output = randomGenerator.randomElementIgnoring(input, ignoreSet);
      assertEquals(x, output);
    }
  }

  @Test
  public void testRandomInt() {
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    assertEquals(randomGenerator1.generateRandomInt(), randomGenerator2.generateRandomInt());

    val randomInt = randomGenerator3.generateRandomInt();
    assertNotEquals(randomGenerator1.generateRandomInt(), randomInt);
    assertNotEquals(randomGenerator2.generateRandomInt(), randomInt);
  }

  @Test
  public void testRandomMd5() {
    val randomGenerator1 = createRandomGenerator("rand1-seed1", 1);
    val md5 = randomGenerator1.generateRandomMD5();
    assertEquals(md5, "953a2fb1afb52dc0ef6a95ec5cac8680");
    val randomGenerator2 = createRandomGenerator("rand1-seed1", 100);
    assertNotEquals(randomGenerator2.generateRandomMD5(), md5);
  }

  @Test
  public void testRandomAsciiString() {
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    val numChars = 100;
    assertEquals(
        randomGenerator1.generateRandomAsciiString(numChars),
        randomGenerator2.generateRandomAsciiString(numChars));

    val randomAsciiString = randomGenerator3.generateRandomAsciiString(numChars);
    assertNotEquals(randomGenerator1.generateRandomAsciiString(numChars), randomAsciiString);
    assertNotEquals(randomGenerator2.generateRandomAsciiString(numChars), randomAsciiString);
  }

  @Test
  public void testRandomUUID() {
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    assertEquals(randomGenerator1.generateRandomUUID(), randomGenerator2.generateRandomUUID());

    val randomUUID = randomGenerator3.generateRandomUUID();
    assertNotEquals(randomGenerator1.generateRandomUUID(), randomUUID);
    assertNotEquals(randomGenerator2.generateRandomUUID(), randomUUID);
  }

  @Test
  public void testRandomIntOffset() {
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    val offset = 100;
    val length = 31;
    val listSize = 41;

    val randList1 = randomList(() -> randomGenerator1.generateRandomInt(offset, length), listSize);
    val randList2 = randomList(() -> randomGenerator2.generateRandomInt(offset, length), listSize);

    assertEquals(randList1, randList2);

    val randList3 = randomList(() -> randomGenerator3.generateRandomInt(offset, length), listSize);

    assertNotEquals(randList3, randList1);
  }

  @Test
  public void testRandomIntOffsetErrors() {
    val randomGenerator1 = createRandomGenerator("rand1-seed1");
    Runnable runnable1 = () -> randomGenerator1.generateRandomInt(MAX_VALUE, 100);
    assertExceptionThrownBy(
        format(
            "The offset(%s) + length (%s) = %s must be less than the max integer value (%s)",
            MAX_VALUE, 100, 100 + (long) MAX_VALUE, MAX_VALUE),
        IllegalArgumentException.class,
        runnable1);

    Runnable runnable2 = () -> randomGenerator1.generateRandomInt(101, 0);
    assertExceptionThrownBy(
        format("The length(%s) must be GREATER THAN 0", 0),
        IllegalArgumentException.class,
        runnable2);

    Runnable runnable3 = () -> randomGenerator1.generateRandomInt(101, -1);
    assertExceptionThrownBy(
        format("The length(%s) must be GREATER THAN 0", -1),
        IllegalArgumentException.class,
        runnable3);
  }

  @Test
  public void testRandomIntRange() {
    val min = MIN_VALUE + 1;
    val max = MAX_VALUE;
    runRandomIntRangeTest(0, max);
    runRandomIntRangeTest(min, 0);
    runRandomIntRangeTest(min / 2, max / 2);
    runRandomIntRangeTest(min, -1);
    runRandomIntRangeTest(1, max);
    assert (true);
  }

  public void runRandomIntRangeTest(int min, int max) {
    val seqSize = 100;
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    assertEquals(
        randomSizedIntList(randomGenerator1, min, max, seqSize),
        randomSizedIntList(randomGenerator2, min, max, seqSize));

    val randomIntSequence = randomSizedIntList(randomGenerator3, min, max, seqSize);
    assertNotEquals(randomSizedIntList(randomGenerator1, min, max, seqSize), randomIntSequence);
    assertNotEquals(randomSizedIntList(randomGenerator2, min, max, seqSize), randomIntSequence);

    val randomInt1Sequence = randomSizedIntList(randomGenerator3, min, min + 1, seqSize);
    assertEquals(randomSizedIntList(randomGenerator1, min, min + 1, seqSize), randomInt1Sequence);
    assertEquals(randomSizedIntList(randomGenerator2, min, min + 1, seqSize), randomInt1Sequence);

    assertExceptionThrownBy(
        format("The inclusiveMin(%s) must be LESS THAN exclusiveMax(%s)", max, min),
        IllegalArgumentException.class,
        () -> randomGenerator1.generateRandomIntRange(max, min));

    assertExceptionThrownBy(
        IllegalArgumentException.class,
        () -> randomGenerator1.generateRandomIntRange(MIN_VALUE, MAX_VALUE));
  }

  @Test
  public void testRandomElement() {
    val r = new Random();
    val intArray = r.ints(100).toArray();
    val intList = Arrays.stream(intArray).boxed().collect(toList());
    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    assertEquals(
        randomSizedElementList(randomGenerator1, intList, 111),
        randomSizedElementList(randomGenerator2, intList, 111));

    assertEquals(
        randomSizedElementList(randomGenerator1, intList, 111),
        randomSizedElementList(randomGenerator2, intList, 111));

    val randomInt = randomSizedElementList(randomGenerator3, intList, 111);
    assertNotEquals(randomSizedElementList(randomGenerator1, intList, 111), randomInt);
    assertNotEquals(randomSizedElementList(randomGenerator2, intList, 111), randomInt);
  }

  @Test
  public void testRandomList() {
    val seed1 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val list1 = randomList(() -> randomGenerator1.generateRandomIntRange(0, 100), 9000);
    val list2 = randomList(() -> randomGenerator2.generateRandomIntRange(0, 100), 9000);
    assertEquals(list1, list2);
  }

  @Test
  public void testRandomStream() {
    val seed1 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val map1 =
        randomStream(() -> randomGenerator1.generateRandomIntRange(1, 100), 9000)
            .collect(groupingBy(x -> x));
    val map2 =
        randomStream(() -> randomGenerator2.generateRandomIntRange(1, 100), 9000)
            .collect(groupingBy(x -> x));
    assertCollectionsMatchExactly(map1.entrySet(), map2.entrySet());
  }

  enum TestEnum {
    A,
    B,
    C,
    D,
    E,
    F;
  }

  @Test
  public void testRandomEnum() {
    val enumClass = TestEnum.class;

    val seed1 = generateRandomSeed();
    val seed2 = generateRandomSeed();
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    val randomGenerator3 = createRandomGenerator("rand3-seed2", seed2);
    assertCollectionsMatchExactly(
        randomSizedEnumList(randomGenerator1, enumClass, 100),
        randomSizedEnumList(randomGenerator2, enumClass, 100));

    val randomEnums = randomSizedEnumList(randomGenerator3, enumClass, 102);
    assertCollectionsMatchExactly(
        randomSizedEnumList(randomGenerator1, enumClass, 102), randomEnums);
    assertCollectionsMatchExactly(
        randomSizedEnumList(randomGenerator2, enumClass, 102), randomEnums);
  }

  @Test
  public void testSeed() {
    val seed1 = generateRandomSeed();
    val numCalls = 20;
    val randomGenerator1 = createRandomGenerator("rand1-seed1", seed1);
    val randomGenerator2 = createRandomGenerator("rand2-seed1", seed1);
    for (int i = 0; i < numCalls; i++) {

      randomGenerator1.generateRandomUUID();
      randomGenerator1.generateRandomInt();
      randomGenerator1.generateRandomAsciiString(6);

      // Repeat with randomGenerator 2 which has same seed
      randomGenerator2.generateRandomUUID();
      randomGenerator2.generateRandomInt();
      randomGenerator2.generateRandomAsciiString(6);
    }
    assertEquals(randomGenerator1.generateRandomInt(), randomGenerator2.generateRandomInt());
    assertEquals(
        randomGenerator1.generateRandomUUIDAsString(),
        randomGenerator2.generateRandomUUIDAsString());
    assertEquals(
        randomGenerator1.generateRandomAsciiString(10),
        randomGenerator2.generateRandomAsciiString(10));
  }

  private List<Integer> randomSizedIntList(RandomGenerator r, int min, int max, int size) {
    return randomList(() -> r.generateRandomIntRange(min, max), size);
  }

  private <T> List<T> randomSizedElementList(RandomGenerator r, List<T> list, int size) {
    return randomList(() -> r.randomElement(list), size);
  }

  private <E extends Enum<E>> List<E> randomSizedEnumList(
      RandomGenerator r, Class<E> enumClass, int size) {
    return randomList(() -> r.randomEnum(enumClass), size);
  }
}
