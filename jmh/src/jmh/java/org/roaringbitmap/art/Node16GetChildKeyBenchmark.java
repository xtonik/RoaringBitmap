package org.roaringbitmap.art;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.roaringbitmap.longlong.LongUtils;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 100)
@Measurement(iterations = 10, timeUnit = TimeUnit.MILLISECONDS, time = 200)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(
    value = 1,
    jvmArgsPrepend = {
        "-XX:-TieredCompilation",
        "-XX:+UseSerialGC", // "-XX:+UseParallelGC",
        "-mx2G",
        "-ms2G",
        "-XX:+AlwaysPreTouch"
    })
public class Node16GetChildKeyBenchmark {

  Node16 node16 = new Node16(0);

  @Param({"4"})
  public byte pos;

  @Setup
  public void prepareNode() {
    for (int i = 0; i < 10; i++) {
      Node child = new LeafNode(0, 0);
      node16.insert(child, (byte) i);
    }
  }
  @Benchmark
  public void getChildKeyOriginal(Blackhole blackhole) {
    blackhole.consume(getChildKeyOriginal(pos));
  }

  @Benchmark
  public void getChildKey(Blackhole blackhole) {
    blackhole.consume(node16.getChildKey(pos));
  }

  public int getChildKeyOriginal(byte k) {
    int posInLong;
    if (pos <= 7) {
      posInLong = pos;
      byte[] firstBytes = LongUtils.toBDBytes(node16.firstV);
      return firstBytes[posInLong];
    } else {
      posInLong = pos - 8;
      byte[] secondBytes = LongUtils.toBDBytes(node16.secondV);
      return secondBytes[posInLong];
    }
  }
}
