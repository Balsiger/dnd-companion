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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.ui.Setup;

public class SettingsActivity extends Activity {

  private Settings settings;
  private TextView nickname;
  private Button save;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setup(savedInstanceState, R.layout.activity_settings, R.string.settings_title);
    View container = findViewById(R.id.settings_content);

    settings = Settings.get();
    nickname = Setup.editText(container, R.id.nickname, settings.getNickname(),
        R.string.settings_nickname_label, R.color.colorAccent,
        this::editNickname, this::update);
    save = Setup.button(container, R.id.save, this::save);

    if (settings.isDefined()) {
      container.findViewById(R.id.initial).setVisibility(View.INVISIBLE);
    }

    update();
  }

  private void save() {
    editNickname();

    if (settings.isDefined()) {
      Intent intent = new Intent(this, MainActivity.class);
      this.startActivity(intent);
    }
  }

  private void editNickname() {
    settings.setNickname(nickname.getText().toString());
  }

  private void update() {
    if (nickname.getText().length() > 0) {
      save.setVisibility(View.VISIBLE);
    } else {
      save.setVisibility(View.INVISIBLE);
    }
  }
}
