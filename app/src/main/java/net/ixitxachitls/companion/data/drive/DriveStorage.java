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
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.LocalCampaign;
import net.ixitxachitls.companion.data.dynamics.LocalCharacter;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.ui.activities.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Connection to google drive.
 */
public class DriveStorage implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
  private final MainActivity activity;
  private Optional<GoogleApiClient> googleApiClient = Optional.empty();
  public List<Task> tasks = new ArrayList<>();

  public DriveStorage(MainActivity activity) {
    this.activity = activity;
  }

  public synchronized void start(Task task) {
    tasks.add(task);
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

    if (googleApiClient.get().isConnected()) {
      process();
    } else if (!googleApiClient.get().isConnecting()) {
      net.ixitxachitls.companion.Status.log("connecting to Google Drive...");
      googleApiClient.get().connect();
    }
  }

  private synchronized void process() {
    if (googleApiClient.isPresent()) {
      for (Iterator<Task> i = tasks.iterator(); i.hasNext(); ) {
        i.next().start(googleApiClient.get(), activity);
        i.remove();
      }
    } else {
      net.ixitxachitls.companion.Status.toast("Google API client not avaiable.");
    }
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    net.ixitxachitls.companion.Status.log("connected to Google Drive");
    process();
  }

  @Override
  public void onConnectionSuspended(int i) {
    net.ixitxachitls.companion.Status.log("connection to Google Drive suspended");
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    net.ixitxachitls.companion.Status.log("connection to Google Drive failed");
    if (connectionResult.hasResolution()) {
      try {
        connectionResult.startResolutionForResult(activity,
            MainActivity.RESOLVE_DRIVE_CONNECTION_CODE);
      } catch (IntentSender.SendIntentException e) {
        net.ixitxachitls.companion.Status.toast("Cannot export data to Google Drive!");
      }
    } else {
      GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
    }
  }

  public static abstract class Task {
    public abstract void start(GoogleApiClient client, MainActivity activity);
  }

  public static class Export extends Task {
    private final List<File> files;
    private boolean started = false;

    public Export(List<File> files) {
      this.files = ImmutableList.copyOf(files);
    }

    @Override
    public void start(GoogleApiClient client, MainActivity activity) {
      if (started) {
        return;
      } else {
        started = true;
      }

      if (files.isEmpty()) {
        net.ixitxachitls.companion.Status.toast("All files already exported.");
        return;
      }

      Drive.DriveApi.getRootFolder(client)
          .createFolder(client, new MetadataChangeSet.Builder()
              .setTitle("RPG Companion")
              .build())
          .setResultCallback(
              new ResolvingResultCallbacks<DriveFolder.DriveFolderResult>(activity, 0) {
                @Override
                public void onSuccess(DriveFolder.DriveFolderResult result) {
                  write(client, activity, result.getDriveFolder());
                }

                @Override
                public void onUnresolvableFailure(Status status) {
                  net.ixitxachitls.companion.Status.toast("Cannot create folder in Google Drive.");
                }
              });
    }

    private void write(GoogleApiClient client, MainActivity activity, DriveFolder folder) {
      net.ixitxachitls.companion.Status.log("writing data to Google Drive");
      for (File file : files) {
        Drive.DriveApi.newDriveContents(client).setResultCallback(
            new ResolvingResultCallbacks<DriveApi.DriveContentsResult>(activity, 0) {
              @Override
              public void onSuccess(DriveApi.DriveContentsResult result) {
                DriveContents contents = result.getDriveContents();
                file.write(contents.getOutputStream());
                folder.createFile(client,
                    new MetadataChangeSet.Builder()
                        .setTitle(file.name)
                        .setMimeType(file.mimeType)
                        .setDescription(file.id)
                        .build(), contents)
                    .setResultCallback(
                        new ResolvingResultCallbacks<DriveFolder.DriveFileResult>(activity, 0) {
                          @Override
                          public void onSuccess(DriveFolder.DriveFileResult result) {
                            net.ixitxachitls.companion.Status.log("file " + file.name + " written");
                          }

                          @Override
                          public void onUnresolvableFailure(Status status) {
                            net.ixitxachitls.companion.Status.toast("could not write file "
                                + file.name);
                          }
                        });
              }

              @Override
              public void onUnresolvableFailure(Status status) {
                net.ixitxachitls.companion.Status.toast("cannot create file contents for "
                    + file.name);
              }
            }
        );
      }
    }

  }

  public static class SelectImportFolder extends Task {

    public SelectImportFolder() {}

    @Override
    public void start(GoogleApiClient client, MainActivity activity) {
      IntentSender intent = Drive.DriveApi.newOpenFileActivityBuilder()
          .setActivityTitle("Select directory to import.")
          .setMimeType(new String[]{"application/vnd.google-apps.folder"})
          .build(client);
      try {
        activity.startIntentSenderForResult(intent, MainActivity.DRIVE_IMPORT_OPEN_CODE, null,
            0, 0, 0);
      } catch (IntentSender.SendIntentException e) {
        net.ixitxachitls.companion.Status.toast("Importing from Google Drive failed: " + e);
      }
    }
  }

  public static class Import extends Task {
    private final DriveFolder folder;

    public Import(DriveFolder folder) {
      this.folder = folder;
    }

    @Override
    public void start(GoogleApiClient client, MainActivity activity) {
      folder.listChildren(client)
          .setResultCallback(
              new ResolvingResultCallbacks<DriveApi.MetadataBufferResult>(activity, 0) {
                @Override
                public void onSuccess(DriveApi.MetadataBufferResult result) {
                  for (Metadata meta : result.getMetadataBuffer()) {
                    if (!meta.isFolder()) {
                      if (meta.getTitle().endsWith(".campaign")
                          || meta.getTitle().endsWith(".character")
                          || meta.getTitle().endsWith(".character.jpg")) {
                        read(client, activity, meta);
                      }
                    }
                  }
                }

                @Override
                public void onUnresolvableFailure(Status status) {
                  net.ixitxachitls.companion.Status.toast(
                      "Could not get files in selected folder.");
                }
              });

    }

    private void read(GoogleApiClient client, MainActivity activity, Metadata meta) {
      meta.getDriveId().asDriveFile().open(client, DriveFile.MODE_READ_ONLY, null)
          .setResultCallback(
              new ResolvingResultCallbacks<DriveApi.DriveContentsResult>(activity, 0) {
                @Override
                public void onSuccess(DriveApi.DriveContentsResult result) {
                  net.ixitxachitls.companion.Status.log("reading file " + meta.getTitle());
                  try {
                    if (meta.getTitle().endsWith(".campaign")) {
                      Data.CampaignProto proto = Data.CampaignProto.getDefaultInstance()
                          .getParserForType()
                          .parseFrom(result.getDriveContents().getInputStream());
                      LocalCampaign.fromProto(Campaigns.getLocalIdFor(proto.getId()), proto)
                          .store();
                    } else if (meta.getTitle().endsWith(".character")) {
                      Data.CharacterProto proto = Data.CharacterProto.getDefaultInstance()
                          .getParserForType()
                          .parseFrom(result.getDriveContents().getInputStream());
                      LocalCharacter.fromProto(
                          Characters.getLocalIdFor(proto.getCreature().getId()), proto).store();
                    } else if (meta.getTitle().endsWith(".character.jpg")) {
                      new Image(Character.TABLE, meta.getDescription(),
                          Image.asBitmap(result.getDriveContents().getInputStream()));
                    }
                  } catch (InvalidProtocolBufferException e) {
                    net.ixitxachitls.companion.Status.toast("Reading of file " + meta.getTitle()
                        + " failed!");
                  }
                }

                @Override
                public void onUnresolvableFailure(Status status) {
                  net.ixitxachitls.companion.Status.toast("could not import Google Drive file "
                      + meta.getTitle());
                }
              });
    }
  }

  public static abstract class File {
    private final String name;
    private final String id;
    private final String mimeType;

    public File(String name, String id, String mimeType) {
      this.name = name;
      this.id = id;
      this.mimeType = mimeType;
    }

    protected abstract boolean write(OutputStream output);
  }

  public static class TextFile extends File {
    private final String content;

    public TextFile(String name, String id, String mimeType, String content) {
      super(name, id, mimeType);
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

    private final byte[] contents;

    public BinaryFile(String name, String id, String mimeType, byte[] contents) {
      super(name, id, mimeType);
      this.contents = contents;
    }

    @Override
    protected boolean write(OutputStream output) {
      try {
        output.write(contents);
        return true;
      } catch (IOException e) {
        return false;
      }
    }
  }

  public static class StreamFile extends File {

    private final InputStream input;

    public StreamFile(String name, String id, String mimeType, InputStream input) {
      super(name, id, mimeType);
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
