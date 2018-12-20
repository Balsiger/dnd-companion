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

import net.ixitxachitls.companion.data.enums.Ability;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An adjustment for an ability check.
 */
public class AbilityCheckAdjustment extends AbilityAdjustment {

  private static final Pattern PARSE_PATTERN =
      Pattern.compile(AbilityAdjustment.PARSE_PATTERN.pattern() + "\\s+check\\s*");

  public AbilityCheckAdjustment(Ability ability, Modifier modifier) {
    super(Adjustment.Type.abilityCheck, ability, modifier);
  }

  @Override
  public String toString() {
    return super.toString() + " check";
  }

  public static Optional<AbilityAdjustment> parseAbilityCheck(String text, String source) {
    Matcher matcher = PARSE_PATTERN.matcher(text);
    if (matcher.matches()) {
      return Optional.of(new AbilityCheckAdjustment(Ability.fromName(matcher.group(3)),
          new Modifier(Integer.parseInt(matcher.group(1)),
              matcher.group(2).isEmpty()
                  ? Modifier.Type.GENERAL : Modifier.Type.valueOf(matcher.group(2).toUpperCase()),
              source)));
    }

    return Optional.empty();
  }

  public static AbilityCheckAdjustment readAbility(Map<String, Object> data) {
    Ability ability = Values.get(data, FIELD_ABILITY, Ability.UNKNOWN);
    Modifier modifier = Modifier.read(Values.get(data, FIELD_MODIFIER));

    return new AbilityCheckAdjustment(ability, modifier);
  }
}
