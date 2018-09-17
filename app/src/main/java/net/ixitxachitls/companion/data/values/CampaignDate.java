/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
 *
 * The Roleplay Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Roleplay Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 * A date in the campaign.
 */
public class CampaignDate {

  private static final String FIELD_YEAR = "year";
  private static final String FIELD_MONTH = "month";
  private static final String FIELD_DAY = "day";
  private static final String FIELD_HOUR = "hour";
  private static final String FIELD_MINUTE = "minute";

  private final int year;
  private final int month;
  private final int day;
  private final int hour;
  private final int minute;

  public CampaignDate() {
    this(0, 0, 0, 0, 0);
  }

  public CampaignDate(int year) {
    this(year, 1, 0, 0, 0);
  }

  public CampaignDate(int year, int month, int day, int hour, int minute) {
    this.year = year;
    this.month = month;
    this.day = day;
    this.hour = hour;
    this.minute = minute;
  }

  public int getYear() {
    return year;
  }

  public int getMonth() {
    return month;
  }

  public int getDay() {
    return day;
  }

  public int getHour() {
    return hour;
  }

  public int getMinute() {
    return minute;
  }

  public boolean isEmpty() {
    return year == 0 && month == 0 && day == 0 && hour == 0 && minute == 0;
  }

  public boolean before(CampaignDate date) {
    return year < date.year
        || month < date.month
        || day < date.day
        || hour < date.hour
        || minute < date.minute;
  }

  public boolean after(CampaignDate date) {
    return !equals(date) && !before(date);
  }

  public boolean equals(CampaignDate date) {
    return year == date.year
        && month == date.month
        && day == date.day
        && hour == date.hour
        && minute == date.minute;
  }

  public static CampaignDate read(Map<String, Object> data) {
    int year = (int) Values.get(data, FIELD_YEAR, 2010);
    int month = (int) Values.get(data, FIELD_MONTH, 3);
    int day = (int) Values.get(data, FIELD_DAY, 3);
    int hour = (int) Values.get(data, FIELD_HOUR, 21);
    int minute = (int) Values.get(data, FIELD_MINUTE, 42);

    return new CampaignDate(year, month, day, hour, minute);
  }

  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_YEAR, year);
    data.put(FIELD_MONTH, month);
    data.put(FIELD_DAY, day);
    data.put(FIELD_HOUR, hour);
    data.put(FIELD_MINUTE, minute);

    return data;
  }

  @Override
  public String toString() {
    return year + "-"
        + Strings.pad(month, 2, true) + "-"
        + Strings.pad(day, 2, true) + " "
        + Strings.pad(hour, 2, true) + ":"
        + Strings.pad(minute, 2, true);
  }

  public Value.DateProto toProto() {
    Value.DateProto.Builder proto = Value.DateProto.newBuilder();

    proto.setYear(year);
    proto.setMonth(month);
    proto.setDay(day);
    proto.setHour(hour);
    proto.setMinute(minute);

    return proto.build();
  }

  public static CampaignDate fromProto(Value.DateProto proto) {
    return new CampaignDate(proto.getYear(), proto.getMonth(), proto.getDay(),
        proto.getHour(), proto.getMinute());
  }
}
