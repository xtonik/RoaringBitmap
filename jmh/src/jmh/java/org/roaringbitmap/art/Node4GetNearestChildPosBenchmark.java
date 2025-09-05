package org.roaringbitmap.art;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.roaringbitmap.longlong.IntegerUtil;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 500)
@Measurement(iterations = 10, timeUnit = TimeUnit.MILLISECONDS, time = 500)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
public class Node4GetNearestChildPosBenchmark {

  private static Node4 node4;

  // values in Node are 1, 3, 5, 7. Even values are between them
  @Param({
      "1_0", "1_1", "1_2",
       "2_0", "2_1", "2_2", "2_3", "2_4",
       "3_0", "3_1", "3_2", "3_3", "3_4", "3_5", "3_6",
       "4_0", "4_1", "4_2", "4_3", "4_4", "4_5", "4_6", "4_7", "4_8"
  })
  String count_k;

  public byte k;

  public int count;

  @Setup(Level.Iteration)
  public void createNode() {
    String[] split = count_k.split("_");
    count = Integer.parseInt(split[0]);
    k = Byte.parseByte(split[1]);
    node4 = new Node4(0);
    for (int i = 0; i < count; i++) {
      @SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
      LeafNode leafNode = new LeafNode(i * 2 + 1, i * 2 + 1);
      node4.insert(leafNode, (byte)i);
    }
  }

  @Benchmark
  public void getNearestChildPosCompromise(Blackhole blackhole) {
    blackhole.consume(getNearestChildPosCompromise(k));
  }

  @Benchmark
  public void getNearestChildPosBranchy(Blackhole blackhole) {
    blackhole.consume(getNearestChildPosBranchy(k));
  }

  @Benchmark
  public void getNearestChildPosOriginal(Blackhole blackhole) {
    blackhole.consume(getNearestChildPosOriginal(k));
  }

  public SearchResult getNearestChildPosOriginal(byte k) {
    byte[] firstBytes = IntegerUtil.toBDBytes(node4.key);
    return Node4.binarySearchWithResult(firstBytes, 0, count, k);
  }

  private static final SearchResult FOUND0_EQUAL = SearchResult.found(0);
  private static final SearchResult NOT_FOUND0_LESS = SearchResult.notFound(-1, 0);
  private static final SearchResult NOT_FOUND0_MORE = SearchResult.notFound(0, -1);

  public SearchResult getNearestChildPosCompromise(byte k) {
    if (count == 1) {
      int ks = node4.key >>> 24;
      return k == ks ? FOUND0_EQUAL : (k < ks ? NOT_FOUND0_LESS : NOT_FOUND0_MORE);
    }
    int uk = k & 0xFF;
    int currentByte = -1;
    int i;
    for (i = 0; i < count; i++) {
      currentByte = (node4.key >>> (24 - (i << 3))) & 0xFF;
      if (currentByte >= uk) {
        break;
      }
    }
    if (currentByte == uk) {
      return SearchResult.found(i);
    }
    return i < count
        ? SearchResult.notFound(i - 1, i) // TODO tabelize
        : SearchResult.notFound(i - 1, BranchNode.ILLEGAL_IDX);
  }

  public SearchResult getNearestChildPosBranchy(byte k) {
    switch (node4.count) {
      case 0:
        return SearchResult.notFound(-1, 0);
      case 1:
        return findByteInOne(Byte.toUnsignedInt(k));
      case 2:
        return findByteInTwo(Byte.toUnsignedInt(k));
      case 3:
        return findByteInThree(k);
      case 4:
        return findByteInFour(k);
      default:
        throw new IllegalStateException("count > 4:" + count);
    }
  }

  private SearchResult findByteInOne(int uk) {
    int key1 = node4.key >>> 24 & 0xFF;
    if (uk == key1) {
      return SearchResult.found(1);
    } else {
      int index = uk < key1 ? 1 : 2;
      return SearchResult.notFound(index - 1, index);
    }
  }

  private SearchResult findByteInTwo(int uk) {
    int key2 = node4.key >>> 16 & 0xFF;
    if (uk < key2) {
      return findByteInOne(uk);
    } else {
      return uk == key2 ? SearchResult.found(2) : SearchResult.notFound(2, 3);
    }
  }

  private SearchResult findByteInThree(byte k) {
    int key2 = node4.key >>> 16 & 0xFF;
    int uk = k & 0xFF;
    if (uk < key2) {
      return findByteInOne(uk);
    } else {
      if (uk == key2) {
        return SearchResult.found(2);
      }
      int key3 = node4.key >>> 8 & 0xFF;
      if (uk == key3) {
        return SearchResult.found(3);
      }
      int index = uk < key3 ? 2 : 3;
      return SearchResult.notFound(index - 1, index);
    }
  }

  private SearchResult findByteInFour(byte k) {
    int key3 = node4.key >>> 8 & 0xFF;
    int uk = k & 0xFF;
    if (uk < key3) {
      return findByteInTwo(uk);
    } else {
      if (uk == key3) {
        return SearchResult.found(3);
      }
      int key4 = node4.key & 0xFF;
      if (uk == key4) {
        return SearchResult.found(4);
      }
      int index = k < key4 ? 3 : 4;
      return SearchResult.notFound(index - 1, index);
    }
  }
}
