/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import net.ixitxachitls.companion.ui.views.StatusView;
import net.ixitxachitls.companion.util.Misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A statically accessible class to record the applications current status. As many components
 * might depend on this class, it should be initialized first.
 */
public class Status {
  private static Optional<StatusView> view = Optional.empty();
  private static List<Action> pendingActions = new ArrayList<>();

  public static void setView(StatusView view) {
    Status.view = Optional.of(view);
  }

  public static void clearView() {
    Status.view = Optional.empty();
  }

  public static void log(String message) {
    Log.d("Status", message);
    if (Misc.IN_UNIT_TEST) {
      System.out.println(message);
    }
    runInView(v -> v.addMessage(message));
  }

  public static void exception(String message, Exception e) {
    Log.e("Status", message, e);
    if (Misc.IN_UNIT_TEST) {
      System.out.println(message);
      e.printStackTrace();
    }
    runInView(v -> v.showException(message, e));
  }

  public static void warning(String message) {
    Log.w("Status", message);
    if (Misc.IN_UNIT_TEST) {
      System.out.println(message);
    }
    runInView(v -> v.addWarningMessage(message));
  }

  public static void error(String message) {
    Log.e("Status", message);
    if (Misc.IN_UNIT_TEST) {
      System.out.println(message);
    }
    toastOnly(message);
    runInView(v -> v.addErrorMessage(message));
  }

  public static void toast(String message) {
    toastOnly(message);
    log(message);
  }

  private static void toastOnly(String message) {
    runInView(v -> Toast.makeText(v.getContext(), message, Toast.LENGTH_LONG).show());
  }

  private static void runInView(Action action) {
    if (view.isPresent()) {
      if (!pendingActions.isEmpty()) {
        new Handler(Looper.getMainLooper()).post(() -> {
          pendingActions.forEach(a -> a.execute(view.get()));
          pendingActions.clear();
        });
      }

      new Handler(Looper.getMainLooper()).post(() -> action.execute(view.get()));
    } else {
      pendingActions.add(action);
    }
  }

  public static void toggleDebug() {
    if (view.isPresent()) {
      view.get().toggleDebug();
    }
  }

  @FunctionalInterface
  public interface Action {
    void execute(StatusView view);
  }
}
