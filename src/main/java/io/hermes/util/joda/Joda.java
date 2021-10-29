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

package io.hermes.util.joda;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author spancer.ray
 */
public class Joda {

  /**
   * Parses a joda based pattern, including some named ones (similar to the built in Joda ISO
   * ones).
   */
  public static FormatDateTimeFormatter forPattern(String input) {
    DateTimeFormatter formatter;
    if ("basicDate".equals(input) || "basic_date".equals(input)) {
      formatter = ISODateTimeFormat.basicDate();
    } else if ("basicDateTime".equals(input) || "basic_date_time".equals(input)) {
      formatter = ISODateTimeFormat.basicDateTime();
    } else if ("basicDateTimeNoMillis".equals(input) || "basic_date_time_no_millis".equals(input)) {
      formatter = ISODateTimeFormat.basicDateTimeNoMillis();
    } else if ("basicOrdinalDate".equals(input) || "basic_ordinal_date".equals(input)) {
      formatter = ISODateTimeFormat.basicOrdinalDate();
    } else if ("basicOrdinalDateTime".equals(input) || "basic_ordinal_date_time".equals(input)) {
      formatter = ISODateTimeFormat.basicOrdinalDateTime();
    } else if ("basicOrdinalDateTimeNoMillis".equals(input)
        || "basic_ordinal_date_time_no_millis".equals(input)) {
      formatter = ISODateTimeFormat.basicOrdinalDateTimeNoMillis();
    } else if ("basicTime".equals(input) || "basic_time".equals(input)) {
      formatter = ISODateTimeFormat.basicTime();
    } else if ("basicTimeNoMillis".equals(input) || "basic_time_no_millis".equals(input)) {
      formatter = ISODateTimeFormat.basicTimeNoMillis();
    } else if ("basicTTime".equals(input) || "basic_t_Time".equals(input)) {
      formatter = ISODateTimeFormat.basicTTime();
    } else if ("basicTTimeNoMillis".equals(input) || "basic_t_time_no_millis".equals(input)) {
      formatter = ISODateTimeFormat.basicTTimeNoMillis();
    } else if ("basicWeekDate".equals(input) || "basic_week_date".equals(input)) {
      formatter = ISODateTimeFormat.basicWeekDate();
    } else if ("basicWeekDateTime".equals(input) || "basic_week_date_time".equals(input)) {
      formatter = ISODateTimeFormat.basicWeekDateTime();
    } else if ("basicWeekDateTimeNoMillis".equals(input)
        || "basic_week_date_time_no_millis".equals(input)) {
      formatter = ISODateTimeFormat.basicWeekDateTimeNoMillis();
    } else if ("date".equals(input)) {
      formatter = ISODateTimeFormat.date();
    } else if ("dateHour".equals(input) || "date_hour".equals(input)) {
      formatter = ISODateTimeFormat.dateHour();
    } else if ("dateHourMinute".equals(input) || "date_hour_minute".equals(input)) {
      formatter = ISODateTimeFormat.dateHourMinute();
    } else if ("dateHourMinuteSecond".equals(input) || "date_hour_minute_second".equals(input)) {
      formatter = ISODateTimeFormat.dateHourMinuteSecond();
    } else if ("dateHourMinuteSecondFraction".equals(input)
        || "date_hour_minute_second_fraction".equals(input)) {
      formatter = ISODateTimeFormat.dateHourMinuteSecondFraction();
    } else if ("dateHourMinuteSecondMillis".equals(input)
        || "date_hour_minute_second_millis".equals(input)) {
      formatter = ISODateTimeFormat.dateHourMinuteSecondMillis();
    } else if ("dateOptionalTime".equals(input) || "date_optional_time".equals(input)) {
      // in this case, we have a separate parser and printer since the dataOptionalTimeParser can't
      // print
      return new FormatDateTimeFormatter(input,
          ISODateTimeFormat.dateOptionalTimeParser().withZone(DateTimeZone.UTC),
          ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC));
    } else if ("dateTime".equals(input) || "date_time".equals(input)) {
      formatter = ISODateTimeFormat.dateTime();
    } else if ("dateTimeNoMillis".equals(input) || "date_time_no_millis".equals(input)) {
      formatter = ISODateTimeFormat.dateTimeNoMillis();
    } else if ("hour".equals(input)) {
      formatter = ISODateTimeFormat.hour();
    } else if ("hourMinute".equals(input) || "hour_minute".equals(input)) {
      formatter = ISODateTimeFormat.hourMinute();
    } else if ("hourMinuteSecond".equals(input) || "hour_minute_second".equals(input)) {
      formatter = ISODateTimeFormat.hourMinuteSecond();
    } else if ("hourMinuteSecondFraction".equals(input)
        || "hour_minute_second_fraction".equals(input)) {
      formatter = ISODateTimeFormat.hourMinuteSecondFraction();
    } else if ("hourMinuteSecondMillis".equals(input)
        || "hour_minute_second_millis".equals(input)) {
      formatter = ISODateTimeFormat.hourMinuteSecondMillis();
    } else if ("ordinalDate".equals(input) || "ordinal_date".equals(input)) {
      formatter = ISODateTimeFormat.ordinalDate();
    } else if ("ordinalDateTime".equals(input) || "ordinal_date_time".equals(input)) {
      formatter = ISODateTimeFormat.ordinalDateTime();
    } else if ("ordinalDateTimeNoMillis".equals(input)
        || "ordinal_date_time_no_millis".equals(input)) {
      formatter = ISODateTimeFormat.ordinalDateTimeNoMillis();
    } else if ("time".equals(input)) {
      formatter = ISODateTimeFormat.time();
    } else if ("tTime".equals(input) || "t_time".equals(input)) {
      formatter = ISODateTimeFormat.tTime();
    } else if ("tTimeNoMillis".equals(input) || "t_time_no_millis".equals(input)) {
      formatter = ISODateTimeFormat.tTimeNoMillis();
    } else if ("weekDate".equals(input) || "week_date".equals(input)) {
      formatter = ISODateTimeFormat.weekDate();
    } else if ("weekDateTime".equals(input) || "week_date_time".equals(input)) {
      formatter = ISODateTimeFormat.weekDateTime();
    } else if ("weekyear".equals(input) || "week_year".equals(input)) {
      formatter = ISODateTimeFormat.weekyear();
    } else if ("weekyearWeek".equals(input)) {
      formatter = ISODateTimeFormat.weekyearWeek();
    } else if ("year".equals(input)) {
      formatter = ISODateTimeFormat.year();
    } else if ("yearMonth".equals(input) || "year_month".equals(input)) {
      formatter = ISODateTimeFormat.yearMonth();
    } else if ("yearMonthDay".equals(input) || "year_month_day".equals(input)) {
      formatter = ISODateTimeFormat.yearMonthDay();
    } else {
      formatter = DateTimeFormat.forPattern(input);
    }
    formatter.withZone(DateTimeZone.UTC);
    return new FormatDateTimeFormatter(input, formatter);
  }
}
