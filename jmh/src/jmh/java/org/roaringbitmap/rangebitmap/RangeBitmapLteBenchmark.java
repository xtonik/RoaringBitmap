package org.roaringbitmap.rangebitmap;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.roaringbitmap.RangeBitmap;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, timeUnit = TimeUnit.MILLISECONDS, time = 1000)
@Measurement(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 1000)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
public class RangeBitmapLteBenchmark {

  RangeBitmap bitmap;

  @Setup(Level.Trial)
  public void prepareBitmap() {
    RangeBitmap.Appender appender = RangeBitmap.appender(10_000);
    // many runs to be computing cardinality of run container in computeRange() remarkable
    for (int i = 0; i < 500; i++) {
      int offset = i * 10;
      LongStream.range(offset, offset + 5).forEach(appender::add);
    }
    bitmap = appender.build();
  }

  // created to measure of computeRange() optimization benefit only
  @Benchmark
  public void lte(Blackhole blackhole) {
    blackhole.consume(bitmap.lte(10_000));
  }
}
