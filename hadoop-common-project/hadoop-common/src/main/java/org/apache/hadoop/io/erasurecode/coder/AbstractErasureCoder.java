/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.io.erasurecode.coder;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.erasurecode.ECSchema;
import org.apache.hadoop.io.erasurecode.rawcoder.RawErasureCoder;
import org.apache.hadoop.io.erasurecode.rawcoder.RawErasureCoderFactory;
import org.apache.hadoop.io.erasurecode.rawcoder.RawErasureDecoder;
import org.apache.hadoop.io.erasurecode.rawcoder.RawErasureEncoder;

/**
 * A common class of basic facilities to be shared by encoder and decoder
 *
 * It implements the {@link ErasureCoder} interface.
 */
public abstract class AbstractErasureCoder
    extends Configured implements ErasureCoder {

  private final int numDataUnits;
  private final int numParityUnits;

  /**
   * Create raw decoder using the factory specified by rawCoderFactoryKey
   * @param rawCoderFactoryKey
   * @return raw decoder
   */
  protected RawErasureDecoder createRawDecoder(
          String rawCoderFactoryKey, int dataUnitsCount, int parityUnitsCount) {
    RawErasureCoder rawCoder = createRawCoder(getConf(),
        rawCoderFactoryKey, false, dataUnitsCount, parityUnitsCount);
    return (RawErasureDecoder) rawCoder;
  }

  /**
   * Create raw encoder using the factory specified by rawCoderFactoryKey
   * @param rawCoderFactoryKey
   * @return raw encoder
   */
  protected RawErasureEncoder createRawEncoder(
          String rawCoderFactoryKey, int dataUnitsCount, int parityUnitsCount) {
    RawErasureCoder rawCoder = createRawCoder(getConf(),
        rawCoderFactoryKey, true, dataUnitsCount, parityUnitsCount);
    return (RawErasureEncoder) rawCoder;
  }

  /**
   * Create raw coder using specified conf and raw coder factory key.
   * @param conf
   * @param rawCoderFactoryKey
   * @param isEncoder
   * @return raw coder
   */
  public static RawErasureCoder createRawCoder(Configuration conf,
      String rawCoderFactoryKey, boolean isEncoder, int numDataUnits,
                                               int numParityUnits) {

    if (conf == null) {
      return null;
    }

    Class<? extends RawErasureCoderFactory> factClass = null;
    factClass = conf.getClass(rawCoderFactoryKey,
        factClass, RawErasureCoderFactory.class);

    if (factClass == null) {
      return null;
    }

    RawErasureCoderFactory fact;
    try {
      fact = factClass.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException("Failed to create raw coder", e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Failed to create raw coder", e);
    }

    return isEncoder ? fact.createEncoder(numDataUnits, numParityUnits) :
            fact.createDecoder(numDataUnits, numParityUnits);
  }

  public AbstractErasureCoder(int numDataUnits, int numParityUnits) {
    this.numDataUnits = numDataUnits;
    this.numParityUnits = numParityUnits;
  }

  public AbstractErasureCoder(ECSchema schema) {
      this(schema.getNumDataUnits(), schema.getNumParityUnits());
  }

  @Override
  public int getNumDataUnits() {
    return numDataUnits;
  }

  @Override
  public int getNumParityUnits() {
    return numParityUnits;
  }

  @Override
  public boolean preferDirectBuffer() {
    return false;
  }

  @Override
  public void release() {
    // Nothing to do by default
  }
}