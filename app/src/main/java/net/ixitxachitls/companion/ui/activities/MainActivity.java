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

package net.ixitxachitls.companion.ui.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.data.Character;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.net.CompanionPublisher;
import net.ixitxachitls.companion.net.CompanionSubscriber;
import net.ixitxachitls.companion.ui.Setup;
import net.ixitxachitls.companion.ui.fragments.CampaignFragment;
import net.ixitxachitls.companion.ui.fragments.CampaignsFragment;
import net.ixitxachitls.companion.ui.fragments.CharacterFragment;
import net.ixitxachitls.companion.ui.fragments.CompanionFragment;
import net.ixitxachitls.companion.ui.fragments.EditCampaignFragment;
import net.ixitxachitls.companion.ui.fragments.EditFragment;
import net.ixitxachitls.companion.ui.fragments.SettingsFragment;

public class MainActivity extends CompanionActivity implements EditFragment.AttachAction {

  private static final String SAVE_FRAGMENT = "fragment";

  // UI elements.
  private TextView status;
  private ImageView online;
  private TextView onlineStatus;

  // Fragments.
  private static CompanionFragment currentFragment;
  private static CampaignFragment campaignFragment;
  private static CampaignsFragment campaignsFragment;
  private static SettingsFragment settingsFragment;
  private static CharacterFragment characterFragment;

  public void status(String message) {
    this.status.setText(message);
  }

  @Override
  protected void onCreate(@Nullable Bundle state) {
    super.onCreate(state);
    setup(state, R.layout.activity_main, R.string.app_name);
    View container = findViewById(R.id.activity_main);

    // Setup the status first, in case any fragment wants to set something.
    status = Setup.textView(container, R.id.status, null);
    online = Setup.imageView(container, R.id.online, this::toggleOnlineStatus);
    onlineStatus = Setup.textView(container, R.id.online_status, null);

    if (state == null || state.getString(SAVE_FRAGMENT) == null) {
      show();
    } else {
      show(CompanionFragment.Type.valueOf(state.getString(SAVE_FRAGMENT)));
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle bundle) {
    bundle.putString(SAVE_FRAGMENT, currentFragment.getType().toString());

    // TODO: also save this in settings?
  }

  public void show() {
    if (currentFragment != null) {
      show(currentFragment);
    }

    // Show the default campign to be able to come back to it.
    show(CompanionFragment.Type.campaigns);
    if (!Settings.get().isDefined()) {
      show(CompanionFragment.Type.settings);
    }
  }

  private void toggleOnlineStatus() {
    if (onlineStatus.getVisibility() == View.GONE) {
      onlineStatus.setVisibility(View.VISIBLE);
    } else {
      onlineStatus.setVisibility(View.GONE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_campaign_selection, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      show(CompanionFragment.Type.settings);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public Fragment show(CompanionFragment.Type fragment) {
    switch(fragment) {
      case settings:
        if (settingsFragment == null) {
          settingsFragment = new SettingsFragment();
        }
        return show(settingsFragment);

      case character:
        if (characterFragment == null) {
          characterFragment = new CharacterFragment();
        }
        return show(characterFragment);

      default:
      case campaigns:
        if (campaignsFragment == null) {
          campaignsFragment = new CampaignsFragment();
        }
        return show(campaignsFragment);

      case campaign:
        if (campaignFragment == null) {
          campaignFragment = new CampaignFragment();
        }
        return show(campaignFragment);
    }
  }

  private Fragment show(CompanionFragment fragment) {
    FragmentTransaction transaction = getFragmentManager()
        .beginTransaction();

    if (currentFragment != null) {
      transaction.addToBackStack(null);
    }

    transaction.replace(R.id.content, fragment).commit();
    getFragmentManager().executePendingTransactions();
    fragment.refresh();
    currentFragment = fragment;
    return fragment;
  }

  public void showCampaign(Campaign campaign) {
    show(CompanionFragment.Type.campaign);
    campaignFragment.showCampaign(campaign);
  }

  public void showCharacter(Character character) {
    show(CompanionFragment.Type.character);
    characterFragment.showCharacter(character);
  }

  public void showLast() {
    getFragmentManager().popBackStackImmediate();
    refresh();
  }

  public void attached(EditFragment fragment) {
    if (fragment instanceof EditCampaignFragment) {
      ((EditCampaignFragment)fragment).setSaveListener(this::saveCampaign);
    }
  }

  public void saveCampaign(Campaign campaign) {
    campaign.store();
    campaignsFragment.refresh();
  }

  @Override
  public void refresh() {
    online.setColorFilter(getResources().getColor(
        CompanionSubscriber.get().isOnline() || CompanionPublisher.get().isOnline()
            ? R.color.light : R.color.out, null));
    String publisherStatus = CompanionPublisher.get().getOnlineStatus();
    String subscriberStatus = CompanionSubscriber.get().getOnlineStatus();
    onlineStatus.setText(publisherStatus + "\n\n" + subscriberStatus);

    currentFragment.refresh();
  }
}
