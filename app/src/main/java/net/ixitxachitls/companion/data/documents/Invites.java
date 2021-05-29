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

import android.os.AsyncTask;

import com.google.common.collect.ImmutableMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.loader.content.AsyncTaskLoader;

/**
 * Manage all invites.
 */
public class Invites extends Documents<Invites> {

  private static final String INVITES = "invites";
  private static final String CAMPAIGNS = "campaigns";
  private static final String FIELD_CAMPAIGN = "campaign";
  private static final String FIELD_ID = "id";

  private Map<String, String> userIdsByEmail = new HashMap<>();

  public Invites(CompanionContext context) {
    super(context);
  }

  @FunctionalInterface
  public interface CampaignsCallback {
    void execute(List<String> campaigns);
  }

  @FunctionalInterface
  public interface UserIdCallback {
    void execute(String id);
  }

  public void doWithUserId(String email, UserIdCallback callback) {
    if (userIdsByEmail.containsKey(email)) {
      callback.execute(userIdsByEmail.get(email));
    } else {
      FirebaseFirestore.getInstance().document(INVITES + "/" + email).get()
          .addOnSuccessListener(snapshot -> {
            userIdsByEmail.put(email, snapshot.getString(FIELD_ID));
            callback.execute(userIdsByEmail.get(email));
          })
          .addOnFailureListener(e -> Status.silentException("Cannot read user id for " + email, e));
    }
  }

  public void listenCampaigns(CampaignsCallback callback) {
    ensureId();

    AsyncTask.execute(() -> {
      db.collection(INVITES + "/" + email() + "/" + CAMPAIGNS)
          .addSnapshotListener((snapshots, e) -> {
            List<String> campaigns = new ArrayList<>();
            if (snapshots != null) {
              for (DocumentSnapshot snapshot : snapshots) {
                campaigns.add(snapshot.getString(FIELD_CAMPAIGN));
              }
            }

            callback.execute(campaigns);
          });
    });
  }

  private void ensureId() {
    // Store the user id with the invites to allow access to a users characters.
    db.document(INVITES + "/" + email()).set(
        ImmutableMap.builder().put(FIELD_ID, FirebaseAuth.getInstance().getCurrentUser().getUid())
            .build());
  }

  private static String email() {
    return FirebaseAuth.getInstance().getCurrentUser().getEmail();
  }

  public static void invite(String email, String campaignId) {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_CAMPAIGN, campaignId);
    FirebaseFirestore.getInstance().collection(INVITES + "/" + email + "/" + CAMPAIGNS).document()
        .set(data);
  }

  public static void uninvite(String email, String campaignId) {
    CollectionReference invites =
        FirebaseFirestore.getInstance().collection(INVITES + "/" + email + "/" + CAMPAIGNS);
    invites.whereEqualTo("campaign", campaignId).addSnapshotListener((snapshots, e) -> {
      if (e == null) {
        for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
          snapshot.getReference().delete();
        }
      } else {
        Status.exception("Cannot read invites!", e);
      }
    });
  }
}
