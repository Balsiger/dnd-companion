/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

package net.ixitxachitls.companion.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.ui.views.Views;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;

/**
 * A dialog to select the skills for a level.
 */
public class LevelSkillsDialog extends Dialog<LevelSkillsDialog, Map<String, Integer>> {

  private static final String ARG_POINTS = "points";
  private static final String ARG_CLASS_SKILLS = "class_skills";
  private static final String ARG_CLASS_RANKS = "class_ranks";
  private static final String ARG_CROSS_CLASS_SKILLS = "cross_class_skills";
  private static final String ARG_CROSS_CLASS_RANKS = "cross_class_ranks";
  private static final String ARG_MAX_RANKS = "max_ranks";

  private int totalPoints = 0;
  private int remainingPoints = 0;
  private SortedMap<String, Integer> classSkills = new TreeMap<>();
  private SortedMap<String, Integer> crossClassSkills = new TreeMap<>();

  // UI elements.
  private TextWrapper<TextView> remainingView;
  private LinearLayout classSkillsView;
  private LinearLayout crossClassSkillsView;

  @Override
  protected Map<String, Integer> getValue() {
    Map<String, Integer> value = new TreeMap<>();
    Views.<LineView>extractDataFromChildren(classSkillsView,
        v -> {
          if (v.getRanks() > 0) {
            value.put(v.getName(), v.getRanks());
          }
        });
    Views.<LineView>extractDataFromChildren(crossClassSkillsView,
        v -> {
          if (v.getRanks() > 0) {
            value.put(v.getName(), v.getRanks());
          }
        });

    return value;
  }

  @Override
  protected void createContent(View view) {
    totalPoints = getArguments().getInt(ARG_POINTS);
    remainingPoints = totalPoints;

    List<String> classSkillNames = getArguments().getStringArrayList(ARG_CLASS_SKILLS);
    List<Integer> classSkillRanks = getArguments().getIntegerArrayList(ARG_CLASS_RANKS);
    List<String> crossClassSkillNames = getArguments().getStringArrayList(ARG_CROSS_CLASS_SKILLS);
    List<Integer> crossClassSkillRanks = getArguments().getIntegerArrayList(ARG_CROSS_CLASS_RANKS);
    int maxRanks = getArguments().getInt(ARG_MAX_RANKS);

    for (int i = 0; i < classSkillNames.size(); i++) {
      classSkills.put(classSkillNames.get(i), classSkillRanks.get(i));
      remainingPoints -= classSkillRanks.get(i);
    }
    for (int i = 0; i < crossClassSkillNames.size(); i++) {
      crossClassSkills.put(crossClassSkillNames.get(i), crossClassSkillRanks.get(i));
      remainingPoints -= crossClassSkillRanks.get(i) * 2;
    }

    remainingView = TextWrapper.wrap(view, R.id.remaining);
    classSkillsView = view.findViewById(R.id.class_skills);
    crossClassSkillsView = view.findViewById(R.id.crossclass_skills);

    for (Map.Entry<String, Integer> skill : classSkills.entrySet()) {
      classSkillsView.addView(new LineView(getContext(), skill.getKey(), skill.getValue(), maxRanks,
          false));
    }
    for (Map.Entry<String, Integer> skill : crossClassSkills.entrySet()) {
      crossClassSkillsView.addView(new LineView(getContext(), skill.getKey(), skill.getValue(),
          maxRanks, true));
    }

    Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);

    refresh();
  }

  private void adjustRemaining(int value) {
    remainingPoints += value;
    refresh();
  }

  private void refresh() {
    remainingView.text(String.valueOf(remainingPoints));
  }

  protected static Bundle arguments(@LayoutRes int layoutId, String title,
                                    @ColorRes int colorId, int points,
                                    ArrayList<String> classSkillNames,
                                    ArrayList<Integer> classSkillRanks,
                                    ArrayList<String> crossClassSkillNames,
                                    ArrayList<Integer> crossClassSkillRanks,
                                    int maxRanks) {
    Bundle arguments = Dialog.arguments(layoutId, title, colorId);
    arguments.putInt(ARG_POINTS, points);
    arguments.putStringArrayList(ARG_CLASS_SKILLS, classSkillNames);
    arguments.putIntegerArrayList(ARG_CLASS_RANKS, classSkillRanks);
    arguments.putStringArrayList(ARG_CROSS_CLASS_SKILLS, crossClassSkillNames);
    arguments.putIntegerArrayList(ARG_CROSS_CLASS_RANKS, crossClassSkillRanks);
    arguments.putInt(ARG_MAX_RANKS, maxRanks);
    return arguments;
  }

  public static LevelSkillsDialog newInstance(int totalPoints, int maxRanks,
                                              Set<String> classSkills,
                                              Map<String, Integer> skills) {
    ArrayList<String> classSkillNames = new ArrayList<>();
    ArrayList<Integer> classSkillRanks = new ArrayList<>();
    ArrayList<String> crossClassSkillNames = new ArrayList<>();
    ArrayList<Integer> crossClassSkillRanks = new ArrayList<>();

    for (Map.Entry<String, Integer> entry : skills.entrySet()) {
      if (classSkills.contains(entry.getKey().toLowerCase())) {
        classSkillNames.add(entry.getKey());
        classSkillRanks.add(entry.getValue());
      } else {
        crossClassSkillNames.add(entry.getKey());
        crossClassSkillRanks.add(entry.getValue());
      }
    }

    // Add the skills that have no ranks.
    for (String skill : Templates.get().getSkillTemplates().getNames()) {
      if (!skills.keySet().contains(skill)) {
        if (classSkills.contains(skill.toLowerCase())) {
          classSkillNames.add(skill);
          classSkillRanks.add(0);
        } else {
          crossClassSkillNames.add(skill);
          crossClassSkillRanks.add(0);
        }
      }
    }

    LevelSkillsDialog dialog = new LevelSkillsDialog();
    dialog.setArguments(arguments(R.layout.dialog_level_skills, "Skills",
        R.color.character, totalPoints, classSkillNames, classSkillRanks,
        crossClassSkillNames, crossClassSkillRanks, maxRanks));
    return dialog;
  }

  private class LineView extends LinearLayout {
    private final TextWrapper<TextView> ranksView;
    private final int maxRanks;
    private final boolean crossClassSkill;
    private final String name;
    private int ranks;

    public LineView(Context context, String name, int ranks, int maxRanks,
                    boolean crossClassSkill) {
      super(context);
      this.name = name;
      this.ranks = ranks;
      this.maxRanks = maxRanks;
      this.crossClassSkill = crossClassSkill;

      View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_level_skills_line,
          this, false);
      TextWrapper.wrap(view, R.id.name).text(name)
          .onClick(this::increase)
          .onLongClick(this::reset);
      this.ranksView = TextWrapper.wrap(view, R.id.ranks);

      refresh();

      addView(view);
    }

    public String getName() {
      return name;
    }

    public int getRanks() {
      return ranks;
    }

    private void increase() {
      ranks++;
      if (ranks > maxRanks) {
        reset();
      } else {
        adjustRemaining(crossClassSkill ? -2 : -1);
        refresh();
      }
    }

    private void refresh() {
      ranksView.text(String.valueOf(ranks));
    }

    private void reset() {
      adjustRemaining(crossClassSkill ? ranks * 2 : ranks);
      ranks = 0;
      refresh();
    }
  }
}
