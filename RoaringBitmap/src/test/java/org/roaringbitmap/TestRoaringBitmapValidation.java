package org.roaringbitmap;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validation testing of the roaring bitmaps.
 */
@SuppressWarnings({"static-method"})
@Execution(ExecutionMode.CONCURRENT)
public class TestRoaringBitmapValidation {

  @ParameterizedTest
  @MethodSource("testCases")
  public void validate(ValidationTestCase testCase) {
    RoaringBitmap bitmap = testCase.getBitmap();
    ValidationResult vr = bitmap.validate();
    assertEquals(testCase.getCode(), vr.getCode());
    assertEquals(testCase.getSubCode(), vr.getSubCode());
    // TODO more asserts on param values
  }

  private static final ValidationTestCase[] testedSet
      = ValidationTestCase.values(); // all
      // = new ValidationTestCase[]{ValidationTestCase.RUN_OVERFLOW}; // for debugging purposes
  private static Stream<Arguments> testCases() {
    return Stream.of(testedSet).map(Arguments::of);
  }

  public enum ValidationTestCase {
    EMPTY(RoaringBitmap.ValidationCode.OK),
    NULL_CONTAINERS(RoaringBitmap.ValidationCode.NULL_CONTAINERS),
    NEGATIVE_SIZE(RoaringBitmap.ValidationCode.NEGATIVE_SIZE),
    MORE_CONTAINERS_THAN_SPACE(RoaringBitmap.ValidationCode.MORE_CONTAINERS_THAN_SPACE),
    NON_INCREASING_KEYS(RoaringBitmap.ValidationCode.NON_INCREASING_KEYS),
    NULL_KEYS(RoaringBitmap.ValidationCode.NULL_KEYS),
    NULL_CONTAINER(RoaringBitmap.ValidationCode.NULL_CONTAINER),
    NULL_CONTAINER_VALUES(RoaringBitmap.ValidationCode.NULL_CONTAINER_VALUES),

    CAPACITY_LESS_THAN_RUN_COUNT(Container.ValidationCode.CAPACITY_LESS_THAN_RUN_COUNT),
    UNKNOWN_CONTAINER_TYPE(Container.ValidationCode.UNKNOWN_CONTAINER_TYPE),
    NEGATIVE_CARDINALITY(Container.ValidationCode.NEGATIVE_CARDINALITY),
    NULL_CONTENT(Container.ValidationCode.NULL_CONTENT),
    CARDINALITY_EXCEEDS_CAPACITY(Container.ValidationCode.CARDINALITY_EXCEEDS_CAPACITY),
    NON_INCREASING_VALUES(Container.ValidationCode.NON_INCREASING_VALUES),
    NULL_BITMAP(Container.ValidationCode.NULL_BITMAP),
    INVALID_BITMAP_LENGTH(Container.ValidationCode.INVALID_BITMAP_LENGTH),
    INVALID_CARDINALITY(Container.ValidationCode.INVALID_CARDINALITY),
    NEGATIVE_RUN_COUNT(Container.ValidationCode.NEGATIVE_RUN_COUNT),
    NULL_VALUES_LENGTH(Container.ValidationCode.NULL_VALUES_LENGTH),
    RUN_OVERLAP(Container.ValidationCode.RUN_OVERLAP),
    RUN_OVERFLOW(Container.ValidationCode.RUN_OVERFLOW),
    RUNS_NOT_MERGED(Container.ValidationCode.RUNS_NOT_MERGED),
    TOO_BIG_CARDINALITY(Container.ValidationCode.TOO_BIG_CARDINALITY)

    // TODO missing - the same violations thrown by other container types
    ;

    public static final int SOME_RUN_START = 1;
    public static final int SOME_RUN_END = 10_000;
    @SuppressWarnings("SpellCheckingInspection")
    public static final String VALUES_LENGTH_FIELD_NAME = "valueslength";

    public RoaringBitmap getBitmap() {
      return bitmapOf(this);
    }

    private static RoaringBitmap bitmapOf(ValidationTestCase tc) {
      // not to have to declare multiple references
      ArrayContainer ac;
      BitmapContainer bc;
      RunContainer rc;

      RoaringBitmap rb = new RoaringBitmap();
      switch (tc) {
        case EMPTY:
          break;
        case NEGATIVE_SIZE:
          rb.highLowContainer.size = -1;
          break;
        case MORE_CONTAINERS_THAN_SPACE:
          rb.highLowContainer.size = Integer.MAX_VALUE;
          break;
        case NULL_KEYS:
          rb.add(0);
          rb.highLowContainer.keys = null;
          break;
        case NULL_CONTAINERS:
          rb.highLowContainer = null;
          break;
        case NON_INCREASING_KEYS:
          rb.add(1 << 16);// key = 1
          rb.add((1 << 16) + (1 << 16));// key = 2 -> 0
          rb.highLowContainer.keys[1] = 0;
          break;
        case NULL_CONTAINER:
          rb.add(0);
          rb.highLowContainer.setContainerAtIndex(0, null);
          break;
        case NEGATIVE_CARDINALITY:
          rb.add(1);
          ac = (ArrayContainer) rb.highLowContainer.getContainerAtIndex(0);
          ac.cardinality = -1;
          break;
        case TOO_BIG_CARDINALITY:
          rb.add(1);
          ac = (ArrayContainer) rb.highLowContainer.getContainerAtIndex(0);
          ac.cardinality = ArrayContainer.DEFAULT_MAX_SIZE + 1;
          break;
        case NULL_CONTENT:
          rb.add(1);
          ac = (ArrayContainer) rb.highLowContainer.getContainerAtIndex(0);
          ac.content = null;
          break;
        case CARDINALITY_EXCEEDS_CAPACITY:
          rb.add(1);
          ac = (ArrayContainer) rb.highLowContainer.getContainerAtIndex(0);
          ac.cardinality = ArrayContainer.DEFAULT_MAX_SIZE;
          break;
        case NON_INCREASING_VALUES:
          rb.add(1);
          rb.add(2);
          ac = (ArrayContainer) rb.highLowContainer.getContainerAtIndex(0);
          ac.content[1] = 0;
          break;
        case NULL_BITMAP:
          bc = putBitmap(rb);
          setPrivateValue("bitmap", null, bc);
          break;
        case INVALID_BITMAP_LENGTH:
          bc = putBitmap(rb);
          setPrivateValue("bitmap", new long[0], bc);
          break;
        case INVALID_CARDINALITY:
          bc = putBitmap(rb);
          bc.cardinality = 42;
          break;
        case UNKNOWN_CONTAINER_TYPE:
          rb.add(0);
          rb.highLowContainer.setContainerAtIndex(0, new UnknownContainer());
          break;
        case NEGATIVE_RUN_COUNT:
          rc = putSomeRun(rb);
          rc.nbrruns = -1;
          break;
        case NULL_VALUES_LENGTH:
          rc = putSomeRun(rb);
          setPrivateValue(VALUES_LENGTH_FIELD_NAME, null, rc);
          break;
        case CAPACITY_LESS_THAN_RUN_COUNT:
          rc = putSomeRun(rb);
          rc.nbrruns = rc.getCardinality() + 1;
          break;
        case RUN_OVERFLOW:
          rc = putRun(rb, (1 << 16) - ArrayContainer.DEFAULT_MAX_SIZE - 2, (1 << 16) - 1);
          char[] valuesLength = new char[2];
          valuesLength[0] = 1 << 16 - 2;
          valuesLength[1] = (char) ((1 << 16) - 1); // ~ 1 << 16 - 1 + 2 = 1 << 16 + 1 > 1 << 16
          setPrivateValue(VALUES_LENGTH_FIELD_NAME, valuesLength, rc);
          break;
        case RUN_OVERLAP:
          putSomeRun(rb);
          rc = putRun(rb, SOME_RUN_END + 2, SOME_RUN_END + 3);
          valuesLength = new char[]{SOME_RUN_START, SOME_RUN_END, SOME_RUN_END - 2,
              SOME_RUN_END + 3};
          setPrivateValue(VALUES_LENGTH_FIELD_NAME, valuesLength, rc);
          break;
        case RUNS_NOT_MERGED:
          putSomeRun(rb);
          rc = putRun(rb, SOME_RUN_END + 2, SOME_RUN_END + 3);
          valuesLength = new char[]{SOME_RUN_START, SOME_RUN_END, SOME_RUN_END + 1, SOME_RUN_END + 3};
          setPrivateValue(VALUES_LENGTH_FIELD_NAME, valuesLength, rc);
          break;
        case NULL_CONTAINER_VALUES:
          rb.highLowContainer.values = null;
          break;
        default:
          throw new IllegalStateException("Unknown test case:" + tc);
      }
      return rb;
    }

    private static RunContainer putRun(RoaringBitmap rb, int start, int end) {
      rb.add((long)start, end);
      return (RunContainer) rb.highLowContainer.getContainerAtIndex(0);
    }

    private static RunContainer putSomeRun(RoaringBitmap rb) {
      return putRun(rb, SOME_RUN_START, SOME_RUN_END);// some valid run
    }

    private static BitmapContainer putBitmap(RoaringBitmap rb) {
      rb.add(1);
      for (int i = 0; i < ArrayContainer.DEFAULT_MAX_SIZE + 1; i++) {
        rb.add(i * 2);
      }
      rb.runOptimize();// may be not necessary
      if (!(rb.highLowContainer.getContainerAtIndex(0) instanceof BitmapContainer)) {
        throw new IllegalStateException("container is not bitmap");
      }
      return (BitmapContainer) rb.highLowContainer.getContainerAtIndex(0);
    }

    public static void setPrivateValue(String fieldName, Object value, Object instance) {
      Class<?> clazz = instance.getClass();
      try {
        Field privateField = clazz.getDeclaredField(fieldName);
        privateField.setAccessible(true);
        privateField.set(instance, value);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new RuntimeException("cannot access private field " + fieldName + " for class " + clazz);
      }
    }

    public RoaringBitmap.ValidationCode getCode() {
      return code;
    }

    public Container.ValidationCode getSubCode() {
      return subCode;
    }

    ValidationTestCase(RoaringBitmap.ValidationCode code) {
      this.code = code;
      this.subCode = Container.ValidationCode.BITMAP_VIOLATION;
    }

    ValidationTestCase(Container.ValidationCode subCode) {
      this.code = RoaringBitmap.ValidationCode.INVALID_CONTAINER;
      this.subCode = subCode;
    }

    private final RoaringBitmap.ValidationCode code;

    private final Container.ValidationCode subCode;
  }
}
