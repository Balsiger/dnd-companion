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

package net.ixitxachitls.companion.data.dynamics;

import android.support.annotation.CallSuper;
import android.support.multidex.MultiDexApplication;

import net.ixitxachitls.companion.CompanionTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Tests for characters.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = MultiDexApplication.class)
public class CharacterTest extends CompanionTest {

  @Before
  @Override
  @CallSuper
  public void setUp() {
    super.setUp();
  }

  @Test
  public void deleteLocal() throws InterruptedException, IOException {
    server.start();
    client1.start();

    assertThat(characterNames(serverContext),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));
    assertThat(characterNames(client1Context),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));

    character(serverContext, "character-server-1").delete();
    processAllMessages(server, client1);

    assertThat(characterNames(serverContext),
        containsInAnyOrder("Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));
    assertThat(characterNames(client1Context),
        containsInAnyOrder("Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));

    // Got deleted above.
    assetAccessor.getExternalFilesDir("characters-local/character-server-1.jpg").createNewFile();
  }

  @Test
  public void deleteRemote() throws InterruptedException, IOException {
    server.start();
    client1.start();

    assertThat(characterNames(serverContext),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));
    assertThat(characterNames(client1Context),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));

    // Process all messages first to ensure that characters are initially sent.
    processAllMessages(server, client1);
    character(client1Context, "character-server-1").delete();
    processAllMessages(server, client1);

    assertThat(characterNames(serverContext),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));
    assertThat(characterNames(client1Context),
        containsInAnyOrder("Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));

    // Got deleted above.
    assetAccessor.getExternalFilesDir("characters-local/character-server-1.jpg").createNewFile();
  }
}