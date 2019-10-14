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

package net.ixitxachitls.companion.data.documents;

import com.google.firebase.firestore.FirebaseFirestore;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

/**
 * Abstract base for all documents.
 */
public abstract class AbstractDocument<D extends AbstractDocument<D>> {

  protected final FirebaseFirestore db = FirebaseFirestore.getInstance();
  private final MutableLiveData<D> live = new MutableLiveData<>();
  @FunctionalInterface
  public interface Action {
    void execute();
  }

  public void observe(LifecycleOwner owner, Observer<D> observer) {
    unobserve(owner);
    live.observe(owner, observer);
  }

  public void observeForever(Observer<D> observer) {
    live.observeForever(observer);
  }

  public void unobserve(LifecycleOwner owner) {
    live.removeObservers(owner);
  }

  @SuppressWarnings("unchecked")
  protected void updated() {
    live.setValue((D) this);
  }
}
