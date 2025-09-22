package org.roaringbitmap.art;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 1000)
@Measurement(iterations = 10, timeUnit = TimeUnit.MILLISECONDS, time = 2000)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(
    value = 1,
    jvmArgsPrepend = {
      "-XX:-TieredCompilation",
      "-XX:+UseSerialGC",
      "-mx2G",
      "-ms2G",
      "-XX:+AlwaysPreTouch"
    })
public class Node4GetChildKeyBenchmark {
  Node4 node4 = new Node4(0);
  int pos = 2;

  @Benchmark
  public void getChildKey(Blackhole blackhole) {
    blackhole.consume(node4.getChildKey(pos));
  }

  @Benchmark
  public void getChildKeyOriginal(Blackhole blackhole) {
    blackhole.consume(getChildKeyOriginal(pos));
  }

  public byte getChildKeyOriginal(int pos) {
    int shiftLeftLen = (3 - pos) * 8;
    byte v = (byte) (node4.key >> shiftLeftLen);
    return v;
  }
}
