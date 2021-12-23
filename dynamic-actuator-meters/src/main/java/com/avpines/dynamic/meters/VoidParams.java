package com.avpines.dynamic.meters;

/**
 * Indicates that no params are needed.
 */
public final class VoidParams implements MeterParams {

  public static final VoidParams INSTANCE = new VoidParams();

  private VoidParams() {
  }

}