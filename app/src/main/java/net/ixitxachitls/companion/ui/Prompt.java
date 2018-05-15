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

package net.ixitxachitls.companion.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Base prompt for all other prompts.
 */
public class Prompt<T extends Prompt> {

  protected final AlertDialog.Builder dialog;

  public Prompt(Context context) {
    this.dialog = new AlertDialog.Builder(context);
  }

  public T title(String title) {
    dialog.setTitle(title);
    return (T) this;
  }

  public T message(String message) {
    dialog.setMessage(message);
    return (T) this;
  }

  public T yes(Action action) {
    dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int whichButton) {
        action.execute();
      }
    });
    return (T) this;
  }

  public T no(Action action) {
    dialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int whichButton) {
        action.execute();
      }
    });
    return (T) this;
  }

  public T noNo() {
    dialog.setNegativeButton(null, null);
    return (T) this;
  }

  public AlertDialog show() {
    return this.dialog.show();
  }


  @FunctionalInterface
  public interface Action {
    public void execute();
  }

  @FunctionalInterface
  @Deprecated
  public interface YesAction {
    public void yes();
  }

  @FunctionalInterface
  @Deprecated
  public interface NoAction {
    public void no();
  }

  public static ConfirmationPrompt create(Context context) {
    return new ConfirmationPrompt(context);
  }
}
