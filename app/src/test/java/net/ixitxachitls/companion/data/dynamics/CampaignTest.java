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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * Tests for campaigns.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = MultiDexApplication.class)
public class CampaignTest extends CompanionTest {

  @Before @Override @CallSuper
  public void setUp() {
    super.setUp();
  }

  @Test
  public void deleteLocal() throws InterruptedException {
    server.start();
    client1.start();

    assertThat(campaignNames(serverContext),
        containsInAnyOrder("Default Campaign", "Superheroes", "Test Campaign FR", "Test Campaign 2",
            "Cormyr", "Campaign Test FR", "Campaign Test 2"));
    assertThat(characterNames(serverContext),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));
    assertThat(campaignNames(client1Context),
        containsInAnyOrder("Default Campaign", "Superheroes", "Test Campaign FR", "Test Campaign 2",
            "Cormyr", "Campaign Test FR", "Campaign Test 2"));
    assertThat(characterNames(client1Context),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));

    //campaign(client1Context, "campaign-server-2").delete();
    processAllMessages(server, client1);

    assertThat(campaignNames(serverContext),
        containsInAnyOrder("Default Campaign", "Superheroes", "Test Campaign FR", "Test Campaign 2",
            "Cormyr", "Campaign Test FR", "Campaign Test 2"));
    assertThat(characterNames(serverContext),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));
    assertThat(campaignNames(client1Context),
        containsInAnyOrder("Default Campaign", "Superheroes", "Test Campaign 2",
            "Cormyr", "Campaign Test FR", "Campaign Test 2"));
    assertThat(characterNames(client1Context),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));

  }

  @Test
  public void deleteRemote() throws InterruptedException {
    server.start();
    client1.start();

    assertThat(campaignNames(serverContext),
        containsInAnyOrder("Default Campaign", "Superheroes", "Test Campaign FR", "Test Campaign 2",
            "Cormyr", "Campaign Test FR", "Campaign Test 2"));
    assertThat(characterNames(serverContext),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));
    assertThat(campaignNames(client1Context),
        containsInAnyOrder("Default Campaign", "Superheroes", "Test Campaign FR", "Test Campaign 2",
            "Cormyr", "Campaign Test FR", "Campaign Test 2"));
    assertThat(characterNames(client1Context),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));

    //campaign(serverContext, "campaign-client1-3").delete();
    processAllMessages(server, client1);

    assertThat(campaignNames(serverContext),
        containsInAnyOrder("Default Campaign", "Superheroes", "Test Campaign FR", "Test Campaign 2",
            "Campaign Test FR", "Campaign Test 2"));
    assertThat(characterNames(serverContext),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow", "Elminster"));
    assertThat(campaignNames(client1Context),
        containsInAnyOrder("Default Campaign", "Superheroes", "Test Campaign FR", "Test Campaign 2",
            "Cormyr", "Campaign Test FR", "Campaign Test 2"));
    assertThat(characterNames(client1Context),
        containsInAnyOrder("Conan", "Hulk", "Wonder Woman", "Black Widow",
            "Elminster", "Khelben Blackstaff Arunsun", "Laeral Silverhand", "The Simbul"));
  }
}