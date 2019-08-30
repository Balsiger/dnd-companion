/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

package net.ixitxachitls.companion.data.drive;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Wrapper to store files on Google Drive.
 */
public class DriveStorage {

  private final Executor executor = Executors.newSingleThreadExecutor();
  private final Drive driveService;

  public DriveStorage(Context context) {
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
    GoogleAccountCredential credential =
        GoogleAccountCredential.usingOAuth2(context, ImmutableList.of(DriveScopes.DRIVE));
    credential.setSelectedAccount(account.getAccount());

    driveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
        new GsonFactory(),
        credential)
        .setApplicationName(context.getString(R.string.app_name))
        .build();
  }

  @FunctionalInterface
  public interface Success {
    void succeeded();
  }

  @FunctionalInterface
  public interface Failure {
    void failed(Exception e);
  }

  public void save(String folderName, Map<String, String> contentByName, Success onSuccess, Failure onFailure) {
    Tasks.call(executor, () -> {
      try {
        // Create folder.
        File metadata = new File();
        metadata.setName(folderName);
        metadata.setMimeType("application/vnd.google-apps.folder");

        File folder = driveService.files().create(metadata)
            .setFields("id")
            .execute();

        for (String name : contentByName.keySet()) {
          metadata = new File()
              .setParents(Collections.singletonList(folder.getId()))
              .setMimeType("text/plain")
              .setName(name);

          ByteArrayContent contentStream =
              ByteArrayContent.fromString("text/plain", contentByName.get(name));

          driveService.files()
              .create(metadata, contentStream)
              .setFields("id, parents")
              .execute();
        }

        return true;
      } catch (UserRecoverableAuthIOException e) {
        // Just rethrow it, to prevent it from being handled by the IOException below.
        throw e;
      } catch (IOException e) {
        Status.exception("Creation of files failed!", e);
        return false;
      }
    })
        .addOnFailureListener(e -> onFailure.failed(e))
        .addOnSuccessListener(e -> onSuccess.succeeded());
  }
}
