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

package net.ixitxachitls.companion.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.FSCampaign;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/**
 * Dialog to invite players to a campaign.
 */
public class InviteDialog extends Dialog {

  private static final String ARG_ID = "id";

  private Optional<FSCampaign> campaign = Optional.empty();
  private LinearLayout invites;
  private LabelledEditTextView email;
  private Wrapper<Button> invite;

  public static InviteDialog newInstance(String campaignId) {
    InviteDialog dialog = new InviteDialog();
    dialog.setArguments(arguments(R.layout.dialog_invite, R.string.title_invite, R.color.campaign,
        campaignId));
    return dialog;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String campaignId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, campaignId);
    return arguments;
  }

  @Override
  protected void createContent(View view) {
    invites = view.findViewById(R.id.invites);
    email = view.findViewById(R.id.email);
    invite = Wrapper.<Button>wrap(view, R.id.invite).onClick(this::invite);

    campaign = application().fsCampaigns().getCampaign(getArguments().getString(ARG_ID));
    refresh();
  }

  private void refresh() {
    invites.removeAllViews();
    if (campaign.isPresent()) {
      for (String invite : campaign.get().getInvites()) {
        invites.addView(new LineView(getContext(), invite));
      }
    }
  }

  private void invite() {
    if (campaign.isPresent()) {
      campaign.get().invite(email.getText());
      refresh();
    }
  }

  private void uninvite(String email) {
    if (campaign.isPresent()) {
      campaign.get().uninvite(email);
      refresh();
    }
  }

  private class LineView extends LinearLayout {
    private final String email;

    private LineView(Context context, String email) {
      super(context);

      View view =
          LayoutInflater.from(getContext()).inflate(R.layout.dialog_invited_line, this, false);

      this.email = email;
      TextWrapper.<TextView>wrap(view, R.id.email).text(email);
      Wrapper.wrap(view, R.id.uninvite).onClick(this::uninvite);

      addView(view);
    }

    private void uninvite() {
      InviteDialog.this.uninvite(email);
    }
  }
}
