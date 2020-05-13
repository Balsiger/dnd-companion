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

package net.ixitxachitls.companion.data;

import com.google.protobuf.MessageLite;

import net.ixitxachitls.companion.data.documents.TemplateFilter;
import net.ixitxachitls.companion.data.documents.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A template store that allows filtering.
 */
public class FilteredTemplatesStore
    <T extends Entry<? extends MessageLite>, F extends TemplateFilter> extends TemplatesStore<T> {

  protected List<T> configured = new ArrayList<>();
  protected List<T> filtered = new ArrayList<>();
  protected int filteredOwned = -1;
  protected F filter;

  protected FilteredTemplatesStore(Class<T> entryClass, F defaultFilter) {
    super(entryClass);

    this.filter = defaultFilter;
  }

  public F getFilter() {
    return filter;
  }

  public int getFilteredNumber() {
    return filtered.size();
  }

  public int getTotalNumber() {
    return configured.size();
  }

  public boolean isFiltered() {
    return filtered.size() != configured.size();
  }

  public void filter(User me, F filter) {
    this.filter = filter;
    filtered = configured.stream()
        .filter(f -> filter.matches(me, f))
        .collect(Collectors.toList());
    filteredOwned = -1;
  }

  public Optional<T> get(int index) {
    if (index >= 0 && filtered.size() > index) {
      return Optional.of(filtered.get(index));
    }

    return Optional.empty();
  }

  public int getFilteredOwnedNumber(User me) {
    if (filteredOwned < 0) {
      filteredOwned = computeFilteredOwned(me);
    }

    return filteredOwned;
  }

  public int getNumber(T miniature) {
    return filtered.indexOf(miniature) + 1;
  }

  ;

  @Override
  public void loaded() {
    configured.addAll(byName.values());
    filtered.addAll(configured);
  }

  protected int computeFilteredOwned(User me) {
    return 0;
  }
}
