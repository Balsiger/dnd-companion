/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.dialogs;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.rules.XP;
import net.ixitxachitls.companion.ui.views.XPCharacterView;
import net.ixitxachitls.companion.ui.views.XPFixedView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Dialog for selecting XP rewards.
 */
public class XPDialog extends Dialog {

  private static final String ARG_CAMPAIGN_ID = "campaign_id";
  private static final int MAX_ECL = 30;
  private final List<TextWrapper<TextView>> eclViews = new ArrayList<>();
  private Optional<Campaign> campaign = Optional.empty();
  private int selectedECL = 0;
  private LinearLayout characterContainer;
  private LinearLayout fixed1;
  private LinearLayout fixed2;

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    campaign = campaigns().get(getArguments().getString(ARG_CAMPAIGN_ID));
  }

  @Override
  protected void createContent(View view) {
    if (campaign.isPresent()) {
      ViewGroup ecls = (ViewGroup) view.findViewById(R.id.ecls);
      LayoutInflater inflator = LayoutInflater.from(getContext());
      int minPartyLevel = characters().minPartyLevel(campaign.get().getId());
      int maxPartyLevel = characters().maxPartyLevel(campaign.get().getId());
      for (int i = 1; i <= MAX_ECL; i++) {
        // Don't display this if characters would not get xpAction anyway.
        if (XP.xpAward(i, minPartyLevel, 1) <= 0 && XP.xpAward(i, maxPartyLevel, 1) <= 0) {
          eclViews.add(null);
          continue;
        }

        final int index = i;
        LinearLayout container = (LinearLayout) inflator.inflate(R.layout.view_ecl, null);
        TextWrapper<TextView> ecl = TextWrapper.wrap(container, R.id.ecl).text(String.valueOf(index))
            .onClick(() -> selectEcl(index)).onLongClick(() -> selectEcl(0));
        if (Characters.isCloseECL(i, minPartyLevel, maxPartyLevel)) {
          ecl.get().setTypeface(Typeface.DEFAULT_BOLD);
        }
        if (selectedECL == i) {
          ecl.backgroundColor(R.color.colorAccent);
        }
        ecls.addView(container);
        eclViews.add(ecl);
        Wrapper.wrap(view, R.id.save).onClick(this::awardEcl);
      }

      characterContainer = view.findViewById(R.id.party);
      for (Character character : characters().getCampaignCharacters(campaign.get().getId())) {
        characterContainer.addView(new XPCharacterView(getContext(), this, character));
      }

      fixed1 = view.findViewById(R.id.fixed_1);
      fixed2 = view.findViewById(R.id.fixed_2);
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

  private void awardEcl() {
    if (campaign.isPresent()) {
      for (int i = 0; i < characterContainer.getChildCount(); i++) {
        XPCharacterView view = (XPCharacterView) characterContainer.getChildAt(i);
        if (view.getXP() > 0) {
          Message.createForXp(campaign.get().getContext(), view.getCharacter().getId(),
              view.getXP());
        }
      }
    }

    save();
  }

  private int fixedXP(LinearLayout layout) {
    int total = 0;
    for (int i = 0; i < layout.getChildCount(); i++) {
      XPFixedView view = (XPFixedView) layout.getChildAt(i);
      total += view.getValue();
    }

    return total;
  }

  private int fixedXP() {
    return fixedXP(fixed1) + fixedXP(fixed2);
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

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String campaignId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_CAMPAIGN_ID, campaignId);
    return arguments;
  }

  public static XPDialog newInstance(String campaignId) {
    XPDialog dialog = new XPDialog();
    dialog.setArguments(arguments(R.layout.dialog_xp,
        R.string.title_xp_rewards, R.color.campaign, campaignId));
    return dialog;
  }
}
