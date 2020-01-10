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

import net.ixitxachitls.companion.data.documents.Data;
import net.ixitxachitls.companion.data.documents.NestedDocument;

import java.util.Date;

/**
 * The history of an ingame item.
 */
public class History {

  private static String FIELD_TYPE = "type";
  private static String FIELD_SUBJECT = "subject";
  private static String FIELD_CAMPAIGN_DATE = "campaign-date";
  private static String FIELD_DATE = "date";

  public enum Type {
    UNKNOWN, CREATE, DESTROY, GIVEN, OBTAINED, DROP, SELL, USE,
  }

  public class Entry extends NestedDocument  {

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

    @Override
    public Data write() {
      return Data.empty()
          .set(FIELD_TYPE, type)
          .set(FIELD_SUBJECT, subjectId)
          .set(FIELD_CAMPAIGN_DATE, campaignDate.write())
          .set(FIELD_DATE, date.toString());
    }
  }

  public Entry readEntry(Data data) {
    return new Entry(data.get(FIELD_TYPE, Type.UNKNOWN),
        data.get(FIELD_SUBJECT, ""),
        CampaignDate.read(data.getNested(FIELD_CAMPAIGN_DATE)),
        new Date(data.get(FIELD_DATE, "")));
  }
}
