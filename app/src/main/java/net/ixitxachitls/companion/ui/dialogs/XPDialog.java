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

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
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
import net.ixitxachitls.companion.rules.XP;

import java.util.List;

/**
 * Dialog for selecting XP rewards.
 */
public class XPDialog extends Dialog {

  private static final String ARG_CAMPAIGN_ID = "campaign_id";
  private static final int MAX_ECL = 30;

  private Optional<Campaign> campaign = Optional.absent();

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
    campaign = Campaigns.getCampaign(getArguments().getString(ARG_CAMPAIGN_ID));
  }

  @Override
  protected void createContent(View view) {
    ViewGroup ecls = (ViewGroup) view.findViewById(R.id.ecls);
    LayoutInflater inflator = LayoutInflater.from(getContext());
    for (int i = 1; i <= MAX_ECL; i++) {
      final int index = i;
      LinearLayout container = (LinearLayout) inflator.inflate(R.layout.view_ecl, null);
      TextView ecl = (TextView) container.findViewById(R.id.ecl);
      ecl.setText(String.valueOf(index));
      ecl.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          awardEcl(index);
        }
      });
      ecls.addView(container);
    }
  }

  private void awardEcl(int level) {
    if (campaign.isPresent()) {
      List<Character> characters = campaign.get().getCharacters();
      int partySize = characters.size();
      for (Character character : characters) {
        int xp = XP.xpAward(level, character.getLevel(), partySize);
        campaign.get().awardXp(character, xp);
      }

      save();
    }
  }
}
