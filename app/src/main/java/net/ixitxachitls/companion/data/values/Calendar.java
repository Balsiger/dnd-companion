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

import android.support.annotation.VisibleForTesting;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A calendarAction representation of a world.
 */
public class Calendar {

  private static final int NIGHT_HOURS = 8;
  private static final int MORNING_HOURS = 6;
  private final List<Year> years;
  private final List<Month> months;
  private final int daysPerWeek;
  private final int hoursPerDay;
  private final int minutesPerHour;
  private final int secondsPerMinute;
  @VisibleForTesting
  public Calendar(List<Year> years, List<Month> months, int daysPerWeek, int hoursPerDay,
                   int minutesPerHour, int secondsPerMinute) {
    this.years = years;
    this.months = months;
    this.daysPerWeek = daysPerWeek;
    this.hoursPerDay = hoursPerDay;
    this.minutesPerHour = minutesPerHour;
    this.secondsPerMinute = secondsPerMinute;
  }

  public int getDaysPerWeek() {
    return daysPerWeek;
  }

  public int getHoursPerDay() {
    return hoursPerDay;
  }

  public int getMinutesPerHour() {
    return minutesPerHour;
  }

  public List<Month> getMonths()
  {
    return Collections.unmodifiableList(months);
  }

  public int getSecondsPerMinute() {
    return secondsPerMinute;
  }

  public List<Year> getYears()
  {
    return Collections.unmodifiableList(years);
  }

  public CampaignDate add(CampaignDate date, Duration duration) {
    return manipulateDate(date, duration.getYears(), 0, duration.getDays(), duration.getHours(),
        duration.getMinutes());
  }

  public CampaignDate addMinutes(CampaignDate date, int minutes) {
    return manipulateDate(date, 0, 0, 0, 0, minutes);
  }

  public Duration dateDifference(CampaignDate current, CampaignDate next) {
    int minutes = minutes(next) - minutes(current);

    int years = minutes / (daysPerYear() * hoursPerDay * minutesPerHour);
    minutes -= years * (daysPerYear() * hoursPerDay * minutesPerHour);

    int days = minutes / (hoursPerDay * minutesPerHour);
    minutes -= days * (hoursPerDay * minutesPerHour);

    int hours = minutes / minutesPerHour;
    minutes -= hours * minutesPerHour;

    return Duration.time(years, days, hours, minutes);
  }

  public int daysPerMonth(int inMonth) {
    if (inMonth == 0 || inMonth >= getMonths().size())
      return 0;

    return getMonths().get(inMonth - 1).getDays();
  }

  // TODO(merlin): Does not handle leap years!
  public int daysPerYear() {
    int days = 0;
    for (int month = 0; month < months.size(); month++) {
      days += months.get(month).days;
    }

    return days;
  }

  public int daysWithinYear(CampaignDate date) {
    int days = date.getDay() - 1;
    for (int i = 1; i < date.getMonth(); i++) {
      days += getMonth(i).days;
    }

    return days;
  }

  public String format(CampaignDate date) {
    return (date.getMonth() > 0 ? formatMonth(date) + " " : "")
        + (date.getDay() > 0 && daysPerMonth(date.getMonth()) != 1
           ? formatDay(date) + " " : "")
        + ", "
        + formatYear(date) + " " + formatTime(date);
  }

  public String formatDay(CampaignDate date) {
    if (date.getDay() == 0 || daysPerMonth(date.getMonth()) == 1)
      return "";

    return String.valueOf(date.getDay());
  }

  public String formatMonth(CampaignDate date) {
    if (date.getMonth() == 0)
      return "";

    if (getMonths().size() >= date.getMonth())
      return getMonths().get(date.getMonth() - 1).getName();

    return String.valueOf(date.getMonth());
  }

  public String formatTime(CampaignDate date) {
    return Strings.pad(date.getHour(), 2, true) + ":" + Strings.pad(date.getMinute(), 2, true);
  }

  public String formatYear(CampaignDate date) {
    Optional<Calendar.Year> calendarYear = getYear(date.getYear());
    if (calendarYear.isPresent() && !calendarYear.get().getName().isEmpty()) {
      return calendarYear.get().getName() + " (" + date.getYear() + ")";
    }

    return String.valueOf(date.getYear());
  }

  public Month getMonth(int inMonth)
  {
    if (inMonth <= 0 || inMonth > months.size())
      Status.toast("invalid month given: " + inMonth + ", must be 1 based.");

    return months.get(inMonth - 1);
  }

  public Optional<Year> getYear(int number) {
    for (Year year : years) {
      if (year.getNumber() == number) {
        return Optional.of(year);
      }
    }

    return Optional.empty();
  }

  // TODO(merlin): Does not handle leap years.
  public int minutes(CampaignDate date) {
    return minutesWithinYear(date) +
        (date.getYear() - 1) * daysPerYear() * minutesPerHour * hoursPerDay;
  }

  public int minutesWithinYear(CampaignDate date) {
    int minutes = date.getMinute();
    minutes += date.getHour() * minutesPerHour;
    minutes += daysWithinYear(date) * minutesPerHour * hoursPerDay;

    return minutes;
  }

  public CampaignDate nextMorning(CampaignDate date) {
    int mins = NIGHT_HOURS * getMinutesPerHour();
    int morning = getHoursPerDay() * getMinutesPerHour()
        + MORNING_HOURS * getMinutesPerHour()
        - dailyMinutes(date.getHour(), date.getMinute());

    if (mins < morning) {
      return addMinutes(date, morning);
    } else {
      return addMinutes(date, mins);
    }
  }

  public CampaignDate normalize(CampaignDate date) {
    int minute = date.getMinute();
    int hour = date.getHour();
    int day = date.getDay();
    int month = date.getMonth();
    int year = date.getYear();

    // Normalize minutes.
    if(minute >= getMinutesPerHour())
    {
      hour += minute / getMinutesPerHour();
      minute = minute % getMinutesPerHour();
    }
    else if(minute < 0)
    {
      hour += minute / getMinutesPerHour() - 1;
      minute = getMinutesPerHour() + minute % getMinutesPerHour();
    }

    // Normalize hours.
    if(hour >= getHoursPerDay())
    {
      day += hour / getHoursPerDay();
      hour = hour % getHoursPerDay();
    }
    else if(hour < 0)
    {
      day += hour / getHoursPerDay() - 1;
      hour = getHoursPerDay() + hour % getHoursPerDay();
    }

    // Normalize months.
    YearMonth yearMonth = normalizeMonths(year, month);
    year = yearMonth.year;
    month = yearMonth.month;

    // Normalize days.
    while(day > getMonth(month).getDays()) {
      day -= getMonth(month).getDays();
      month++;
      yearMonth = normalizeMonths(year, month);
      year = yearMonth.year;
      month = yearMonth.month;
    }

    while(day <= 0)
    {
      month--;
      yearMonth = normalizeMonths(year, month);
      year = yearMonth.year;
      month = yearMonth.month;
      day += getMonth(month).getDays();
    }

    return new CampaignDate(year, month, day, hour, minute);
  }

  public Value.CalendarProto toProto()
  {
    Value.CalendarProto.Builder proto = Value.CalendarProto.newBuilder();

    Collections.sort(years);

    for(Year year : years)
      proto.addYear(Value.CalendarProto.Year.newBuilder()
          .setStart(year.number)
          .setName(year.name)
          .build());
    for(Month month : months)
      proto.addMonth(Value.CalendarProto.Month.newBuilder()
          .setName(month.name)
          .setDays(month.days)
          .setLeapYears(month.leapYears)
          .build());

    proto.setDaysPerWeek(daysPerWeek);
    proto.setHoursPerDay(hoursPerDay);
    proto.setMinutesPerHour(minutesPerHour);
    proto.setSecondsPerMinute(secondsPerMinute);

    return proto.build();
  }

  private int dailyMinutes(int hour, int minute) {
    return hour * getMinutesPerHour() + minute;
  }

  private CampaignDate manipulateDate(CampaignDate date, int years, int months, int days,
                                      int hours, int minutes) {
    CampaignDate manipulated = new CampaignDate(date.getYear() + years, date.getMonth() + months,
        date.getDay() + days, date.getHour() + hours, date.getMinute() + minutes);
    return normalize(manipulated);
  }

  private YearMonth normalizeMonths(int year, int month) {
    if(month > getMonths().size())
    {
      year += month / getMonths().size();
      month = month % getMonths().size() + 1;

      return normalizeMonths(year, month);
    }
    else if(month <= 0)
    {
      year += month / getMonths().size() - 1;
      month = getMonths().size() + month % getMonths().size();

      normalizeMonths(year, month);
    } else {
      // Avoid leap year months.
      if(getMonth(month).getLeapYears() != 0 && year % getMonth(month).getLeapYears() != 0)
      {
        month++;

        return normalizeMonths(year, month);
      }
    }

    return new YearMonth(year, month);
  }

  public static Calendar fromProto(Value.CalendarProto proto)
  {
    List<Year> years = new ArrayList<>();
    for(Value.CalendarProto.Year year : proto.getYearList())
      years.add(new Year(year.getStart(), year.getName()));

    List<Month> months = new ArrayList<>();
    for(Value.CalendarProto.Month month : proto.getMonthList())
      months.add(new Month(month.getName(), month.getDays(),
          month.getLeapYears()));

    return new Calendar(years, months, proto.getDaysPerWeek(),
        proto.getHoursPerDay(), proto.getMinutesPerHour(),
        proto.getSecondsPerMinute());
  }

  public static class Year implements Comparable<Year> {
    private final int number;
    private final String name;

    public Year(int number, String name)
    {
      this.number = number;
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public int getNumber() {
      return number;
    }

    @Override
    public int compareTo(Year other) {
      int compared = Integer.compare(this.number, other.number);
      if (compared != 0) {
        return compared;
      }

      return name.compareTo(other.name);
    }

    @Override
    public String toString()
    {
      return number + " " + name;
    }
  }

  public static class Month
  {
    private final String name;
    private final int days;
    private final int leapYears;

    public Month(String name, int days)
    {
      this(name, days, 0);
    }

    public Month(String name, int days, int leapYears)
    {
      this.name = name;
      this.days = days;
      this.leapYears = leapYears;
    }

    public int getDays()
    {
      return days;
    }

    public int getLeapYears()
    {
      return leapYears;
    }

    public String getName()
    {
      return name;
    }

    @Override
    public String toString()
    {
      return name
          + " ("
          + days + " "
          + (days == 1 ? "day" : "days")
          + (leapYears == 0 ? "" : " every " + leapYears + " years")
          + ")";
    }
  }  @Override
  public String toString()
  {
    return years + "; "
        + months + "; "
        + daysPerWeek + " days per week; "
        + hoursPerDay + " hours per day; "
        + minutesPerHour + " minutes per hour; "
        + secondsPerMinute + " seconds per minute";
  }

  private class YearMonth {
    private final int year;
    private final int month;

    private YearMonth(int year, int month) {
      this.year = year;
      this.month = month;
    }
  }


}
