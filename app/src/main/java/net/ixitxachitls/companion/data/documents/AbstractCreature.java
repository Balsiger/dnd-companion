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

package net.ixitxachitls.companion.data.documents;

import android.support.annotation.CallSuper;

import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.data.dynamics.Item;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.data.statics.Monster;
import net.ixitxachitls.companion.data.values.TargetedTimedCondition;
import net.ixitxachitls.companion.data.values.TimedCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The base for all creatures or characters.
 */
public class AbstractCreature<T extends AbstractCreature<T>> extends Document<T> {
  private static final int NO_INITIATIVE = 200;

  private String campaignId = "";
  private String name;
  private Optional<Monster> race = Optional.empty();
  private Gender gender = Gender.UNKNOWN;
  private int strength;
  private int constitution;
  private int dexterity;
  private int intelligence;
  private int wisdom;
  private int charisma;
  private int hp;
  private int maxHp;
  private int nonlethalDamage;
  private List<Item> items = new ArrayList<>();
  private int initiative = NO_INITIATIVE;
  private int initiativeRandom = 0;
  private int battleNumber = 0;
  private List<TargetedTimedCondition> initiatedConditions = new ArrayList<>();
  private List<TimedCondition> affectedCOnditiosn = new ArrayList<>();

  public AbstractCreature(String path) {
    super(path);
  }

  public AbstractCreature(DocumentSnapshot snapshot) {
    super(snapshot);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCampaignId() {
    return campaignId;
  }

  public void setCampaignId(String campaignId) {
    this.campaignId = campaignId;
  }

  @Override
  @CallSuper
  protected void read() {

  }

  @Override
  protected Map<String, Object> write(Map<String, Object> data) {
    return null;
  }
}
