package org.roaringbitmap.art;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, timeUnit = TimeUnit.MILLISECONDS, time = 500)
@Measurement(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 1000)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
public class Node4GetChildPosBenchmark {

  @Param({"1", "2", "3", "4", "5"})
  byte key;

  private Node4 node4;

  @Setup
  public void prepareNode() {
    node4 = new Node4(0);
    for (byte currentKey = 1; currentKey <= 4; currentKey++) {
      node4.insert(new LeafNode(currentKey, 1), currentKey);
    }
    node4.key = 0x01020304;
  }

  @Benchmark
  public void reversedLookup(Blackhole blackhole) {
    blackhole.consume(getChildPosReversed(key));
  }

  @Benchmark
  public void original(Blackhole blackhole) {
    blackhole.consume(getChildPosOriginal(key));
  }

  @Benchmark
  public void getChildPosEnrolled(Blackhole blackhole) {
    blackhole.consume(node4.getChildPos(key));
  }

  public int getChildPosReversed(byte k) {
    int uk = Byte.toUnsignedInt(k);
    int rest = node4.key >>> ((4 - node4.count) << 3);
    int i = 1;
    while (true) {
      if ((rest & 0xFF) == uk) {
        return node4.count - i;
      }
      if (i < node4.count) {
        rest >>= 8;
        i++;
      } else {
        return BranchNode.ILLEGAL_IDX;
      }
    }
  }

  public int getChildPosOriginal(byte k) {
    for (int i = 0; i < node4.count; i++) {
      int shiftLeftLen = (3 - i) * 8;
      byte v = (byte) (node4.key >> shiftLeftLen);
      if (v == k) {
        return i;
      }
    }
    return BranchNode.ILLEGAL_IDX;
  }
}
