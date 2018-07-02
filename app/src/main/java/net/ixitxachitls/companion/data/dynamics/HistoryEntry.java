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

package net.ixitxachitls.companion.data.dynamics;

import android.support.annotation.DrawableRes;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.values.CampaignDate;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.util.Strings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * An entry in the history of the roleplay companion.
 */
public class HistoryEntry extends StoredEntry<Entry.HistoryProto>
    implements Comparable<HistoryEntry> {

  public static final String TYPE = "history";
  public static final String TABLE = "history";

  private final DateFormat REAL_DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

  private final String realDate;
  private final CampaignDate gameDate;
  private final Type type;
  private final List<String> ids;
  private final String description;
  private boolean viewed;

  public enum Type {
    unknown, other, create, add, remove, xp, expired
  }

  public HistoryEntry(CompanionContext context, CampaignDate gameDate, Type type,
                      List<String> ids, boolean viewed) {
    this(context, gameDate, type, ids, "", viewed);
  }

  public HistoryEntry(CompanionContext context, CampaignDate gameDate, Type type, List<String> ids,
                      String description, boolean viewed) {
    this(context, 0, null, gameDate, type, ids, description, viewed);
  }

  public HistoryEntry(CompanionContext context, long id, @Nullable String realDate,
                      CampaignDate gameDate, Type type, List<String> ids, String description,
                      boolean viewed) {
    super(context, id, TYPE, TYPE + "-" + id, TYPE, true, DataBaseContentProvider.HISTORY);
    this.realDate = realDate == null ? REAL_DATE_FORMAT.format(new Date()) : realDate;
    this.gameDate = gameDate;
    this.type = type;
    this.ids = ids;
    this.description = description;
    this.viewed = viewed;
  }

  public List<String> getIds() {
    return Collections.unmodifiableList(ids);
  }

  public String buildNotificationTitle() {
    switch (type) {
      default:
        return type.toString();

      case xp:
        return "xp";

      case expired:
        return "expired";
    }
  }

  public @DrawableRes int getNotificationDrawable() {
    switch (type) {
      default:
        return R.drawable.history;

      case xp:
        return R.drawable.history_xp;

      case expired:
        return R.drawable.history_expired;
    }
  }

  @Override
  public int compareTo(HistoryEntry other) {
    return other.realDate.compareTo(this.realDate);
  }

  public boolean hasId(@Nullable String id) {
    return id == null || ids.contains(id);
  }

  public boolean isViewed() {
    return viewed;
  }

  public Type getEntryType() {
    return type;
  }

  public void markViewed() {
    viewed = true;
    store();
    context.histories().update(this);
  }

  @Override
  public boolean store() {
    if (super.store()) {
      if (context.histories().has(this)) {
        context.histories().update(this);
      } else {
        context.histories().add(this);
      }

      return true;
    }

    return false;
  }

  @Override
  public String toString() {
    return realDate + " (" + formatDate() + "): " + describe();
  }

  @Override
  public Entry.HistoryProto toProto() {
    return Entry.HistoryProto.newBuilder()
        .setRealDate(realDate)
        .setGameDate(gameDate.toProto())
        .setType(convert(type))
        .addAllEntity(ids)
        .setDescription(description)
        .setViewed(viewed)
        .build();
  }

  public static HistoryEntry fromProto(CompanionContext context, long id,
                                       Entry.HistoryProto proto) {
    return new HistoryEntry(context, id, proto.getRealDate(),
        CampaignDate.fromProto(proto.getGameDate()), convert(proto.getType()),
        proto.getEntityList(), proto.getDescription(), proto.getViewed());
  }

  private static Type convert(Entry.HistoryProto.Type type) {
    switch(type) {
      case UNKNOWN:
      case UNRECOGNIZED:
        return Type.unknown;

      case OTHER:
        return Type.other;

      case CREATE:
        return Type.create;

      case ADD:
        return Type.add;

      case REMOVE:
        return Type.remove;

      case XP:
        return Type.xp;

      case EXPIRED:
        return Type.expired;
    }

    Status.error("Uknown history type encountered: " + type);
    return Type.unknown;
  }

  private static Entry.HistoryProto.Type convert(Type type) {
    switch(type) {
      case unknown:
        return Entry.HistoryProto.Type.UNKNOWN;

      case other:
        return Entry.HistoryProto.Type.OTHER;

      case create:
        return Entry.HistoryProto.Type.CREATE;

      case add:
        return Entry.HistoryProto.Type.ADD;

      case remove:
        return Entry.HistoryProto.Type.REMOVE;

      case xp:
        return Entry.HistoryProto.Type.XP;

      case expired:
        return Entry.HistoryProto.Type.EXPIRED;
    }

    Status.error("Unknown history type encountered: " + type);
    return Entry.HistoryProto.Type.UNKNOWN;
  }

  public String describe() {
    String action;
    switch (type) {
      default:
      case unknown:
        action = "Unknown action for " + describeIds();
        break;

      case other:
        action = "Other action for " + describeIds();
        break;

      case create:
        return describeCreate();

      case add:
        action = "Added " + describeIds();
        break;

      case remove:
        return describeRemove();

      case xp:
        return describeXp();

      case expired:
        return describeExpired();
    }

    if (description == null || description.isEmpty()) {
      return action;
    }

    return action + " (" + description + ")";
  }

  private String describeCreate() {
    if (ids.isEmpty()) {
      return description;
    }

    switch (extractType(ids.get(0))) {
      case Campaign.TYPE:
        return "Created campaign " + description;

      case Character.TYPE:
        return "Created character " + description;

      case Creature.TYPE:
        return "Created creature " + description;

      default:
        return "Created " + describeIds() + " (" + description + ")";
    }
  }

  private String describeRemove() {
    if (ids.isEmpty()) {
      return description;
    }

    switch (extractType(ids.get(0))) {
      case Campaign.TYPE:
        return "Removed campaign " + description;

      case Character.TYPE:
        return "Removed character " + description;

      case Creature.TYPE:
        return "Removed creature " + description;

      default:
        return "Removed " + describeIds() + " (" + description + ")";
    }
  }

  private String describeXp() {
    if (ids.size() < 2) {
      return description;
    }

    Optional<Character> character = context.characters().getCharacter(ids.get(0)).getValue();
    Optional<Campaign> campaign = context.campaigns().getCampaign(ids.get(1)).getValue();
    if (character.isPresent() && campaign.isPresent()) {
      return campaign.get().getDm() + " has awarded " + description + " XP to "
          + character.get().getName();
    } else {
      return "The DM has awarded " + ids.get(0) + " " + description + " XP";
    }
  }

  private String describeExpired() {
    if (ids.size() < 2) {
      return description;
    }

    Optional<Character> character = context.characters().getCharacter(ids.get(0)).getValue();
    Optional<Campaign> campaign = context.campaigns().getCampaign(ids.get(1)).getValue();
    if (character.isPresent() && campaign.isPresent()) {
      return "The " + description + " condition for character " + character.get().getName()
          + " (" + campaign.get().getName() + ") has expired.";
    } else {
      return "The " + description + " condition for " + ids.get(0) + " has expired.";
    }
  }

  private String describeIds() {
    return Strings.SPACE_JOINER.join(ids.stream()
        .map(this::describeById)
        .collect(Collectors.toList()));
  }

  private String describeById(String id) {
    Optional<? extends StoredEntry> entry = context.entryForId(id);
    if (entry.isPresent()) {
      return entry.toString();
    } else {
      return id;
    }
  }

  private String formatDate() {
    for (String id : ids) {
      if (extractType(id).equals(Campaign.TYPE)) {
        Optional<Campaign> campaign = context.campaigns().getCampaign(id).getValue();
        if (campaign.isPresent()) {
          return campaign.get().getCalendar().format(gameDate);
        }
      }
    }

    return gameDate.toString();
  }
}
