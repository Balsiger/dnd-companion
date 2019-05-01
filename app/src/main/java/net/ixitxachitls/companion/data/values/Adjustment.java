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

import android.graphics.Color;
import android.support.annotation.CallSuper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import net.ixitxachitls.companion.data.documents.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A modifier with a description of the target value that is to be modified.
 */
public abstract class Adjustment {

  public enum Type {generic, ability, abilityCheck, speed, saves, initiative}

  private static final String FIELD_TYPE = "type";
  private final Type type;

  protected Adjustment(Type type) {
    this.type = type;
  }

  public boolean is(Type type) {
    return this.type == type;
  }

  public Spanned toSpanned() {
    return new SpannableStringBuilder(toString());
  }

  @CallSuper
  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_TYPE, type.name());

    return data;
  }

  protected Spanned toSupportedSpanned() {
    String text = toString();
    SpannableStringBuilder builder = new SpannableStringBuilder(text);
    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#00AA00")), 0, text.length(),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    return builder;
  }

  public static Adjustment parse(String text, String source) {
    Optional<? extends Adjustment> parsed = AbilityCheckAdjustment.parseAbilityCheck(text, source);
    if (parsed.isPresent()) {
      return parsed.get();
    }

    parsed = AbilityAdjustment.parseAbility(text, source);
    if (parsed.isPresent()) {
      return parsed.get();
    }

    parsed = InitiativeAdjustment.parseInitiative(text, source);
    if (parsed.isPresent()) {
      return parsed.get();
    }

    parsed = SpeedAdjustment.parseSpeed(text, source);
    if (parsed.isPresent()) {
      return parsed.get();
    }

    parsed = SavesAdjustment.parseSaves(text, source);
    if (parsed.isPresent()) {
      return parsed.get();
    }

    return new GenericAdjustment(text);
  }

  public static Adjustment read(Data data) {
    switch (data.get(FIELD_TYPE, Type.generic)) {
      default:
      case generic:
        return GenericAdjustment.readGeneric(data);

      case ability:
        return AbilityAdjustment.readAbility(data);

      case abilityCheck:
        return AbilityCheckAdjustment.readAbility(data);

      case initiative:
        return InitiativeAdjustment.readInitiative(data);

      case speed:
        return SpeedAdjustment.readSpeed(data);

      case saves:
        return SavesAdjustment.readSaves(data);
    }
  }
}
