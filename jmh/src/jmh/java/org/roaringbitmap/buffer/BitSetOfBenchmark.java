package org.roaringbitmap.buffer;

import org.openjdk.jmh.annotations.*;
import org.roaringbitmap.BitSetUtil;
import org.roaringbitmap.PeekableCharIterator;
import org.roaringbitmap.PeekableIntIterator;

import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, timeUnit = TimeUnit.MILLISECONDS, time = 1000)
@Measurement(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 1000)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class BitSetOfBenchmark {

  private static final int valuesCount = 10_000_000;
  private static final int maxValue = 100_000_000;

  @Param({"CONSECUTIVE", "RANDOM"})
  public String inputData;

  public ImmutableRoaringBitmap bitmap;

  @Setup
  public void setUp() {
    int[] a = new int[valuesCount];
    if (inputData.equals("RANDOM")) {
      Random r = new Random(0);
      for (int i = 0; i < valuesCount; i++) {
        a[i] = r.nextInt(maxValue);
      }
    }
    if (inputData.equals("CONSECUTIVE")) {
      for (int i = 0; i < valuesCount; i++) {
        a[i] = maxValue + i;
      }
    }
    bitmap = ImmutableRoaringBitmap.bitmapOf(a);
  }

  @Benchmark
  public BitSet[] original() {
    return BitSetUtil.bitSetOf(bitmap);
  }

  @Benchmark
  public BitSet[] bitSetOfPreallocated() {
    return bitSetOfPreallocatedImpl(bitmap);
  }

  @Benchmark
  public BitSet[] bitSetOfNegativeValuesSeparately() {
    return bitSetOfNegativeValuesSeparatelyImpl(bitmap);
  }

  @Benchmark
  public BitSet[] bitSetOfNegativeValuesSeparatelyRegisterPreviousValue() {
    return bitSetOfNegativeValuesSeparatelyRegisterPreviousValueImpl(bitmap);
  }

  @Benchmark
  public BitSet[] bitSetOfIterateThroughContainers() {
    return bitSetOfIterateThroughContainersImpl(bitmap);
  }

  public static BitSet[] bitSetOfPreallocatedImpl(ImmutableRoaringBitmap bitmap) {
    int last = bitmap.last();
    BitSet bitSetPositive = last > 0 ? new BitSet(last) : new BitSet();
    BitSet bitSetNegative = last < 0 ? new BitSet() : new BitSet(last & 0x7F_FF_FF_FF);
    PeekableIntIterator it = bitmap.getIntIterator();
    while (it.hasNext()) {
      int value = it.next();
      if (value >= 0) {
        bitSetPositive.set(value);
      } else {
        bitSetNegative.set(value & 0x7F_FF_FF_FF);
      }
    }
    return new BitSet[]{bitSetPositive, bitSetNegative};
  }

  public static BitSet[] bitSetOfNegativeValuesSeparatelyImpl(ImmutableRoaringBitmap bitmap) {
    int last = bitmap.last();
    BitSet bitSetPositive = last > 0 ? new BitSet(last) : new BitSet();
    BitSet bitSetNegative = last < 0 ? new BitSet() : new BitSet(last & 0x7F_FF_FF_FF);
    PeekableIntIterator it = bitmap.getIntIterator();

    int value = 0;
    while (value != last) {
      value = it.next();
      if (value < 0) {
        break;
      }
      bitSetPositive.set(value);
    }

    if (value < 0) {
      bitSetNegative.set(value & 0x7F_FF_FF_FF);
      while (value != last) {
        value = it.next();
        bitSetNegative.set(value & 0x7F_FF_FF_FF);
      }
    }

    return new BitSet[]{bitSetPositive, bitSetNegative};
  }

  public static BitSet[] bitSetOfNegativeValuesSeparatelyRegisterPreviousValueImpl(
      ImmutableRoaringBitmap bitmap) {
    int last = bitmap.last();
    BitSet bitSetPositive = last > 0 ? new BitSet(last) : new BitSet();
    BitSet bitSetNegative = last < 0 ? new BitSet() : new BitSet(last & 0x7F_FF_FF_FF);
    PeekableIntIterator it = bitmap.getIntIterator();

    int previous = 0, value = 0, start = -1, length = 0;
    while (value != last) {
      value = it.next();
      if (value < 0) {
        break;
      }
      if (value == previous + 1) {
        if (start == -1) {
          start = previous;
        } else {
          length++;
        }
      } else {
        if (start != -1) {
          bitSetPositive.set(start, start + length);
          start = -1;
        }
        bitSetPositive.set(value);
      }
      previous = value;
    }

    if (start != -1) {
      bitSetPositive.set(start, start + length);
    }
    if (value < 0) {
      // here we can also register previous value
      bitSetNegative.set(value & 0x7F_FF_FF_FF);
      while (value != last) {
        value = it.next();
        bitSetNegative.set(value & 0x7F_FF_FF_FF);
      }
    }

    return new BitSet[]{bitSetPositive, bitSetNegative};
  }

  public static BitSet[] bitSetOfIterateThroughContainersImpl(ImmutableRoaringBitmap bitmap) {
    // to avoid BitSet internal array resizing
    int last = bitmap.last();
    BitSet bitSetPositive = last > 0 ? new BitSet(last) : new BitSet();
    BitSet bitSetNegative = last < 0 ? new BitSet(last & 0x7F_FF_FF_FF) : new BitSet();

    MappeableContainerPointer pointer = bitmap.getContainerPointer();
    while (pointer.hasContainer()) {
      MappeableContainer container = pointer.getContainer();
      pointer.advance();
      BitSet current;
      boolean negative = false;
      int key = pointer.key() << 16;
      if (key < 0) {
        key &= 0x7F_FF_FF_FF;
        current = bitSetNegative;
        negative = true;
      } else {
        current = bitSetPositive;
      }
      if (container instanceof MappeableArrayContainer) {
        MappeableArrayContainer ac = (MappeableArrayContainer) container;
        PeekableCharIterator it = ac.getCharIterator();
        while (it.hasNext()) {
          current.set(key | it.next());
        }
      } else if (container instanceof MappeableRunContainer) {
        MappeableRunContainer rc = (MappeableRunContainer) container;
        for (int i = 0; i < rc.getCardinality(); i++) {
          char value = rc.getValue(i);
          char length = rc.getLength(i);
          current.set(key | value, key | (value + length));
        }
      } else if (container instanceof MappeableBitmapContainer) {
        MappeableBitmapContainer bc = (MappeableBitmapContainer) container;
        if (current.isEmpty()) {
          current = BitSet.valueOf(bc.toLongArray());
          if (negative) {
            bitSetNegative = current;
          } else {
            bitSetPositive = current;
          }
        } else {
          PeekableCharIterator it = bc.getCharIterator();
          while (it.hasNext()) {
            current.set(key | it.next());
          }
        }
      }
    }
    return new BitSet[]{bitSetPositive, bitSetNegative};
  }
}


