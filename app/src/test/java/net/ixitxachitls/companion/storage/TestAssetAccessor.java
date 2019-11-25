/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An asset accessessor for the real assets that can be used in tests.
 */
public class TestAssetAccessor implements AssetAccessor {
  public static final String  PATH = "../app/src/main/assets/";

  public TestAssetAccessor() {
  }

  @Override
  public String[] list(String path) throws IOException {
    return new File(PATH + path).list();
  }

  @Override
  public InputStream open(String file) throws IOException {
    return new FileInputStream(PATH + file);
  }

  @Override
  public File getExternalFilesDir(String name) {
    // TODO(merlin): Might need to do something properly here...
    throw new UnsupportedOperationException();
  }
}

