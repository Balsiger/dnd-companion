/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.values;

import android.text.Spanned;

import net.ixitxachitls.companion.data.documents.Data;
import net.ixitxachitls.companion.data.enums.Ability;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An adjustment to an ability value.
 */
public class AbilityAdjustment extends Adjustment {

  protected static final Pattern PARSE_PATTERN = Pattern.compile("(?i:\\s*((?:\\+|-)\\d+)"
        + "\\s+(" + Modifier.TYPE_PATTERN + ")"
        + "\\s*(" + Ability.PATTERN + ")\\s*)");
  protected static final String FIELD_ABILITY = "ability";
  protected static final String FIELD_MODIFIER = "modifier";
  private final Ability ability;
  private final Modifier modifier;

  public AbilityAdjustment(Ability ability, Modifier modifier) {
    this(Type.ability, ability, modifier);
  }

  protected AbilityAdjustment(Type type, Ability ability, Modifier modifier) {
    super(type);

    this.ability = ability;
    this.modifier = modifier;
  }

  public Ability getAbility() {
    return ability;
  }

  public Modifier getModifier() {
    return modifier;
  }

  @Override
  public Spanned toSpanned() {
    return toSupportedSpanned();
  }

  @Override
  public Map<String, Object> write() {
    Map<String, Object> data = super.write();
    data.put(FIELD_ABILITY, ability.toString());
    data.put(FIELD_MODIFIER, modifier.write());

    return data;
  }

  @Override
  public String toString() {
    return modifier.toShortString() + " " + ability.getName();
  }

  public static Optional<AbilityAdjustment> parseAbility(String text, String source) {
    Matcher matcher = PARSE_PATTERN.matcher(text);
    if (matcher.matches()) {
      return Optional.of(new AbilityAdjustment(Ability.fromName(matcher.group(3)),
          new Modifier(Integer.parseInt(matcher.group(1)),
              matcher.group(2).isEmpty()
                  ? Modifier.Type.GENERAL : Modifier.Type.valueOf(matcher.group(2).toUpperCase()),
              source)));
    }

    return Optional.empty();
  }

  public static AbilityAdjustment readAbility(Data data) {
    Ability ability = data.get(FIELD_ABILITY, Ability.UNKNOWN);
    Modifier modifier = Modifier.read(data.getNested(FIELD_MODIFIER));

    return new AbilityAdjustment(ability, modifier);
  }
}
