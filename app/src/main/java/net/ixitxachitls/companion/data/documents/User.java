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

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.data.CompanionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The data for a single user.
 */
public class User extends Document<User> {

  private static final Factory FACTORY = new Factory();

  protected static final String PATH = "users";
  private static final String FIELD_NICKNAME = "nickname";
  private static final String DEFAULT_NICKNAME = "";
  private static final String FIELD_PHOTO_URL = "photoUrl";
  private static final String FIELD_FEATURES = "features";

  private String nickname;
  private String photoUrl = "";
  private List<String> campaigns = new ArrayList<>();
  private List<String> features = new ArrayList<>();

  protected static User getOrCreate(CompanionContext context, String id) {
    User user = Document.getOrCreate(FACTORY, context, PATH + "/" + id);
    user.whenReady(user::readCampaigns);

    return user;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }

  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
  }

  public List<String> getFeatures() {
    return features;
  }

  public void setFeatures(List<String> features) {
    this.features = new ArrayList<>(features);
  }

  public List<String> getCampaigns() {
    return campaigns;
  }

  private void readCampaigns() {
    context.invites().listenCampaigns(campaigns -> { this.campaigns = campaigns; updated(); });
  }

  public boolean owns(String id) {
    return id.startsWith(getId());
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

  @Override
  public String toString() {
    return getNickname();
  }

  private static class Factory implements DocumentFactory<User> {
    @Override
    public User create() {
      return new User();
    }
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

  public static boolean isUser(String id) {
    return id.matches(PATH + "/[^/]*/");
  }
}
