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

import android.support.annotation.CallSuper;

import com.google.firebase.firestore.DocumentReference;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.MiniatureTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * The data for a single user.
 */
public class User extends Document<User> {

  protected static final String PATH = "users";
  private static final String MINIATURE_PATH = "/miniatures/miniatures";
  private static final Factory FACTORY = new Factory();
  private static final String FIELD_NICKNAME = "nickname";
  private static final String DEFAULT_NICKNAME = "";
  private static final String FIELD_PHOTO_URL = "photoUrl";
  private static final String FIELD_FEATURES = "features";
  private static final String FIELD_MINIATURE_OWNED = "owned";
  private static final String FIELD_MINIATURE_LOCATIONS = "locations";
  private static final String FIELD_MINIATURE_HIDDEN_SETS = "hidden-sets";

  private String nickname;
  private String photoUrl = "";
  private List<String> campaigns = new ArrayList<>();
  private List<String> features = new ArrayList<>();
  private Map<String, Long> miniatures = new HashMap<>();
  private Optional<DocumentReference> miniaturesDocument = Optional.empty();
  private boolean miniaturesLoading = false;
  private SortedMap<String, MiniatureLocation> locations = new TreeMap<>();
  private Set<String> hiddenSets = new HashSet<>();

  public List<String> getCampaigns() {
    return campaigns;
  }

  public List<String> getFeatures() {
    return features;
  }

  public void setFeatures(List<String> features) {
    this.features = new ArrayList<>(features);
  }

  public List<String> getLocationNames() {
    List<String> values = new ArrayList<>();
    values.add("");
    values.addAll(locations.keySet());
    return values;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public List<String> getOwnedValues() {
    SortedSet<Long> owned = new TreeSet(miniatures.values());
    owned.add(0L);
    return new ArrayList<>(owned.stream().map(String::valueOf).collect(Collectors.toList()));
  }

  public String getPhotoUrl() {
    return photoUrl;
  }

  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
  }

  public SortedSet<MiniatureLocation> getSortedLocations() {
    return new TreeSet<>(locations.values());
  }

  public void setHiddenSets(List<String> sets) {
    hiddenSets.clear();
    hiddenSets.addAll(sets);

    writeMiniatures();
  }

  public static boolean isUser(String id) {
    return id.matches(PATH + "/[^/]*/");
  }

  public boolean amDM(String id) {
    // Campaigns start with the user id.
    if (isA(id, Campaigns.PATH) && id.startsWith(getId())) {
      return true;
    }

    // Characters an anything below it need an indirection to the campaign.
    if (isA(id, Characters.PATH)) {
      Optional<Character> character =
          CompanionApplication.get().context().characters().get(
              id.replaceAll("(/" + Characters.PATH + "/.*?)/.*$", "$1"));
      return character.isPresent() && character.get().amDM();
    }

    return false;
  }

  public boolean amPlayer(String id) {
    return id.startsWith(getId());
  }

  public void deleteLocation(String name) {
    locations.remove(name);
    writeMiniatures();
  }

  public Optional<MiniatureLocation> getLocation(String location) {
    return Optional.ofNullable(locations.get(location));
  }

  public long getMiniatureCount(String name) {
    return miniatures.getOrDefault(name, 0L);
  }

  public boolean hasSetHidden(String name) {
    return hiddenSets.contains(name);
  }

  public String locationFor(MiniatureTemplate template) {
    SortedMap<MiniatureFilter, String> filteredLocations = new TreeMap<MiniatureFilter, String>();
    for (MiniatureLocation location : locations.values()) {
      Optional<MiniatureFilter> matches = location.matches(this, template);
      if (matches.isPresent()) {
        filteredLocations.put(matches.get(), location.getName());
      }
    }

    if (filteredLocations.isEmpty()) {
      return "";
    }

    return filteredLocations.values().iterator().next();
  }

  public boolean owns(String id) {
    return id.startsWith(getId());
  }

  public void readMiniatures(Callback callback) {
    if (miniaturesLoading) {
      callback.done();
      return;
    }

    miniaturesLoading = true;
    if (!miniaturesDocument.isPresent()) {
      miniaturesDocument = Optional.of(db.document(getId() + MINIATURE_PATH));
    }

    miniaturesDocument.get().get().addOnCompleteListener(task -> {
      miniaturesLoading = false;
      if (task.isSuccessful() ) {
        miniaturesDocument = Optional.of(task.getResult().getReference());
        Object data = task.getResult().get(FIELD_MINIATURE_OWNED);
        if (data != null) {
          if (miniatures.isEmpty()) {
            miniatures.putAll((Map<String, Long>) data);
          } else {
            Map<String, Long> existing = new HashMap<>(miniatures);
            miniatures.putAll((Map<String, Long>) data);
            miniatures.putAll(existing);
            writeMiniatures();
          }
        }

        data = task.getResult().get(FIELD_MINIATURE_LOCATIONS);
        if (data != null) {
          for (MiniatureLocation location : ((List<Map<String, Object>>) data).stream()
              .map(MiniatureLocation::read).collect(Collectors.toList())) {
            locations.put(location.getName(), location);
          }
        }

        data = task.getResult().get(FIELD_MINIATURE_HIDDEN_SETS);
        if (data != null) {
          hiddenSets.addAll((List<String>) data);
        }

        verifyMiniatures();
        callback.done();
      }
    });
  }

  public void replaceLocation(String originalName, MiniatureLocation location) {
    locations.remove(originalName);
    locations.put(location.getName(), location);

    writeMiniatures();
   }

  public void setMiniatureCount(String miniature, long count) {
    if (miniatures.getOrDefault(miniature, 0L) != count) {
      if (count == 0) {
        miniatures.remove(miniature);
      } else{
        miniatures.put(miniature, count);
      }
      writeMiniatures();
    }
  }

  @Override
  public String toString() {
    return getNickname();
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();
    nickname = get(FIELD_NICKNAME, DEFAULT_NICKNAME);
    photoUrl = get(FIELD_PHOTO_URL, "");
    features = get(FIELD_FEATURES, new ArrayList<>());
  }

  @Override
  protected Map<String, Object> write(Map<String, Object> data) {
    data.put(FIELD_NICKNAME, nickname);
    data.put(FIELD_PHOTO_URL, photoUrl);
    data.put(FIELD_FEATURES, features);
    return data;
  }

  private void readCampaigns() {
    context.invites().listenCampaigns(campaigns -> {
      this.campaigns = campaigns;
      updated(this);
    });
  }

  private void verifyMiniatures() {
    boolean toasted = false;
    for (String miniature : miniatures.keySet()) {
      if (!Templates.get().getMiniatureTemplates().get(miniature).isPresent()) {
        Templates.get().getMiniatureTemplates().addDummy(miniature);
        if (!toasted) {
          Status.error("Cannot find owned miniature " + miniature);
          // Only show the first missing to avoid toast spamming!
          toasted = true;
        }
      }
    }
  }

  private void writeMiniatures() {
    if (miniaturesLoading || !miniaturesDocument.isPresent()) {
      return;
    }

    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_MINIATURE_OWNED, miniatures);
    data.put(FIELD_MINIATURE_LOCATIONS, locations.values().stream()
        .map(MiniatureLocation::write).collect(Collectors.toList()));
    data.put(FIELD_MINIATURE_HIDDEN_SETS, new ArrayList<>(hiddenSets));

    miniaturesDocument.get().update(data);
  }

  protected static User getOrCreate(CompanionContext context, String id) {
    User user = Document.getOrCreate(FACTORY, context, PATH + "/" + id);
    user.whenReady(() -> {
      user.readCampaigns();
      context.users().updated(id);
    });

    return user;
  }

  private static class Factory implements DocumentFactory<User> {
    @Override
    public User create() {
      return new User();
    }
  }
}
