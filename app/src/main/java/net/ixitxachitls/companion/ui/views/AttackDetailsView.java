/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.values.Damage;
import net.ixitxachitls.companion.data.values.Distance;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.data.values.ModifiedValue;
import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by balsiger on 2019-05-20.
 */
public class AttackDetailsView extends LinearLayout {

  private final TextWrapper<TextView> weapon;
  private final TextWrapper<TextView> type;
  private final ModifiedValueView attack;
  private final TextWrapper<TextView> multipleAttacks;
  private final TextWrapper<TextView> damage;
  private final TextWrapper<TextView> criticalDelimiter;
  private final TextWrapper<TextView> critical;
  private final TextWrapper<TextView> notes;

  public AttackDetailsView(Context context, Character character, Item item, boolean large) {
    this(context);

    if (large) {
      weapon.textStyle(R.style.LargeText);
      type.textStyle(R.style.LargeText);
      attack.setTextAppearance(R.style.LargeText);
      multipleAttacks.textStyle(R.style.LargeText);
      damage.textStyle(R.style.LargeText);
      criticalDelimiter.textStyle(R.style.LargeText);
      critical.textStyle(R.style.LargeText);
      notes.textStyle(R.style.LargeText);
    }

    set(character, item);
  }

  public AttackDetailsView(Context context) {
    this(context, null);
  }

  public AttackDetailsView(Context context, AttributeSet attributes) {
    this(context, attributes, 0);
  }

  public AttackDetailsView(Context context, AttributeSet attributes, int defStyleAttribute) {
    super(context, attributes, defStyleAttribute);

    ViewGroup view = (ViewGroup)
        LayoutInflater.from(getContext()).inflate(R.layout.view_attack_details, null, false);

    weapon = TextWrapper.wrap(view, R.id.weapon);
    type = TextWrapper.wrap(view, R.id.type);
    attack = view.findViewById(R.id.attack);
    multipleAttacks = TextWrapper.wrap(view, R.id.multiple_attacks);
    damage = TextWrapper.wrap(view, R.id.damage);
    criticalDelimiter = TextWrapper.wrap(view, R.id.critical_delimiter);
    critical = TextWrapper.wrap(view, R.id.critical);
    notes = TextWrapper.wrap(view, R.id.notes);

    addView(view);
  }

  public void set(Character character, Item item) {
    ModifiedValue bonus = character.attackBonus(item);
    int attacks = character.numberOfAttacks(item);

    weapon.text(item.getPlayerName() + ": ");
    type.text(convert(item.getWeaponStyle()));
    attack.set(bonus);

    if (attacks <= 1) {
      multipleAttacks.gone();
    } else {
      multipleAttacks.visible();
      int totalBonus = bonus.total();
      String multiple = "";
      for (int i = 1; i < attacks; i++) {
        multiple += "/" + Strings.signed(totalBonus - i * 6);
      }
      multipleAttacks.text(multiple);
    }

    Damage damageValue = character.damage(item);
    damage.text(damageValue.toString());
    damage.onLongClick(() -> MessageDialog.create(getContext())
        .title("Damage " + item.getPlayerName())
        .message(Strings.NEWLINE_JOINER.join(damageValue.details()))
        .show());

    int criticalLow = item.weaponCriticalLow();
    int criticalMultiplier = item.weaponCriticalMultiplier();
    if (criticalLow == 20 && criticalMultiplier == 2) {
      criticalDelimiter.gone();
      critical.gone();
    } else {
      criticalDelimiter.visible();
      critical.visible();
      critical.text(criticalLow + "-20/x" + criticalMultiplier);
    }

    List<String> notesParts = new ArrayList<>();
    Optional<Distance> range = item.range();
    Optional<Distance> reach = item.reach();

    if (range.isPresent() && !range.get().isZero()) {
      notesParts.add("range " + range.get());
    }
    if (reach.isPresent() && !reach.get().isZero() && (int) reach.get().asFeet() != 5) {
      notesParts.add("reach " + reach.get());
    }
    notes.text(Strings.COMMA_JOINER.join(notesParts));
  }

  private String convert(Value.WeaponStyle style) {
    switch (style) {
      default:
      case UNKNOWN_STYLE:
      case UNRECOGNIZED:
        return "unknown";

      case TWOHANDED_MELEE:
        return "two handed melee";

      case LIGHT_MELEE:
      case ONEHANDED_MELEE:
        return "melee";

      case UNARMED:
        return "unarmed melee";

      case RANGED_TOUCH:
        return "ranged touch";

      case RANGED:
        return "ranged";

      case THROWN_TOUCH:
        return "thrown touch";

      case THROWN:
        return "thrown";

      case TOUCH:
        return "touch";

      case THROWN_TWO_HANDED:
        return "thrown two handed";
    }
  }
}
