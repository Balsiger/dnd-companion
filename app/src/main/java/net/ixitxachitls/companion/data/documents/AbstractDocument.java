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

package net.ixitxachitls.companion.data.documents;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Abstract base for all documents.
 */
public abstract class AbstractDocument<D extends AbstractDocument<D>> {

  @FunctionalInterface
  public interface Action {
    void execute();
  }

  protected final FirebaseFirestore db = FirebaseFirestore.getInstance();
  private final MutableLiveData<D> live = new MutableLiveData<>();

  public void observeForever(Observer<D> observer) {
    live.observeForever(observer);
  }

  public void observe(LifecycleOwner owner, Observer<D> observer) {
    unobserve(owner);
    live.observe(owner, observer);
  }

  public void unobserve(LifecycleOwner owner) {
    live.removeObservers(owner);
  }

  protected void updated() {
    live.setValue((D) this);
  }
}
