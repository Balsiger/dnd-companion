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

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.ui.CampaignActivity;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.ListProtoAdapter;
import net.ixitxachitls.companion.ui.Setup;
import net.ixitxachitls.companion.ui.fragments.EditCampaignFragment;
import net.ixitxachitls.companion.ui.fragments.EditFragment;

public class MainActivity extends Activity
    implements LoaderManager.LoaderCallbacks<Cursor>, EditFragment.AttachAction {

  private ListProtoAdapter<Data.CampaignProto> campaignsAdapter;
  private Settings settings;

  private void init() {
    Entries.init(this);
    Settings.init(this);

    settings = Settings.get();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setup(savedInstanceState, R.layout.activity_main, R.string.app_name);
    View container = findViewById(R.id.activity_main);

    init();

    Setup.floatingButton(container, R.id.campaign_add, this::addCampaign);

    // Setup list view.
    ListView campaigns = (ListView) findViewById(R.id.campaignsList);
    campaignsAdapter = new ListProtoAdapter<>(this, R.layout.list_item_campaign,
        Data.CampaignProto.getDefaultInstance(),
        new ListProtoAdapter.ContentCreator<Data.CampaignProto>() {
          @Override
          public void create(View view, long id, Data.CampaignProto proto) {
            Campaign campaign = Campaign.fromProto(id, proto);
            ((TextView) view.findViewById(R.id.name)).setText(campaign.getName());
            ((TextView) view.findViewById(R.id.world)).setText(campaign.getWorld());
            if (campaign.isLocal()) {
              view.findViewById(R.id.local).setVisibility(View.VISIBLE);
              view.findViewById(R.id.remote).setVisibility(View.GONE);
              view.findViewById(R.id.publish).setVisibility(View.VISIBLE);
            } else {
              view.findViewById(R.id.local).setVisibility(View.GONE);
              view.findViewById(R.id.remote).setVisibility(View.VISIBLE);
              view.findViewById(R.id.publish).setVisibility(View.GONE);
            }

            Setup.switchButton(view, R.id.publish, (w) -> publishCampaign(w, id));
          }
        });
    campaigns.setAdapter(campaignsAdapter);
    getLoaderManager().initLoader(0, null, this);

    campaigns.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(MainActivity.this, CampaignActivity.class);
        intent.putExtra(DataBase.COLUMN_ID, id);
        MainActivity.this.startActivity(intent);
      }
    });
  }

  private boolean publishing = false;
  private void publishCampaign(Switch widget, long id) {
    if (publishing) {
      return;
    }

    if (widget.isChecked()) {
      ConfirmationDialog.show(this,
          getString(R.string.main_campaign_publish_title),
          getString(R.string.main_campaign_publish_message),
          new ConfirmationDialog.Callback() {
            @Override
            public void yes() {

            }

            @Override
            public void no() {
              publishing = true;
              widget.toggle();
              publishing = false;
            }
          });
    } else {
      ConfirmationDialog.show(this,
          getString(R.string.main_campaign_unpublish_title),
          getString(R.string.main_campaign_unpublish_message),
          new ConfirmationDialog.Callback() {
            @Override
            public void yes() {
            }

            @Override
            public void no() {
              publishing = true;
              widget.toggle();
              publishing = false;
            }
          });
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
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      gotoSettings();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void refresh() {
    getLoaderManager().restartLoader(0, null, this);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(this, DataBaseContentProvider.CAMPAIGNS,
        DataBase.COLUMNS, null, null, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    campaignsAdapter.swapCursor(data);

    // Go to other activities if needed.
    if (!settings.isDefined()) {
      gotoSettings();
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    campaignsAdapter.swapCursor(null);
  }

  private void gotoSettings() {
    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
    MainActivity.this.startActivity(intent);
  }

  private void addCampaign() {
    EditCampaignFragment.newInstance().display(getFragmentManager());
  }

  public void attached(EditFragment fragment) {
    if (fragment instanceof EditCampaignFragment) {
      ((EditCampaignFragment)fragment).setSaveListener(this::saveCampaign);
    }
  }

  public void saveCampaign(Campaign campaign) {
    campaign.store();
    refresh();
  }
}
