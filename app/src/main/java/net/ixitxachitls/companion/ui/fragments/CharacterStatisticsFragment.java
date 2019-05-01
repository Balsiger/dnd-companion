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

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Documents;
import net.ixitxachitls.companion.data.documents.Feat;
import net.ixitxachitls.companion.data.documents.Level;
import net.ixitxachitls.companion.data.documents.Quality;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.values.Distance;
import net.ixitxachitls.companion.rules.XP;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.ui.views.AbilityView;
import net.ixitxachitls.companion.ui.views.ConditionIconsView;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.LabelledView;
import net.ixitxachitls.companion.ui.views.ModifiedValueView;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;
import java.util.Set;

/**
 * Fragment for a character's base statistics
 */
public class CharacterStatisticsFragment extends NestedCompanionFragment {

  protected Optional<Character> character = Optional.empty();

  // UI elements.
  protected ConditionIconsView conditions;
  protected AbilityView strength;
  protected AbilityView dexterity;
  protected AbilityView constitution;
  protected AbilityView intelligence;
  protected AbilityView wisdom;
  protected AbilityView charisma;
  protected LabelledEditTextView xp;
  protected Wrapper<ImageView> xpAdjust;
  protected TextWrapper<TextView> xpNext;
  protected LabelledTextView levels;
  protected LabelledEditTextView hp;
  protected Wrapper<ImageView> hpAdjust;
  protected LabelledTextView hpMax;
  protected LabelledEditTextView damageNonlethal;
  protected Wrapper<ImageView> hpNonlethalAdjust;
  protected TextWrapper<TextView> levelUp;
  protected LabelledView<LabelledView, ModifiedValueView> fortitude;
  protected LabelledView<LabelledView, ModifiedValueView> will;
  protected LabelledView<LabelledView, ModifiedValueView> reflex;
  protected LabelledView<LabelledView, ModifiedValueView> initiative;
  protected LabelledView<LabelledView, LinearLayout> speed;
  protected TextWrapper<TextView> speedFeet;
  protected ModifiedValueView speedSquares;
  protected FlexboxLayout feats;
  protected FlexboxLayout qualities;

  public CharacterStatisticsFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
    super.onCreateView(inflater, container, state);

    ViewGroup view =
        (ViewGroup) inflater.inflate(R.layout.fragment_character_statistics, container, false);

    LinearLayout conditionsContainer = view.findViewById(R.id.conditions);
    conditions = new ConditionIconsView(view.getContext(), LinearLayout.HORIZONTAL,
        R.color.character, R.color.characterDark);
    conditionsContainer.addView(conditions);

    strength = view.findViewById(R.id.strength);
    dexterity = view.findViewById(R.id.dexterity);
    constitution = view.findViewById(R.id.constitution);
    intelligence = view.findViewById(R.id.intelligence);
    wisdom = view.findViewById(R.id.wisdom);
    charisma = view.findViewById(R.id.charisma);

    xp = view.findViewById(R.id.xp);
    xp.disabled().onBlur(this::redraw);
    xpAdjust = Wrapper.<ImageView>wrap(view, R.id.xp_adjust).gone();
    xpNext = TextWrapper.wrap(view, R.id.xp_next);
    levels = view.findViewById(R.id.levels);
    levelUp = TextWrapper.wrap(view, R.id.level_up);

    hp = view.findViewById(R.id.hp);
    hp.enabled(false);
    hpAdjust = Wrapper.<ImageView>wrap(view, R.id.hp_adjust).gone();
    hpMax = view.findViewById(R.id.hp_max);
    hpMax.enabled(false);
    damageNonlethal = view.findViewById(R.id.hp_nonlethal);
    damageNonlethal.enabled(false);
    hpNonlethalAdjust = Wrapper.<ImageView>wrap(view, R.id.nonlethal_adjust).gone();

    fortitude = view.findViewById(R.id.fortitude);
    fortitude.view(new ModifiedValueView(getContext()).ranged());
    fortitude.getView().style(R.style.LargeText);
    will = view.findViewById(R.id.will);
    will.view(new ModifiedValueView(getContext()).ranged());
    will.getView().style(R.style.LargeText);
    reflex = view.findViewById(R.id.reflex);
    reflex.view(new ModifiedValueView(getContext()).ranged());
    reflex.getView().style(R.style.LargeText);

    initiative = view.findViewById(R.id.initiative);
    initiative.view(new ModifiedValueView(getContext()));
    initiative.getView().style(R.style.LargeText);
    speed = view.findViewById(R.id.speed);
    speedFeet = TextWrapper.wrap(speed, R.id.speed_feet);
    speedSquares = speed.findViewById(R.id.speed_squares);

    feats = view.findViewById(R.id.feats);
    qualities = view.findViewById(R.id.qualities);

    // TODO(merlin): This might be unnecessary?
    if (character.isPresent()) {
      update(character.get());
    }

    characters().observe(this, this::update);

    return view;
  }

  public void update(Character character) {
    this.character = Optional.of(character);

    if (strength == null) {
      return;
    }

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
        .validate(new EditTextWrapper.RangeValidator(-20, character.getMaxHp()));
    hpMax.text(String.valueOf(character.getMaxHp()));
    damageNonlethal.text(String.valueOf(character.getNonlethalDamage()));
    conditions.update(character);

    fortitude.getView().set(character.fortitude());
    will.getView().set(character.will());
    reflex.getView().set(character.reflex());

    initiative.getView().set(character.initiative());
    speedFeet.text(Distance.fromSquares(character.speed().total()).toString());
    speedSquares.set(character.speed());

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

      qualities.removeAllViews();
      Set<Quality> collectedQualities = character.collectQualities();
      i = 1;
      for (Quality quality : collectedQualities) {
        boolean last = i++ == collectedQualities.size();
        TextWrapper<TextView> text = TextWrapper.wrap(new TextView(getContext()))
            .noWrap();
        text.text(quality.getName() + (last ? "" : ", ")).textStyle(R.style.SmallText)
            .onClick(() -> showQuality(quality));

        qualities.addView(text.get());
      }
    }

    redraw();
  }

  protected void redraw() {
    if (character.isPresent()) {
      int level = character.get().getLevel();
      xpNext.text("(next " + XP.xpForLevel(level <= 1 ? 2 : level +1) + ")");
      hp.text(String.valueOf(character.get().getHp()));
      levels.text(Level.summarized(character.get().getLevels()))
          .error(character.get().validateLevels());
      levelUp.visible(character.get().getMaxLevel() > character.get().getLevel()
          || (character.get().getXp() == 0 && character.get().getLevel() == 0));
    }
  }

  private void showFeat(Feat feat) {
    String message = feat.getSource() + "\n\n" + feat.getTemplate().getDescription();
    MessageDialog.create(getContext()).title(feat.getTitle()).message(message).show();
  }

  private void showQuality(Quality quality) {
    String message = quality.getEntity() + "\n\n" + quality.getTemplate().getDescription();
    MessageDialog.create(getContext()).title(quality.getName()).formatted(message).show();
  }

  private void update(Documents.Update update) {
    if (character.isPresent()) {
      update(character.get());
    }
  }
}
