package org.roaringbitmap;

import org.roaringbitmap.RoaringBitmap.ValidationCode;

public class ValidationResult {

  private final RoaringBitmap.ValidationCode code;
  private final Container.ValidationCode subCode;
  private final Object[] params;

  private ValidationResult(RoaringBitmap.ValidationCode code, Object... params) {
    this.code = code;
    this.subCode = Container.ValidationCode.BITMAP_VIOLATION;
    this.params = params;
  }

  private ValidationResult(RoaringBitmap.ValidationCode code, Container.ValidationCode subCode,
                           Object... params) {
    this.code = code;
    this.subCode = subCode;
    this.params = params;
  }

  public Object[] getParams() {
    return params;
  }

  public static ValidationResult ok() {
    return new ValidationResult(RoaringBitmap.ValidationCode.OK);
  }

  /**
   * Creates invalid validation results.
   *
   * @param code reason code
   * @return violated validation
   */
  public static ValidationResult invalid(RoaringBitmap.ValidationCode code) {
    return new ValidationResult(code);
  }

  /**
   * Creates invalid validation results.
   *
   * @param code   reason code
   * @param params violation details parameters, their count expected to correspond to code,
   *               otherwise IllegalArgumentException is thrown
   * @return violated validation
   */
  public static ValidationResult invalid(RoaringBitmap.ValidationCode code, Object... params) {
    if (params.length != code.getParamsCount()) {
      throw new IllegalArgumentException("expected " + code.getParamsCount() + ", but given "
          + params.length);
    }
    return new ValidationResult(code, params);
  }

  /** Adds key as first parameter to validation results.
   *
   * @param vr validation result of container
   * @param key container key
   * @return validation result enriched by key
   */
  public static ValidationResult prependKeyToInvalidContainer(ValidationResult vr, int key) {
    if (vr.getCode() != ValidationCode.INVALID_CONTAINER) {
      throw new IllegalArgumentException("validation must be about invalid container, not: "
          + vr.getCode());
    }
    if (vr.getParams().length != vr.getCode().getParamCount()) {
      throw new IllegalArgumentException("validation already enriched");
    }
    return new ValidationResult(vr.getCode(), vr.getSubCode(), key, vr.getParams());
  }
  /**
   * Creates invalid validation results.
   *
   * @param code reason code
   * @return violated validation
   */
  public static ValidationResult invalid(Container.ValidationCode code) {
    return new ValidationResult(ValidationCode.INVALID_CONTAINER, code);
  }

  /**
   * Creates invalid validation results.
   *
   * @param code   reason code
   * @param params violation details parameters, their count expected to correspond to code,
   *               otherwise IllegalArgumentException is thrown
   * @return violated validation
   */
  public static ValidationResult invalid(Container.ValidationCode code, Object... params) {
    if (params.length != code.getParamsCount()) {
      throw new IllegalArgumentException("expected " + code.getParamsCount() + ", but given "
          + params.length);
    }
    return new ValidationResult(RoaringBitmap.ValidationCode.INVALID_CONTAINER, code, params);
  }

  public boolean isValid() {
    return code == RoaringBitmap.ValidationCode.OK;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ValidationResult{")
        .append("code=").append(code)
        .append(", ");
    for (int i = 0; i < params.length; i++) {
      sb.append(params[i]).append('=').append(params[i]).append(", ");
    }
    sb.append('}');
    return sb.toString();
  }

  public RoaringBitmap.ValidationCode getCode() {
    return code;
  }

  public Container.ValidationCode getSubCode() {
    return subCode;
  }
}
