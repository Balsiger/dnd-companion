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

import net.ixitxachitls.companion.proto.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A calendar representation of a world.
 */
public class Calendar {

  public static class Year implements Comparable<Year> {
    private final int number;
    private final String name;

    public Year(int number, String name)
    {
      this.number = number;
      this.name = name;
    }

    public int getNumber() {
      return number;
    }

    public String getName() {
      return name;
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

    public String getName()
    {
      return name;
    }

    public int getDays()
    {
      return days;
    }

    public int getLeapYears()
    {
      return leapYears;
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
  }

  private final List<Year> years;
  private final List<Month> months;
  private final int daysPerWeek;
  private final int hoursPerDay;
  private final int minutesPerHour;
  private final int secondsPerMinute;

  private Calendar(List<Year> years, List<Month> months, int daysPerWeek, int hoursPerDay,
                   int minutesPerHour, int secondsPerMinute) {
    this.years = years;
    this.months = months;
    this.daysPerWeek = daysPerWeek;
    this.hoursPerDay = hoursPerDay;
    this.minutesPerHour = minutesPerHour;
    this.secondsPerMinute = secondsPerMinute;
  }


  public List<Year> getYears()
  {
    return Collections.unmodifiableList(years);
  }

  public List<Month> getMonths()
  {
    return Collections.unmodifiableList(months);
  }

  public Month getMonth(int inMonth)
  {
    if (inMonth <= 0 || inMonth > months.size())
      throw new IllegalArgumentException("invalid month given: " + inMonth
          + ", must be 1 based.");

    return months.get(inMonth - 1);
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

  public int getSecondsPerMinute() {
    return secondsPerMinute;
  }

  public Optional<Year> getYear(int number) {
    for (Year year : years) {
      if (year.getNumber() == number) {
        return Optional.of(year);
      }
    }

    return Optional.absent();
  }

  @Override
  public String toString()
  {
    return years + "; "
        + months + "; "
        + daysPerWeek + " days per week; "
        + hoursPerDay + " hours per day; "
        + minutesPerHour + " minutes per hour; "
        + secondsPerMinute + " seconds per minute";
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
}
