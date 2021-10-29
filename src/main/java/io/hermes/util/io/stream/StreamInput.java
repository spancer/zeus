/*******************************************************************************
 * Copyright 2021 spancer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package io.hermes.util.io.stream;

import io.hermes.util.ThreadLocals;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author spancer.ray
 */
public abstract class StreamInput extends InputStream {

  private static ThreadLocal<ThreadLocals.CleanableValue<byte[]>> cachedBytes =
      new ThreadLocal<ThreadLocals.CleanableValue<byte[]>>() {
        @Override
        protected ThreadLocals.CleanableValue<byte[]> initialValue() {
          return new ThreadLocals.CleanableValue<byte[]>(new byte[256]);
        }
      };

  /**
   * Reads and returns a single byte.
   */
  public abstract byte readByte() throws IOException;

  /**
   * Reads a specified number of bytes into an array at the specified offset.
   *
   * @param b      the array to read bytes into
   * @param offset the offset in the array to start storing bytes
   * @param len    the number of bytes to read
   */
  public abstract void readBytes(byte[] b, int offset, int len) throws IOException;

  public void readFully(byte[] b) throws IOException {
    readBytes(b, 0, b.length);
  }

  public short readShort() throws IOException {
    return (short) (((readByte() & 0xFF) << 8) | (readByte() & 0xFF));
  }

  /**
   * Reads four bytes and returns an int.
   */
  public int readInt() throws IOException {
    return ((readByte() & 0xFF) << 24) | ((readByte() & 0xFF) << 16) | ((readByte() & 0xFF) << 8)
        | (readByte() & 0xFF);
  }

  /**
   * Reads an int stored in variable-length format. Reads between one and five bytes. Smaller values
   * take fewer bytes. Negative numbers are not supported.
   */
  public int readVInt() throws IOException {
    byte b = readByte();
    int i = b & 0x7F;
    for (int shift = 7; (b & 0x80) != 0; shift += 7) {
      b = readByte();
      i |= (b & 0x7F) << shift;
    }
    return i;
  }

  /**
   * Reads eight bytes and returns a long.
   */
  public long readLong() throws IOException {
    return (((long) readInt()) << 32) | (readInt() & 0xFFFFFFFFL);
  }

  /**
   * Reads a long stored in variable-length format. Reads between one and nine bytes. Smaller values
   * take fewer bytes. Negative numbers are not supported.
   */
  public long readVLong() throws IOException {
    byte b = readByte();
    long i = b & 0x7F;
    for (int shift = 7; (b & 0x80) != 0; shift += 7) {
      b = readByte();
      i |= (b & 0x7FL) << shift;
    }
    return i;
  }

  /**
   * Reads a string.
   */
  public String readUTF() throws IOException {
    /*
     * int length = readVInt(); byte[] bytes = cachedBytes.get().get(); if (bytes == null || length
     * > bytes.length) { bytes = new byte[(int) (length * 1.25)]; cachedBytes.get().set(bytes); }
     * readBytes(bytes, 0, length); return new String(bytes, StandardCharsets.US_ASCII);
     */

    int charCount = readVInt();
    char[] chars = CachedStreamInput.getCharArray(charCount);
    int c, charIndex = 0;
    while (charIndex < charCount) {
      c = readByte() & 0xff;
      switch (c >> 4) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
          chars[charIndex++] = (char) c;
          break;
        case 12:
        case 13:
          chars[charIndex++] = (char) ((c & 0x1F) << 6 | readByte() & 0x3F);
          break;
        case 14:
          chars[charIndex++] =
              (char) ((c & 0x0F) << 12 | (readByte() & 0x3F) << 6 | (readByte() & 0x3F) << 0);
          break;
      }
    }
    return new String(chars, 0, charCount);
  }

  public final float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  public final double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  /**
   * Reads a boolean.
   */
  public final boolean readBoolean() throws IOException {
    byte ch = readByte();
    if (ch < 0) {
      throw new EOFException();
    }
    return (ch != 0);
  }


  /**
   * Resets the stream.
   */
  public abstract void reset() throws IOException;

  /**
   * Closes the stream to further operations.
   */
  public abstract void close() throws IOException;

  // IS

  @Override
  public int read() throws IOException {
    return readByte();
  }

  // Here, we assume that we always can read the full byte array

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    readBytes(b, off, len);
    return len;
  }
}
