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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The data for a single user.
 */
public class User extends Document<User> {

  protected static final String PATH = "users";
  private static final String FIELD_NAME = "name";
  private static final String FIELD_NICKNAME = "nickname";
  private static final String FIELD_PHOTO_URL = "photoUrl";
  private static final String FIELD_CAMPAIGNS = "campaigns";
  private static final String FIELD_FEATURES = "features";

  private String name;
  private String nickname;
  private String photoUrl;
  private List<String> campaigns = new ArrayList<>();
  private List<String> features = new ArrayList<>();

  public User(String id) {
    super(id, PATH);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return getId();
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

  @Override
  protected void read() {
    name = get(FIELD_NAME, "(not found)");
    nickname = get(FIELD_NICKNAME, name);
    photoUrl = get(FIELD_PHOTO_URL, "");
    campaigns = get(FIELD_FEATURES, new ArrayList<>());
    features = get(FIELD_FEATURES, new ArrayList<>());
  }

  @Override
  protected Map<String, Object> write(Map<String, Object> data) {
    data.put(FIELD_NAME, name);
    data.put(FIELD_NICKNAME, nickname);
    data.put(FIELD_PHOTO_URL, photoUrl);
    data.put(FIELD_CAMPAIGNS, campaigns);
    data.put(FIELD_FEATURES, features);
    return data;
  }

  @Override
  public String toString() {
    return getNickname();
  }
}
