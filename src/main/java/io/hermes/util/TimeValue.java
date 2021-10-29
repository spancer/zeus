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

package io.hermes.util;

import io.hermes.HermesParseException;
import io.hermes.util.io.stream.StreamInput;
import io.hermes.util.io.stream.StreamOutput;
import io.hermes.util.io.stream.Streamable;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @author spancer.ray
 */
public class TimeValue implements Serializable, Streamable {

  static final long C0 = 1L;
  static final long C1 = C0 * 1000L;
  static final long C2 = C1 * 1000L;
  static final long C3 = C2 * 1000L;
  static final long C4 = C3 * 60L;
  static final long C5 = C4 * 60L;
  static final long C6 = C5 * 24L;
  private long duration;
  private TimeUnit timeUnit;

  private TimeValue() {

  }

  public TimeValue(long millis) {
    this(millis, TimeUnit.MILLISECONDS);
  }

  public TimeValue(long duration, TimeUnit timeUnit) {
    this.duration = duration;
    this.timeUnit = timeUnit;
  }

  public static TimeValue timeValueMillis(long millis) {
    return new TimeValue(millis, TimeUnit.MILLISECONDS);
  }

  public static TimeValue timeValueSeconds(long seconds) {
    return new TimeValue(seconds, TimeUnit.SECONDS);
  }

  public static TimeValue timeValueMinutes(long minutes) {
    return new TimeValue(minutes, TimeUnit.MINUTES);
  }

  public static TimeValue timeValueHours(long hours) {
    return new TimeValue(hours, TimeUnit.HOURS);
  }

  public static TimeValue parseTimeValue(String sValue, TimeValue defaultValue) {
    if (sValue == null) {
      return defaultValue;
    }
    try {
      long millis;
      if (sValue.endsWith("S")) {
        millis = Long.parseLong(sValue.substring(0, sValue.length() - 1));
      } else if (sValue.endsWith("ms")) {
        millis = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - "ms".length())));
      } else if (sValue.endsWith("s")) {
        millis = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * 1000);
      } else if (sValue.endsWith("m")) {
        millis = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * 60 * 1000);
      } else if (sValue.endsWith("H")) {
        millis =
            (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 1)) * 60 * 60 * 1000);
      } else {
        millis = Long.parseLong(sValue);
      }
      return new TimeValue(millis, TimeUnit.MILLISECONDS);
    } catch (NumberFormatException e) {
      throw new HermesParseException("Failed to parse [" + sValue + "]", e);
    }
  }

  public static TimeValue readTimeValue(StreamInput in) throws IOException {
    TimeValue timeValue = new TimeValue();
    timeValue.readFrom(in);
    return timeValue;
  }

  public long nanos() {
    return timeUnit.toNanos(duration);
  }

  public long micros() {
    return timeUnit.toMicros(duration);
  }

  public long millis() {
    return timeUnit.toMillis(duration);
  }

  public long seconds() {
    return timeUnit.toSeconds(duration);
  }

  public long minutes() {
    return timeUnit.toMinutes(duration);
  }

  public long hours() {
    return timeUnit.toHours(duration);
  }

  public long days() {
    return timeUnit.toDays(duration);
  }

  public double microsFrac() {
    return ((double) nanos()) / C1;
  }

  public double millisFrac() {
    return ((double) nanos()) / C2;
  }

  public double secondsFrac() {
    return ((double) nanos()) / C3;
  }

  public double minutesFrac() {
    return ((double) nanos()) / C4;
  }

  public double hoursFrac() {
    return ((double) nanos()) / C5;
  }

  public double daysFrac() {
    return ((double) nanos()) / C6;
  }

  @Override
  public String toString() {
    long nanos = nanos();
    if (nanos == 0) {
      return "0s";
    }
    double value = nanos;
    String suffix = "nanos";
    if (nanos >= C6) {
      value = daysFrac();
      suffix = "d";
    } else if (nanos >= C5) {
      value = hoursFrac();
      suffix = "h";
    } else if (nanos >= C4) {
      value = minutesFrac();
      suffix = "m";
    } else if (nanos >= C3) {
      value = secondsFrac();
      suffix = "s";
    } else if (nanos >= C2) {
      value = millisFrac();
      suffix = "ms";
    } else if (nanos >= C1) {
      value = microsFrac();
      suffix = "micros";
    }
    return Strings.format1Decimals(value, suffix);
  }

  @Override
  public void readFrom(StreamInput in) throws IOException {
    duration = in.readLong();
    timeUnit = TimeUnit.NANOSECONDS;
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {
    out.writeLong(nanos());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TimeValue timeValue = (TimeValue) o;

    if (duration != timeValue.duration) {
      return false;
    }
    return timeUnit == timeValue.timeUnit;
  }

  @Override
  public int hashCode() {
    int result = (int) (duration ^ (duration >>> 32));
    result = 31 * result + (timeUnit != null ? timeUnit.hashCode() : 0);
    return result;
  }
}
