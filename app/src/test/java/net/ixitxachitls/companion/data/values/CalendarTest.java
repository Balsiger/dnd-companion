/*
 * Copyright (c) 2017-2018 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.values;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by balsiger on 6/14/18.
 */
public class CalendarTest {

  private static final Calendar CALENDAR =
      new Calendar(ImmutableList.of(new Calendar.Year(2018, "")),
          ImmutableList.of(
              new Calendar.Month("January", 31),
              new Calendar.Month("February", 28),
              new Calendar.Month("March", 31),
              new Calendar.Month("April", 30),
              new Calendar.Month("May", 31),
              new Calendar.Month("June", 30),
              new Calendar.Month("July", 31),
              new Calendar.Month("August", 31),
              new Calendar.Month("September", 30),
              new Calendar.Month("October", 31),
              new Calendar.Month("November", 30),
              new Calendar.Month("December", 31)), 7, 24, 60, 60);

  @Test
  public void daysWithinYear() {
    assertEquals(0, CALENDAR.daysWithinYear(new CampaignDate(2018, 1, 1, 23, 24)));
    assertEquals(4, CALENDAR.daysWithinYear(new CampaignDate(2018, 1, 5, 23, 24)));
    assertEquals(31 + 28 + 4, CALENDAR.daysWithinYear(new CampaignDate(2018, 3, 5, 23, 24)));
    assertEquals(364, CALENDAR.daysWithinYear(new CampaignDate(2018, 12, 31, 23, 24)));
  }

  @Test
  public void minutesWithinYear() {
    assertEquals(0, CALENDAR.minutesWithinYear(new CampaignDate(2018, 1, 1, 0, 0)));
    assertEquals(5, CALENDAR.minutesWithinYear(new CampaignDate(2018, 1, 1, 0, 5)));
    assertEquals(2 * 60 + 15, CALENDAR.minutesWithinYear(new CampaignDate(2018, 1, 1, 2, 15)));
    assertEquals(2 * 24 * 60 + 2 * 60 + 15,
        CALENDAR.minutesWithinYear(new CampaignDate(2018, 1, 3, 2, 15)));
    assertEquals((31 + 28) * 24 * 60 + 4 * 24 * 60 + 2 * 60 + 15,
        CALENDAR.minutesWithinYear(new CampaignDate(2018, 3, 5, 2, 15)));
  }

  @Test
  public void minutes() {
    assertEquals(2017 * 365 * 24 * 60,
        CALENDAR.minutes(new CampaignDate(2018, 1, 1, 0, 0)));
    assertEquals(1969 * 365 * 24 * 60 + 5,
        CALENDAR.minutes(new CampaignDate(1970, 1, 1, 0, 5)));
  }

  @Test
  public void dateDifference() {
    assertEquals("ending",
        CALENDAR.dateDifference(new CampaignDate(2018, 6, 14, 8, 48),
            new CampaignDate(2018, 6, 14, 8, 48)).toString());
    assertEquals("1 minute",
        CALENDAR.dateDifference(new CampaignDate(2018, 6, 14, 8, 48),
            new CampaignDate(2018, 6, 14, 8, 49)).toString());
    assertEquals("61 days 2 hours 3 minutes",
        CALENDAR.dateDifference(new CampaignDate(2018, 6, 14, 8, 48),
            new CampaignDate(2018, 8, 14, 10, 51)).toString());
    assertEquals("12 minutes",
        CALENDAR.dateDifference(new CampaignDate(2018, 12, 31, 23, 55),
            new CampaignDate(2019, 1, 1, 0, 7)).toString());
    assertEquals("3 years 1 day 2 hours 12 minutes",
        CALENDAR.dateDifference(new CampaignDate(2018, 5, 17, 23, 55),
            new CampaignDate(2021, 5, 19, 2, 7)).toString());
  }
}