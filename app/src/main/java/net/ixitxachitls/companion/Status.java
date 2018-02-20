/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

package net.ixitxachitls.companion;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.ui.views.StatusView;

import java.util.ArrayList;
import java.util.List;

/**
 * A statically accessible class to record the applications current status. As many components
 * might depend on this class, it should be initialized first.
 */
public class Status {
  private static Optional<StatusView> view = Optional.absent();
  private static List<Action> pendingActions = new ArrayList<>();

  public static void setView(StatusView view) {
    Status.view = Optional.of(view);
  }

  public static void clearView() {
    Status.view = Optional.absent();
  }

  public static void log(String message) {
    runInView(v -> v.addMessage(message));
  }

  public static void toast(String message) {
    runInView(v -> Toast.makeText(v.getContext(), message, Toast.LENGTH_LONG).show());
  }

  public static void addClientConnection(String id, String name) {
    runInView(v -> v.addClientConnection(id, name));
  }

  public static void addServerConnection(String id, String name) {
    runInView(v -> v.addServerConnection(id, name));
  }

  public static void refreshClientConnection(String id) {
    runInView(v -> v.updateClientConnection(id));
  }

  public static void refreshServerConnection(String id) {
    runInView(v -> v.updateServerConnection(id));
  }

  public static void removeClientConnection(String id) {
    runInView(v -> v.removeClientConnection(id));
  }

  public static void removeServerConnection(String id) {
    runInView(v -> v.removeServerConnection(id));
  }

  public static void heartBeat() {
    runInView(StatusView::heartBeat);
  }

  private static void runInView(Action action) {
    if (view.isPresent()) {
      new Handler(Looper.getMainLooper()).post(() -> action.execute(view.get()));

      if (!pendingActions.isEmpty()) {
        new Handler(Looper.getMainLooper()).post(() -> pendingActions.stream()
            .forEach(a -> a.execute(view.get())));
        pendingActions.clear();
      }
    } else {
      pendingActions.add(action);
    }
  }

  @FunctionalInterface
  public interface Action {
    void execute(StatusView view);
  }
}
