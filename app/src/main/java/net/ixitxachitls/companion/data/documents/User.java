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

import com.google.firebase.firestore.DocumentReference;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.MiniatureTemplate;

import java.util.ArrayList;
import java.util.Collections;
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

import androidx.annotation.CallSuper;

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
    SortedSet<Long> owned = new TreeSet<>(miniatures.values());
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

    storeMiniatures();
    Templates.get().getMiniatureTemplates().updateSets(this, hiddenSets);
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
    storeMiniatures();
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
        Data data = Data.fromSnapshot(task.getResult());
        if (miniatures.isEmpty()) {
          miniatures.putAll(data.getMap(FIELD_MINIATURE_OWNED, 0L));
        } else {
          Map<String, Long> existing = new HashMap<>(miniatures);
          miniatures.putAll(data.getMap(FIELD_MINIATURE_OWNED, 0L));
          miniatures.putAll(existing);
          storeMiniatures();
        }

        for (MiniatureLocation location :
            data.getNestedList(FIELD_MINIATURE_LOCATIONS)
                .stream()
                .map(MiniatureLocation::read)
                .collect(Collectors.toList())) {
          locations.put(location.getName(), location);
        }

        hiddenSets.addAll(data.getList(FIELD_MINIATURE_HIDDEN_SETS, Collections.emptyList()));

        verifyMiniatures();
        callback.done();
      }
    });
  }

  public void replaceLocation(String originalName, MiniatureLocation location) {
    locations.remove(originalName);
    locations.put(location.getName(), location);

    storeMiniatures();
   }

  public void setMiniatureCount(String miniature, long count) {
    if (miniatures.getOrDefault(miniature, 0L) != count) {
      if (count == 0) {
        miniatures.remove(miniature);
      } else{
        miniatures.put(miniature, count);
      }
      storeMiniatures();
    }
  }

  @Override
  public String toString() {
    return getNickname();
  }

  public Data writeMiniatures() {
    return Data.empty()
        .set(FIELD_MINIATURE_OWNED, miniatures)
        .setNested(FIELD_MINIATURE_LOCATIONS, locations.values())
        .set(FIELD_MINIATURE_HIDDEN_SETS, new ArrayList<>(hiddenSets));
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();
    nickname = data.get(FIELD_NICKNAME, DEFAULT_NICKNAME);
    photoUrl = data.get(FIELD_PHOTO_URL, "");
    features = data.getList(FIELD_FEATURES, new ArrayList<>());
  }

  @Override
  public Data write() {
    return Data.empty()
        .set(FIELD_NICKNAME, nickname)
        .set(FIELD_PHOTO_URL, photoUrl)
        .set(FIELD_FEATURES, features);
  }

  private void readCampaigns() {
    context.invites().listenCampaigns(campaigns -> {
      this.campaigns = campaigns;
      updated(this);
    });
  }

  private void storeMiniatures() {
    if (miniaturesLoading || !miniaturesDocument.isPresent()) {
      return;
    }

    miniaturesDocument.get().update(writeMiniatures().asMap());
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
