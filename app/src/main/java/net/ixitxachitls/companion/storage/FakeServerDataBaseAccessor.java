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

package net.ixitxachitls.companion.storage;

/**
 * Fake data for the serer.
 */
public class FakeServerDataBaseAccessor extends FakeDataBaseAccessor {

  public FakeServerDataBaseAccessor() {
    // Setup the default data for the cursor.
    add(settingsById, settings("server", "Server", 23));

    add(localCampaignsById,
        campaign("campaign-server-1", "Superheroes", "Generic", true));
    add(localCharactersById,
        character("character-server-1", "Conan", "campaign-server-1"));
    add(remoteCharactersById,
        character("character-client1-3", "Hulk", "campaign-server-1"));
    add(remoteCharactersById,
        character("character-client2-5", "Wonder Woman", "campaign-server-1"));
    add(remoteCharactersById,
        character("character-client3-2", "Black Widow", "campaign-server-1"));

    add(localCampaignsById,
        campaign("campaign-server-2", "Test Campaign FR", "Forgotten Realms", false));
    add(localCampaignsById,
        campaign("campaign-server-3", "Test Campaign 2", "Generic", true));

    add(remoteCampaignsById,
        campaign("campaign-client1-3", "Cormy", "Forgotten Realms", false));
    add(localCharactersById,
        character("character-server-2", "Elminster", "campaign-client1-3"));
    add(remoteCharactersById,
        character("character-client1-4", "Khelben Blackstaff", "campaign-client1-3"));
    add(remoteCharactersById,
        character("character-client2-3", "Laeral Silverhand", "campaign-client1-3"));
    add(remoteCharactersById,
        character("character-client3-3", "The Simbul", "campaign-client1-3"));

    add(remoteCampaignsById,
        campaign("campaign-client2-4", "Campaign Test FR", "Forgotten Realms", false));
    add(remoteCampaignsById,
        campaign("campaign-client3-1", "Campaign Test 2", "Generic", false));
  }
}
