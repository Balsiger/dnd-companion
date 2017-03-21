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

package net.ixitxachitls.companion.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.proto.Entity;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.ui.activities.SettingsActivity;
import net.ixitxachitls.companion.ui.edit.EditTextFragment;

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private ListProtoAdapter<Entity.CampaignProto> campaignsAdapter;
  private Settings settings;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    Entries.init(this);
    settings = Settings.load(this).or(new Settings(""));

    // Add campaign button.
    FloatingActionButton button = (FloatingActionButton) findViewById(R.id.addCampaign);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        EditTextFragment edit = EditTextFragment.newInstance(R.string.campaign_add_title,
            R.string.campaign_edit_name, "", R.color.colorCampaign);
        edit.setListener(MainActivity.this::onEdit);
        edit.display(getFragmentManager());
      }
    });

    // Setup list view.
    ListView campaigns = (ListView) findViewById(R.id.campaignsList);
    campaignsAdapter = new ListProtoAdapter<>(this, R.layout.list_item_campaign,
        new ListProtoAdapter.OnItemClick<Entity.CampaignProto>() {
          @Override
          public void click(long id, Entity.CampaignProto proto) {
            ConfirmationDialog.show(MainActivity.this,
                getResources().getString(R.string.campaign_delete_title),
                getResources().getString(R.string.campaign_delete_message),
                new ConfirmationDialog.Callback() {
                  @Override
                  public void yes() {
                    getContentResolver().delete(DataBaseContentProvider.CAMPAIGNS,
                        "id = " + id, null);
                    getLoaderManager().restartLoader(0, null, MainActivity.this);
                    Toast.makeText(MainActivity.this, "The campaign has been deleted",
                        Toast.LENGTH_SHORT).show();
                  }

                  @Override
                  public void no() {
                    // nothing to do here
                  }
                });
          }
        },
        Entity.CampaignProto.getDefaultInstance(),
        new ListProtoAdapter.Binder<Entity.CampaignProto>() {
          @Override
          public void bind(View view, Entity.CampaignProto proto) {
            ((TextView) view.findViewById(R.id.text)).setText(proto.getName());
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

  public void onEdit(String name) {
    Log.d("Campaign", "edited campaign to " + name);

    // Add the name to storage.
    getContentResolver().insert(DataBaseContentProvider.CAMPAIGNS,
        new Campaign(0, name).toValues());

    // Refresh the list view.
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

    if(!campaignsAdapter.isEmpty()) {
      findViewById(R.id.campaignsTitle).setVisibility(View.VISIBLE);
      findViewById(R.id.campaignsTitleEmpty).setVisibility(View.GONE);
    }

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
}
