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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.proto.Entity;

public class CampaignActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private Campaign mCampaign;
  private ListProtoAdapter<Entity.CharacterProto> mCharactersAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mCampaign = Campaign.load(getApplicationContext(),
        getIntent().getLongExtra(DataBase.COLUMN_ID, 0)).or(new Campaign(0, "not found"));

    setContentView(R.layout.activity_campaign);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_character);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openCharacter(0);
      }
    });

    setTitle(mCampaign.getName());

    // Setup list view.
    ListView characters = (ListView) findViewById(R.id.charactersList);
    mCharactersAdapter = new ListProtoAdapter<>(this, R.layout.list_item_character,
        new ListProtoAdapter.OnItemClick<Entity.CharacterProto>() {
          @Override
          public void click(long id, Entity.CharacterProto proto) {
            ConfirmationDialog.show(CampaignActivity.this,
                getString(R.string.character_delete_title),
                getString(R.string.character_delete_message),
                new ConfirmationDialog.Callback() {
                  @Override
                  public void yes() {
                    getContentResolver().delete(DataBaseContentProvider.CHARACTERS, "id = " + id,
                        null);
                    getLoaderManager().restartLoader(0, null, CampaignActivity.this);
                    Toast.makeText(CampaignActivity.this, "The character has been deleted",
                        Toast.LENGTH_SHORT).show();
                  }

                  @Override
                  public void no() {
                    // nothing to do here
                  }
                });
          }
        },
        Entity.CharacterProto.getDefaultInstance(),
        new ListProtoAdapter.Binder<Entity.CharacterProto>() {
          @Override
          public void bind(View view, Entity.CharacterProto proto) {
            ((TextView) view.findViewById(R.id.text)).setText(proto.getName());
          }
        });
    characters.setAdapter(mCharactersAdapter);
    getLoaderManager().initLoader(0, null, this);

    characters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CampaignActivity.this.openCharacter(id);
      }
    });
  }

  private void openCharacter(long id) {
    Intent intent = new Intent(CampaignActivity.this, CharacterActivity.class);
    intent.putExtra(DataBase.COLUMN_ID, id);
    startActivity(intent);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(this, DataBaseContentProvider.CHARACTERS,
        DataBase.COLUMNS, null, null, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mCharactersAdapter.swapCursor(data);

    if(!mCharactersAdapter.isEmpty()) {
      findViewById(R.id.charactersTitle).setVisibility(View.VISIBLE);
      findViewById(R.id.charactersTitleEmpty).setVisibility(View.GONE);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mCharactersAdapter.swapCursor(null);
  }

  @Override
  public void onResume() {
    super.onResume();

    // Refresh the list view.
    getLoaderManager().restartLoader(0, null, this);
   }
}
