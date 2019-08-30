/*
 * Copyright (c) 2017-2018 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
 *
 * The Roleplay Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Roleplay Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.data.documents.Data;

import java.util.Map;

/**
 * A generic, free text adjustment.
 */
public class GenericAdjustment extends Adjustment {

  private static final String FIELD_TEXT = "text";

  private final String text;

  public GenericAdjustment(String text) {
    super(Type.generic);
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }

  @Override
  public Map<String, Object> write() {
    Map<String, Object> data = super.write();
    data.put(FIELD_TEXT, text);

    return data;
  }

  public static GenericAdjustment readGeneric(Data data) {
    return new GenericAdjustment(data.get(FIELD_TEXT, ""));
  }
}
