/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Player Companion.
 *
 * The Player Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Player Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.dialogs;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.rules.XP;
import net.ixitxachitls.companion.ui.views.XPCharacterView;
import net.ixitxachitls.companion.ui.views.XPFixedView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for selecting XP rewards.
 */
public class XPDialog extends Dialog {

  private static final String ARG_CAMPAIGN_ID = "campaign_id";
  private static final int MAX_ECL = 30;

  private Optional<Campaign> campaign = Optional.absent();
  private final List<TextWrapper<TextView>> eclViews = new ArrayList<>();
  private int selectedECL = 0;
  private LinearLayout characterContainer;
  private LinearLayout fixed1;
  private LinearLayout fixed2;

  public static XPDialog newInstance(String campaignId) {
    XPDialog dialog = new XPDialog();
    dialog.setArguments(arguments(R.layout.dialog_xp,
        R.string.title_xp_rewards, R.color.campaign, campaignId));
    return dialog;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String campaignId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_CAMPAIGN_ID, campaignId);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    campaign = Campaigns.getCampaign(getArguments().getString(ARG_CAMPAIGN_ID)).getValue();
  }

  @Override
  protected void createContent(View view) {
    if (campaign.isPresent()) {
      ViewGroup ecls = (ViewGroup) view.findViewById(R.id.ecls);
      LayoutInflater inflator = LayoutInflater.from(getContext());
      for (int i = 1; i <= MAX_ECL; i++) {
        // Don't display this if characters would not get xp anyway.
        if (XP.xpAward(i, campaign.get().getMinPartyLevel(), 1) <= 0
            && XP.xpAward(i, campaign.get().getMaxPartyLevel(), 1) <= 0) {
          eclViews.add(null);
          continue;
        }

        final int index = i;
        LinearLayout container = (LinearLayout) inflator.inflate(R.layout.view_ecl, null);
        TextWrapper<TextView> ecl = TextWrapper.wrap(container, R.id.ecl).text(String.valueOf(index))
            .onClick(() -> selectEcl(index)).onLongClick(() -> selectEcl(0));
        if (campaign.get().isCloseECL(i)) {
          ecl.get().setTypeface(Typeface.DEFAULT_BOLD);
        }
        if (selectedECL == i) {
          ecl.backgroundColor(R.color.colorAccent);
        }
        ecls.addView(container);
        eclViews.add(ecl);
        Wrapper.wrap(view, R.id.save).onClick(this::awardEcl);
      }

      characterContainer = (LinearLayout) view.findViewById(R.id.party);
      for (String characterId: campaign.get().getCharacterIds().getValue()) {
        Character character = Characters.getCharacter(characterId).getValue().get();
        characterContainer.addView(new XPCharacterView(getContext(), this, character));
      }

      characterContainer.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Log.d("GURU", "clicked");
        }
      });

      fixed1 = (LinearLayout) view.findViewById(R.id.fixed_1);
      fixed2 = (LinearLayout) view.findViewById(R.id.fixed_2);
      fixed1.addView(new XPFixedView(getContext(), this, 5));
      fixed1.addView(new XPFixedView(getContext(), this, 10));
      fixed1.addView(new XPFixedView(getContext(), this, 25));
      fixed1.addView(new XPFixedView(getContext(), this, 50, false));
      fixed2.addView(new XPFixedView(getContext(), this, 100));
      fixed2.addView(new XPFixedView(getContext(), this, 250));
      fixed2.addView(new XPFixedView(getContext(), this, 500));
      fixed2.addView(new XPFixedView(getContext(), this, 1000, false));
    }
  }

  private void selectEcl(int level) {
    if (selectedECL > 0) {
      eclViews.get(selectedECL - 1).backgroundColor(R.color.cell);
    }
    selectedECL = level;

    if (selectedECL > 0) {
      eclViews.get(selectedECL - 1).backgroundColor(R.color.colorAccent);
    }

    refresh();
  }

  public void refresh() {
    int selectedCharacters = selectedCharacters();
    int fixedXp = selectedCharacters > 0 ? fixedXP() / selectedCharacters : 0;
    for (int i = 0; i < characterContainer.getChildCount(); i++) {
      XPCharacterView view = (XPCharacterView) characterContainer.getChildAt(i);
      if (view.isSelected()) {
        view.setXP(fixedXp + XP.xpAward(selectedECL, view.getLevel(), selectedCharacters));
      } else {
        view.setXP(0);
      }
    }
  }

  private int fixedXP() {
    return fixedXP(fixed1) + fixedXP(fixed2);
  }

  private int fixedXP(LinearLayout layout) {
    int total = 0;
    for (int i = 0; i < layout.getChildCount(); i++) {
      XPFixedView view = (XPFixedView) layout.getChildAt(i);
      total += view.getValue();
    }

    return total;
  }

  private int selectedCharacters() {
    int selected = 0;
    for (int i = 0; i < characterContainer.getChildCount(); i++) {
      XPCharacterView view = (XPCharacterView) characterContainer.getChildAt(i);
      if (view.isSelected()) {
        selected++;
      }
    }

    return selected;
  }

  private void awardEcl() {
    if (campaign.isPresent()) {
      for (int i = 0; i < characterContainer.getChildCount(); i++) {
        XPCharacterView view = (XPCharacterView) characterContainer.getChildAt(i);
        campaign.get().awardXp(view.getCharacter(), view.getXP());
      }
    }

    save();
  }
}
