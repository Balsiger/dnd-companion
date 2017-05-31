/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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

package net.ixitxachitls.companion.data.drive;

import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.common.base.Optional;

import net.ixitxachitls.companion.ui.activities.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

/**
 * Connection to google drive.
 */
public class DriveStorage implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
  private final MainActivity activity;
  private Optional<GoogleApiClient> googleApiClient = Optional.absent();
  private List<File> files;

  public DriveStorage(MainActivity activity) {
    this.activity = activity;
  }

  public void export(List<File> files) {
    this.files = files;
    connect();
  }

  public void connect() {
    if (!googleApiClient.isPresent()) {
      googleApiClient = Optional.of(new GoogleApiClient.Builder(activity)
          .addApi(Drive.API)
          .addScope(Drive.SCOPE_FILE)
          .addConnectionCallbacks(this)
          .addOnConnectionFailedListener(this)
          .build());
    }

    if (!googleApiClient.get().isConnected() && !googleApiClient.get().isConnecting()) {
      activity.status("connecting to drive...");
      googleApiClient.get().connect();
    } else {
      exportFiles();
    }
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    activity.status("connected to drive");
    exportFiles();
  }

  private void exportFiles() {
    // Start storing information.
    if (googleApiClient.isPresent()) {
      Drive.DriveApi.getRootFolder(googleApiClient.get())
          .createFolder(googleApiClient.get(), new MetadataChangeSet.Builder()
              .setTitle("RPG Companion").build())
          .setResultCallback(
              new ResolvingResultCallbacks<DriveFolder.DriveFolderResult>(activity, 0) {
                @Override
                public void onSuccess(DriveFolder.DriveFolderResult result) {
                  write(result.getDriveFolder());
                }

                @Override
                public void onUnresolvableFailure(Status status) {
                  activity.toast("Cannot create folder in Drive.");
                }
              });
    }
  }

  private void write(DriveFolder folder) {
    activity.status("writing data to drive");
    if (googleApiClient.isPresent()) {
      for (File file : files) {
        Drive.DriveApi.newDriveContents(googleApiClient.get()).setResultCallback(
            new ResolvingResultCallbacks<DriveApi.DriveContentsResult>(activity, 0) {
              @Override
              public void onSuccess(@NonNull DriveApi.DriveContentsResult result) {
                DriveContents contents = result.getDriveContents();
                file.write(contents.getOutputStream());
                folder.createFile(googleApiClient.get(),
                    new MetadataChangeSet.Builder()
                        .setTitle(file.name)
                        .setMimeType(file.mimeType)
                        .build(), contents)
                    .setResultCallback(
                        new ResolvingResultCallbacks<DriveFolder.DriveFileResult>(activity, 0) {
                          @Override
                          public void onSuccess(@NonNull DriveFolder.DriveFileResult result) {
                            activity.status("file " + file.name + " written");
                          }

                          @Override
                          public void onUnresolvableFailure(@NonNull Status status) {
                            activity.toast("could not write file " + file.name);
                          }
                        });
              }

              @Override
              public void onUnresolvableFailure(@NonNull Status status) {
                activity.toast("cannot create file contents for " + file.name);
              }
            }
        );
      }
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    activity.status("connection to drive suspended");
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    activity.status("connection to drive failed");
    if (connectionResult.hasResolution()) {
      try {
        connectionResult.startResolutionForResult(activity,
            MainActivity.RESOLVE_DRIVE_CONNECTION_CODE);
      } catch (IntentSender.SendIntentException e) {
        // Unable to resolve, message user appropriately
      }
    } else {
      GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
    }
  }

  public static abstract class File {
    private final String name;
    private final String mimeType;

    public File(String name, String mimeType) {
      this.name = name;
      this.mimeType = mimeType;
    }

    protected abstract boolean write(OutputStream output);

  }

  public static class TextFile extends File {
    private final String content;

    public TextFile(String name, String mimeType, String content) {
      super(name, mimeType);
      this.content = content;
    }

    @Override
    protected boolean write(OutputStream output) {
      try(Writer writer = new OutputStreamWriter(output)) {
        writer.write(content);
        return true;
      } catch (IOException e) {
        return false;
      }
    }
  }

  public static class BinaryFile extends File {

    private final InputStream input;

    public BinaryFile(String name, String mimeType, InputStream input) {
      super(name, mimeType);
      this.input = input;
    }

    @Override
    protected boolean write(OutputStream output) {
      byte[] buffer = new byte[10_000];
      try {
        for (int length = input.read(buffer); length > 0; length = input.read(buffer)) {
          output.write(buffer, 0, length);
        }
      } catch (IOException e) {
        return false;
      } finally {
        try {
          output.close();
          input.close();
        } catch (IOException e) {
        }
      }

      return true;
    }
  }
}
