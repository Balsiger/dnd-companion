/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Player Companion.
 *
 * The Player Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Player Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.values;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.util.Strings;

/**
 * A date in the campaign.
 */
public class CampaignDate {

  private static final int NIGHT_HOURS = 8;
  private static final int MORNING_HOURS = 6;

  private final Calendar calendar;
  private int year;
  private int month;
  private int day;
  private int hour;
  private int minute;

  public CampaignDate(Calendar calendar)
  {
    this.calendar = calendar;
    year = calendar.getYears().get(0).getNumber();
    month = 1;
  }

  public CampaignDate(Calendar calendar, int year, int month, int day, int hour, int minute) {
    this.calendar = calendar;
    this.year = year;
    this.month = month;
    this.day = day;
    this.hour = hour;
    this.minute = minute;

    normalize();
  }

  public CampaignDate nextMorning() {
    int mins = NIGHT_HOURS * calendar.getMinutesPerHour();
    int morning = calendar.getHoursPerDay() * calendar.getMinutesPerHour()
        + MORNING_HOURS * calendar.getMinutesPerHour()
        - dailyMinutes();

    int minsH = mins / 60;
    int morningH = morning / 60;
    if (mins < morning) {
      return addMinutes(morning);
    } else {
      return addMinutes(mins);
    }
  }

  public int dailyMinutes() {
    return hour * this.calendar.getMinutesPerHour() + minute;
  }

  public CampaignDate fromDate(int year, int month, int day) {
    return new CampaignDate(this.calendar, year, month, day, this.hour, this.minute);
  }

  public CampaignDate fromDate(int hours, int minutes) {
    return new CampaignDate(this.calendar, this.year, this.month, this.day, hours, minutes);
  }

  public int getYear()
  {
    return year;
  }

  public int getMonth()
  {
    return month;
  }

  public int getDay()
  {
    return day;
  }

  public int getHour()
  {
    return hour;
  }

  public int getMinute()
  {
    return minute;
  }

  public boolean isEmpty() {
    return year == 0 && month == 0 && day == 0 && hour == 0 && minute == 0;
  }

  private int daysPerMonth(int inMonth) {
    if (inMonth == 0 || inMonth >= calendar.getMonths().size())
      return 0;

    return calendar.getMonths().get(inMonth - 1).getDays();
  }

  public String getDayFormatted() {
    if (day == 0 || daysPerMonth(month) == 1)
      return "";

    return String.valueOf(day);
  }

  public String getTimeFormatted() {
    return Strings.pad(hour, 2, true) + ":" + Strings.pad(minute, 2, true);
  }

  public String getHoursFormatted() {
    return Strings.pad(hour, 2, true);
  }

  public String getMinutesFormatted() {
    return Strings.pad(minute, 2, true);
  }

  public String getMonthFormatted() {
    if (month == 0)
      return "";

    if (calendar.getMonths().size() >= month)
      return calendar.getMonths().get(month - 1).getName();

    return String.valueOf(month);
  }

  public String getYearFormatted() {
    Optional<Calendar.Year> calendarYear = calendar.getYear(year);
    if (calendarYear.isPresent() && !calendarYear.get().getName().isEmpty()) {
      return calendarYear.get().getName() + " (" + year + ")";
    }

    return String.valueOf(year);
  }

  @Override
  public String toString() {
    return (month > 0 ? getMonthFormatted() + " " : "")
        + (day > 0 && daysPerMonth(month) != 1
        ? getDayFormatted() + " " : "")
        + getYearFormatted() + " " + getTimeFormatted();
  }

  public Data.CampaignProto.Date toProto() {
    Data.CampaignProto.Date.Builder proto = Data.CampaignProto.Date.newBuilder();

    proto.setYear(year);
    proto.setMonth(month);
    proto.setDay(day);
    proto.setHour(hour);
    proto.setMinute(minute);

    return proto.build();
  }

  public static CampaignDate fromProto(Calendar calendar, Data.CampaignProto.Date proto) {
    return new CampaignDate(calendar, proto.getYear(), proto.getMonth(), proto.getDay(),
        proto.getHour(), proto.getMinute());
  }

  public CampaignDate addMinutes(int minutes) {
    return manipulate(0, 0, 0, 0, minutes);
  }

  private CampaignDate manipulate(int years, int months, int days, int hours, int minutes) {
    CampaignDate date = new CampaignDate(calendar, this.year + years, this.month + months,
        this.day + days, this.hour + hours, this.minute + minutes);
    date.normalize();

    return date;
  }

  private void normalize() {
    normalizeMinutes();
    normalizeHours();
    normalizeDays();
  }

  private void normalizeMinutes() {
    if(minute >= calendar.getMinutesPerHour())
    {
      hour += minute / calendar.getMinutesPerHour();
      minute = minute % calendar.getMinutesPerHour();
    }
    else if(minute < 0)
    {
      hour += minute / calendar.getMinutesPerHour() - 1;
      minute = calendar.getMinutesPerHour() + minute % calendar.getMinutesPerHour();
    }
  }

  private void normalizeHours() {
    if(hour >= calendar.getHoursPerDay())
    {
      day += hour / calendar.getHoursPerDay();
      hour = hour % calendar.getHoursPerDay();
    }
    else if(hour < 0)
    {
      day += hour / calendar.getHoursPerDay() - 1;
      hour = calendar.getHoursPerDay() + hour % calendar.getHoursPerDay();
    }
  }

  private void normalizeDays() {
    normalizeMonths();
    while(day > calendar.getMonth(month).getDays()) {
      day -= calendar.getMonth(month).getDays();
      month++;
      normalizeMonths();
    }

    while(day <= 0)
    {
      month--;
      normalizeMonths();
      day += calendar.getMonth(month).getDays();
    }
  }

  private void normalizeMonths() {
    if(month > calendar.getMonths().size())
    {
      year += month / calendar.getMonths().size();
      month = month % calendar.getMonths().size() + 1;

      normalizeMonths();
    }
    else if(month <= 0)
    {
      year += month / calendar.getMonths().size() - 1;
      month = calendar.getMonths().size() + month % calendar.getMonths().size();

      normalizeMonths();
    } else {
      // Avoid leap year months.
      if(calendar.getMonth(month).getLeapYears() != 0
          && year % calendar.getMonth(month).getLeapYears() != 0)
      {
        month++;

        normalizeMonths();
      }
    }
  }
}
