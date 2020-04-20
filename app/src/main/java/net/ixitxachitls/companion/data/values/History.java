/*
 * Copyright (c) 2017-2020 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Data;
import net.ixitxachitls.companion.data.documents.NestedDocument;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The history of an ingame item.
 */
public class History extends NestedDocument {

  public enum Type {
    UNKNOWN, CREATE, DESTROY, GIVEN, OBTAINED, DROP, SELL, USE,
  }

  private static String FIELD_ENTRIES = "entries";
  private static String FIELD_TYPE = "type";
  private static String FIELD_SUBJECT = "subject";
  private static String FIELD_CAMPAIGN_DATE = "campaign-date";
  private static String FIELD_DATE = "date";
  private static DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  private static DateFormat SIMPLE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private List<Entry> entries = new ArrayList<>();

  public History(List<Entry> entries) {
    this.entries.addAll(entries);
  }

  public List<Entry> getEntries() {
    return Collections.unmodifiableList(entries);
  }

  @Override
  public Data write() {
    return Data.empty()
        .setNested(FIELD_ENTRIES, entries);
  }

  public static History create(String creatorId, CampaignDate date) {
    return new History(ImmutableList.of(new Entry(Type.CREATE, creatorId, date, new Date())));
  }

  public static History read(Data data) {
    return new History(data.getNestedList(FIELD_ENTRIES).stream()
        .map(History::readEntry)
        .collect(Collectors.toList()));
  }

  public static Entry readEntry(Data data) {
    Date date;
    try {
      date = FORMAT.parse(data.get(FIELD_DATE, ""));
    } catch (ParseException e) {
      Status.exception("Cannot parse date: " + data.get(FIELD_DATE, ""), e);
      date = new Date();
    }

    return new Entry(data.get(FIELD_TYPE, Type.UNKNOWN),
        data.get(FIELD_SUBJECT, ""),
        CampaignDate.read(data.getNested(FIELD_CAMPAIGN_DATE)), date);
  }

  public static class Entry extends NestedDocument {
    private Type type;
    private String subjectId;
    private CampaignDate campaignDate;
    private Date date;

    public Entry(Type type, String subjectId, CampaignDate campaignDate, Date date) {
      this.type = type;
      this.subjectId = subjectId;
      this.campaignDate = campaignDate;
      this.date = date;
    }

    public String format() {
      return campaignDate + "/" + SIMPLE.format(date) + ": " + type + " (" + subjectId + ")";
    }

    @Override
    public Data write() {
      return Data.empty()
          .set(FIELD_TYPE, type)
          .set(FIELD_SUBJECT, subjectId)
          .set(FIELD_CAMPAIGN_DATE, campaignDate.write())
          .set(FIELD_DATE, FORMAT.format(date));
    }
  }
}
