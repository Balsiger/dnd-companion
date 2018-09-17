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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.FSCampaign;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * View for a campaign title.
 */
public class CampaignTitleView extends LinearLayout {

  private FSCampaign campaign ;

  // Ui elements.
  private TitleView title;
  private TextWrapper<TextView> dm;

  public CampaignTitleView(Context context) {
    super(context);

    setup();
  }

  public CampaignTitleView(Context context, AttributeSet attributes) {
    super(context, attributes);

    setup();
  }

  public void addTo(ViewGroup group) {
    ViewGroup parent = (ViewGroup) getParent();
    if (parent != null) {
      parent.removeView(this);
    }

    group.addView(this);
  }

  private void setup() {
    RelativeLayout view = (RelativeLayout)
        LayoutInflater.from(getContext()).inflate(R.layout.view_campaign_title, this, false);
    // Adding the margin in the xml layout does not work.
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 0, 0, 2);
    view.setLayoutParams(params);

    title = view.findViewById(R.id.title);
    dm = TextWrapper.wrap(view, R.id.dm);
    dm.description("DM", "This indicates that you are the Dungeon Master of the campaign. You are "
        + "the only person that can make changes to the values and settings of the campaign.");

    addView(view);
  }

  public void update(FSCampaign campaign) {
    this.campaign = campaign;
    refresh();
  }

  public void update(User dm) {
    refresh();
  }

  public void setAction(Wrapper.Action action) {
    title.setAction(action);
  }

  public void removeAction() {
    title.removeAction();
  }


  public void refresh() {
    if (campaign != null) {
      title.setTitle(campaign.getName());
      title.setSubtitle(campaign.getWorld().toString() + ", " + campaign.getDm());
      dm.visible(campaign.amDM());
    }
  }

  @Deprecated
  public void update(Campaign campaign) {
  }
}
