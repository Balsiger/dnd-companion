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

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.common.collect.Multimap;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Feat;
import net.ixitxachitls.companion.data.documents.Level;
import net.ixitxachitls.companion.data.documents.Quality;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.values.Distance;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.data.values.ModifiedValue;
import net.ixitxachitls.companion.databinding.FragmentCharacterStatisticsBinding;
import net.ixitxachitls.companion.rules.Items;
import net.ixitxachitls.companion.rules.XP;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.ui.dialogs.LevelsDialog;
import net.ixitxachitls.companion.ui.dialogs.NumberAdjustDialog;
import net.ixitxachitls.companion.ui.dialogs.QualityDialog;
import net.ixitxachitls.companion.ui.views.AbilityView;
import net.ixitxachitls.companion.ui.views.AttackDetailsView;
import net.ixitxachitls.companion.ui.views.ConditionIconsView;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.ModifiedValueView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Validator;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 * Fragment for a character's base statistics
 */
public class CharacterStatisticsFragment extends NestedCompanionFragment {

  protected Character character = Character.DEFAULT;

  // UI elements.
  protected FragmentCharacterStatisticsBinding views;
  protected ConditionIconsView conditions;
  protected Wrapper<ImageView> abilitiesGroup;
  protected AbilityView strength;
  protected AbilityView dexterity;
  protected AbilityView constitution;
  protected AbilityView intelligence;
  protected AbilityView wisdom;
  protected AbilityView charisma;

  protected Wrapper<ImageView> levelGroup;
  protected LabelledTextView<LabelledTextView, TextView> levels;
  protected LabelledEditTextView xp;
  protected Wrapper<ImageView> xpAdjust;

  protected Wrapper<ImageView> healthGroup;
  protected LabelledEditTextView hp;
  protected Wrapper<ImageView> hpAdjust;
  protected TextWrapper<TextView> hpMax;
  protected LabelledEditTextView damageNonlethal;
  protected Wrapper<ImageView> hpNonlethalAdjust;

  protected Wrapper<ImageView> battleGroup;
  protected ModifiedValueView ac;
  protected ModifiedValueView acTouch;
  protected ModifiedValueView acFlat;
  protected LinearLayout attacks;
  protected ModifiedValueView initiative;
  protected TextWrapper<TextView> speedFeet;
  protected ModifiedValueView speedSquares;
  protected ModifiedValueView fortitude;
  protected ModifiedValueView will;
  protected ModifiedValueView reflex;

  protected Wrapper<ImageView> featsGroup;
  protected FlexboxLayout feats;

  protected Wrapper<ImageView> skillsGroup;
  protected FlexboxLayout skills;

  protected Wrapper<ImageView> qualitiesGroup;
  protected FlexboxLayout qualities;

  public CharacterStatisticsFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
    super.onCreateView(inflater, container, state);

    views = FragmentCharacterStatisticsBinding.inflate(inflater, container, false);
    ViewGroup view = (ViewGroup) views.getRoot();

    LinearLayout conditionsContainer = views.conditions;
    conditions = new ConditionIconsView(view.getContext(), LinearLayout.HORIZONTAL,
        R.color.character, R.color.characterDark);
    conditionsContainer.addView(conditions);

    abilitiesGroup = Wrapper.<ImageView>wrap(view, R.id.group_abilities)
        .onClick(() -> showSimpleMessage("Abilities", "All your ability values and modifiers.\n"
            + "Long press on any modifier to get a detailed look at how it was computed."));
    strength = view.findViewById(R.id.strength);
    dexterity = view.findViewById(R.id.dexterity);
    constitution = view.findViewById(R.id.constitution);
    intelligence = view.findViewById(R.id.intelligence);
    wisdom = view.findViewById(R.id.wisdom);
    charisma = view.findViewById(R.id.charisma);

    levelGroup = Wrapper.<ImageView>wrap(view, R.id.group_levels).onClick(this::showLevelSummary);
    levels = view.findViewById(R.id.levels);
    xp = view.findViewById(R.id.xp);
    xp.disabled().onBlur(this::redraw);
    xpAdjust = Wrapper.<ImageView>wrap(view, R.id.xp_adjust).gone();

    healthGroup = Wrapper.<ImageView>wrap(view, R.id.group_health).onClick(this::showHealthSummary);
    hp = view.findViewById(R.id.hp);
    hp.enabled(false);
    hpAdjust = Wrapper.<ImageView>wrap(view, R.id.hp_adjust).gone();
    hpMax = TextWrapper.wrap(view, R.id.hp_max);
    damageNonlethal = view.findViewById(R.id.hp_nonlethal);
    damageNonlethal.enabled(false);
    hpNonlethalAdjust = Wrapper.<ImageView>wrap(view, R.id.nonlethal_adjust).gone();

    battleGroup = Wrapper.<ImageView>wrap(view, R.id.group_battle)
        .onClick(() -> showSimpleMessage("Battle", "All your battle statistics.\n"
            + "Long press on any value to get an overview how it was computed."));
    Wrapper.wrap(view, R.id.icon_ac)
        .onClick(() -> showSimpleMessage("Armor Class",
            "The current armor class from all your worn items.\n"
                + "Long press on a value to see how it was computed."));
    ac = view.findViewById(R.id.ac);
    acTouch = view.findViewById(R.id.ac_touch);
    acFlat = view.findViewById(R.id.ac_flat_footed);
    Wrapper.wrap(view, R.id.icon_attack)
        .onClick(() -> showSimpleMessage("Attacks",
            "All the attacks you could possibly do with worn weapons.\n"
                + "Long press on a value to see details on how it was computed."));
    attacks = view.findViewById(R.id.attacks);
    Wrapper.wrap(view, R.id.icon_initiative)
        .onClick(() -> showSimpleMessage("Initiative",
            "Your initiative modifier.\n"
                + "Long press on the value to see details on how it was computed."));
    initiative = view.findViewById(R.id.initiative);
    Wrapper.wrap(view, R.id.icon_speed)
        .onClick(() -> showSimpleMessage("Speed",
            "The speed of your character in feet and squares.\n"
                + "Long press on on the squares value to see details on how it was computed."));
    speedFeet = TextWrapper.wrap(view, R.id.speed_feet);
    speedSquares = view.findViewById(R.id.speed_squares);
    fortitude = view.findViewById(R.id.fortitude);
    fortitude.ranged().style(R.style.LargeText);
    will = view.findViewById(R.id.will);
    will.ranged().style(R.style.LargeText);
    reflex = view.findViewById(R.id.reflex);
    reflex.ranged().style(R.style.LargeText);

    featsGroup = Wrapper.<ImageView>wrap(view, R.id.group_feats)
        .onClick(() -> showSimpleMessage("Feats", "All your current feats.\n"
            + "Press on a feat to get more details."));
    feats = view.findViewById(R.id.feats);

    skillsGroup = Wrapper.<ImageView>wrap(view, R.id.group_skills)
        .onClick(() -> showSimpleMessage("Skills", "All your skills with a non-zero modifier.\n"
            + "Press on a skill rank to see how the skill ranks were computed."));
    skills = view.findViewById(R.id.skills);

    qualitiesGroup = Wrapper.<ImageView>wrap(view, R.id.group_qualities)
        .onClick(() -> showSimpleMessage("Qualities",
            "All the qualities your character has from race and class levels.\n"
                + "Press on a quality to get more information about it."));
    qualities = view.findViewById(R.id.qualities);

    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    views = null;
  }

  @Override
  public void onResume() {
    super.onResume();

    update();
  }

  public void show(Character character) {
    this.character = character;
  }

  public void update() {
    if (getView() == null) {
      return;
    }

    Status.log("updating character " + character.getName());
    strength.update(Ability.STRENGTH, character.getStrength(), character.getStrengthCheck());
    dexterity.update(Ability.DEXTERITY, character.getDexterity(), character.getDexterityCheck());
    constitution.update(Ability.CONSTITUTION, character.getConstitution(),
        character.getConstitutionCheck());
    intelligence.update(Ability.INTELLIGENCE, character.getIntelligence(),
        character.getIntelligenceCheck());
    wisdom.update(Ability.WISDOM, character.getWisdom(), character.getWisdomCheck());
    charisma.update(Ability.CHARISMA, character.getCharisma(), character.getCharismaCheck());

    xp.text(String.valueOf(character.getXp()));
    hp.text(String.valueOf(character.getHp()))
        .validate(new Validator.RangeValidator(-20, character.getMaxHp()));
    hpMax.text(String.valueOf(character.getMaxHp()));
    damageNonlethal.text(String.valueOf(character.getNonlethalDamage()));
    conditions.update(character);

    ac.set(character.getNormalArmorClass());
    acTouch.set(character.getTouchArmorClass());
    acFlat.set(character.getFlatFootedArmorClass());

    // Update attacks.
    attacks.removeAllViews();
    for (Item item : character.getItems()) {
      if (item.isWeapon() && character.isWearing(item)) {
        if (character.isWearing(item, Items.Slot.hands)) {
          attacks.addView(new AttackDetailsView(getContext(), character, item, true), 0);
        } else {
          attacks.addView(new AttackDetailsView(getContext(), character, item, true));
        }
      }
    }

    fortitude.set(character.getFortitude());
    will.set(character.getWill());
    reflex.set(character.getReflex());

    initiative.set(character.getInitiative());
    speedFeet.text(Distance.fromSquares(character.getSpeed().total()).toString());
    speedSquares.set(character.getSpeed());

    // We sometimes call update before actually having a context.
    if (getContext() != null) {
      feats.removeAllViews();
      Set<Feat> collectedFeats = character.collectFeats();
      int i = 1;
      for (Feat feat : collectedFeats) {
        boolean last = i++ == collectedFeats.size();
        TextWrapper<TextView> text = TextWrapper.wrap(new TextView(getContext()))
            .noWrap();
        text.text(feat.getTitle() + (last ? "" : ", ")).textStyle(R.style.SmallText)
            .onClick(() -> showFeat(feat));

        feats.addView(text.get());
      }

      skills.removeAllViews();
      SortedMap<String, ModifiedValue> collectedSkills = character.collectSkills();
      i = 1;
      for (Map.Entry<String, ModifiedValue> skillRank : collectedSkills.entrySet()) {
        boolean last = i++ == collectedSkills.size();
        TextWrapper<TextView> text = TextWrapper.wrap(new TextView(getContext()))
            .noWrap();
        text.text(skillRank.getKey() + " " + skillRank.getValue().totalFormatted()
            + (last ? "" : ", ")).textStyle(R.style.SmallText)
            .onClick(() -> showSkill(skillRank.getKey(), skillRank.getValue()));

        skills.addView(text.get());
      }


      qualities.removeAllViews();
      Multimap<String, Quality> collectedQualities = character.collectQualities();
      boolean first = true;
      for (String key : collectedQualities.keySet()) {
        TextWrapper<TextView> text = TextWrapper.wrap(new TextView(getContext()))
            .noWrap();
        int count = collectedQualities.get(key).size();
        Quality quality = collectedQualities.get(key).iterator().next();
        String name = quality.formatName(getContext(), count, character).toString();
        text.text(first ? "" : ", " + name)
            .textStyle(R.style.SmallText)
            .onClick(() -> QualityDialog.newInstance(character, quality, name, count).display());

        qualities.addView(text.get());

        first = false;
      }
    }

    if (character.amPlayer()) {
      strength.setAction(this::editAbilities);
      dexterity.setAction(this::editAbilities);
      constitution.setAction(this::editAbilities);
      intelligence.setAction(this::editAbilities);
      wisdom.setAction(this::editAbilities);
      charisma.setAction(this::editAbilities);

      levels.onClick(this::editLevels);

      xp.onBlur(this::changeXp).enabled(true);
      xpAdjust.visible().onClick(this::adjustXp);
      hp.onBlur(this::changeHp).enabled(true);
      hpAdjust.visible().onClick(this::adjustHp);
      damageNonlethal.onBlur(this::changeNonlethalDamage).enabled(true);
      hpNonlethalAdjust.visible().onClick(this::adjustNonlethalDamage);
    } else {
      strength.removeAction();
      dexterity.removeAction();
      constitution.removeAction();
      intelligence.removeAction();
      wisdom.removeAction();
      charisma.removeAction();

      levels.removeClick();

      xp.enabled(false);
      xpAdjust.invisible().removeClick();
      hp.enabled(false);
      hpAdjust.invisible().removeClick();
      damageNonlethal.enabled(false);
      hpNonlethalAdjust.invisible().removeClick();
    }

    redraw();
  }

  private void adjustHp() {
    NumberAdjustDialog.newInstance(R.string.title_dialog_adjust_hp, R.color.character,
        "HP Adjustment", "Adjust the current HP value.")
        .setAdjustAction(this::doAdjustHp)
        .display();
  }

  private void adjustNonlethalDamage() {
    NumberAdjustDialog.newInstance(R.string.title_dialog_adjust_hp, R.color.character,
        "Nonlethal Damage Adjustment", "Adjust the current nonlethal damage.")
        .setAdjustAction(this::doAdjustNonlethalDamage)
        .display();
  }

  private void adjustXp() {
    NumberAdjustDialog.newInstance(R.string.title_dialog_adjust_xp, R.color.character,
        "XP Adjustment", "Adjust the current XP value.")
        .setAdjustAction(this::doAdjustXp)
        .display();
  }

  private void changeHp() {
    try {
      character.setHp(Integer.parseInt(hp.getText()));
    } catch (NumberFormatException e) {
      character.setHp(1);
    }
    character.store();

    redraw();
  }

  private void changeNonlethalDamage() {
    try {
      character.setNonlethalDamage(Integer.parseInt(damageNonlethal.getText()));
    } catch (NumberFormatException e) {
      character.setNonlethalDamage(0);
    }
    character.store();

    redraw();
  }

  private void changeXp() {
    if (!xp.getText().isEmpty()) {
      try {
        character.setXp(Integer.parseInt(xp.getText()));
        character.store();
      } catch (NumberFormatException e) {
        Status.error("Invalid format for xp ignored: " + xp.getText());
      }
    }
  }

  private void doAdjustHp(int value) {
    character.addHp(value);
    character.store();
    redraw();
  }

  private void doAdjustNonlethalDamage(int value) {
    character.addNonlethalDamage(value);
    character.store();
    redraw();
  }

  private void doAdjustXp(int value) {
    character.addXp(value);
    character.store();
    redraw();
  }

  private void editAbilities() {
    AbilitiesDialog.newInstance(character.getId(), character.getCampaignId()).display();
  }

  private void editLevels() {
    LevelsDialog.newInstance(character.getId()).display();
  }

  private boolean hasLevelUp() {
    return character.getMaxLevel() > character.getLevel();
  }

  protected void redraw() {
    Status.log("redraw character stats for " + character.getName());
    int level = character.getLevel();
    hp.text(String.valueOf(character.getHp()));
    levels.text(Level.summarized(character.getLevels()));
    levels.error(character.validateLevels());

    if (hasLevelUp() || !character.validateLevels().isEmpty()) {
      levelGroup.tint(R.color.error);
      Status.log("level error");
    } else {
      levelGroup.tint(R.color.characterDark);
    }

    if (!validateHealth().isEmpty()) {
      healthGroup.tint(R.color.error);
    } else {
      healthGroup.tint(R.color.characterDark);
    }
  }

  private void showFeat(Feat feat) {
    String message = feat.getSource() + "\n\n" + feat.getTemplate().getDescription();
    MessageDialog.create(getContext()).title(feat.getTitle()).message(message).show();
  }

  private void showHealthSummary() {
    List<String> messages = new ArrayList<>();
    List<String> conditions = new ArrayList<>();

    messages.addAll(validateHealth());
    conditions.addAll(character.getAdjustedConditions().stream()
        .map(c -> c.getCondition().getCondition().getName())
        .collect(Collectors.toList()));

    if (conditions.isEmpty()) {
      messages.add("No conditions.");
    } else {
      messages.add("Conditions:");
      messages.addAll(conditions);
    }

    MessageDialog.create(getContext())
        .title("Health Summary")
        .message(Strings.NEWLINE_JOINER.join(messages))
        .show();
  }

  private void showLevelSummary() {
    List<String> messages = new ArrayList<>();
    if (hasLevelUp()) {
      messages.add("Level Up!");
    }

    messages.add("Next level at " + XP.next(character.getXp()));
    messages.addAll(character.validateLevels());


    MessageDialog.create(getContext())
        .title("Level Summary")
        .message(Strings.NEWLINE_JOINER.join(messages))
        .show();
  }

  private void showQuality(Quality quality, int count) {
    String message = quality.getEntity() + "\n\n" + quality.getTemplate().getDescription();
    MessageDialog.create(getContext()).title(quality.getName())
        .formatted(message, quality.collectFormatValues(count, character)).show();
  }

  private void showSimpleMessage(String title, String message) {
    MessageDialog.create(getContext()).title(title).message(message).show();
  }

  private void showSkill(String name, ModifiedValue ranks) {
    MessageDialog.create(getContext()).title(name).message(ranks.describeModifiers()).show();
  }

  private List<String> validateHealth() {
    return new Validator.RangeValidator(-20, character.getMaxHp()).validate(hp.getText());
  }
}
