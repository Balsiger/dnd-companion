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

package net.ixitxachitls.companion.net;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.os.Handler;
import android.support.multidex.MultiDexApplication;

import net.ixitxachitls.companion.data.FakeData;
import net.ixitxachitls.companion.net.nsd.FakeNsdAccessor;
import net.ixitxachitls.companion.storage.FakeClient1DataBaseAccessor;
import net.ixitxachitls.companion.storage.FakeClient2DataBaseAccessor;
import net.ixitxachitls.companion.storage.FakeDataBaseAccessor;
import net.ixitxachitls.companion.storage.FakeServerDataBaseAccessor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * End 2 end test for the whole network stack.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = MultiDexApplication.class)
public class NetworkTest {

  // Make .setValue calls synchronous and allow them to be called in tests.
  @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

  protected final FakeNsdAccessor serverNsd = new FakeNsdAccessor();
  protected final FakeDataBaseAccessor serverDataBase = new FakeServerDataBaseAccessor();
  protected final FakeData serverData = new FakeData("Server", "server", serverDataBase);

  protected final FakeNsdAccessor client1Nsd = new FakeNsdAccessor();
  protected final FakeDataBaseAccessor client1DataBase = new FakeClient1DataBaseAccessor();
  protected final FakeData client1Data = new FakeData("Client 1", "client1", client1DataBase);

  protected final FakeNsdAccessor client2Nsd = new FakeNsdAccessor();
  protected final FakeDataBaseAccessor client2DataBase = new FakeClient2DataBaseAccessor();
  protected final FakeData client2Data = new FakeData("Client 2", "client2", client2DataBase);

  @Before
  public void setUp() {
  }

  @Test
  public void welcome() {
    CompanionMessenger server =
        new CompanionMessenger(serverData, serverNsd, null, new Handler());
    server.start();
    CompanionMessenger client1 =
        new CompanionMessenger(client1Data, client1Nsd, null, new Handler());
    client1.start();
    CompanionMessenger client2 =
        new CompanionMessenger(client2Data, client2Nsd, null, new Handler());
    client2.start();


  }
}
