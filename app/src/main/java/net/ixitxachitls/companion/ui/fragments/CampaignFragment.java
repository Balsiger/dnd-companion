/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Documents;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.DateDialog;
import net.ixitxachitls.companion.ui.dialogs.EditCampaignDialog;
import net.ixitxachitls.companion.ui.dialogs.InviteDialog;
import net.ixitxachitls.companion.ui.dialogs.MessageDialog;
import net.ixitxachitls.companion.ui.dialogs.MonsterInitiativeDialog;
import net.ixitxachitls.companion.ui.dialogs.TimedConditionDialog;
import net.ixitxachitls.companion.ui.dialogs.XPDialog;
import net.ixitxachitls.companion.ui.views.ActionBarView;
import net.ixitxachitls.companion.ui.views.CampaignTitleView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

import java.io.IOException;
import java.util.Optional;

import static android.app.Activity.RESULT_OK;

/** A fragment displaying campaign information. */
public class CampaignFragment extends CompanionFragment {

  private final int PICK_IMAGE = 1;

  protected Optional<Campaign> campaign = Optional.empty();

  // UI elements.
  protected CampaignTitleView title;
  protected PartyFragment party;
  protected EncounterFragment encounter;
  protected ActionBarView.Action editAction;
  protected ActionBarView.Action calendarAction;
  protected ActionBarView.ActionGroup encounterGroup;
  protected ActionBarView.Action addConditionAction;
  protected ActionBarView.Action xpAction;
  protected ActionBarView.Action inviteAction;
  protected ActionBarView.Action sendMessageAction;
  protected ActionBarView.Action deleteAction;
  private TextWrapper<TextView> date;
  private ActionBarView.Action stopEncounter;
  private ActionBarView.Action nextEncounter;
  private ActionBarView.Action addMonster;
  private ActionBarView.Action delayEncounter;

  public CampaignFragment() {
    super(Type.campaign);
  }

  @Override
  public boolean goBack() {
    CompanionFragments.get().show(Type.campaigns, Optional.of(title));
    return true;
  }

  @Override
  public void refresh() {
    Status.log("CampaignF refresh");
    refresh(Documents.FULL_UPDATE);
    title.refresh(Documents.FULL_UPDATE);

    encounter.refresh();
    party.refresh();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (campaign.isPresent() && requestCode == PICK_IMAGE && resultCode == RESULT_OK &&
        data != null && data.getData() != null) {
      try {
        Uri uri = data.getData();
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
        images().set(campaign.get().getId(), bitmap);
      } catch (IOException e) {
        Status.toast("Cannot load image bitmap: " + e);
      }
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaign, container, false);

    title = view.findViewById(R.id.title);
    title.setImageAction(this::editImage);
    date = TextWrapper.wrap(view, R.id.date)
        .description("Calendar", "Open the calendar for the campaign to allow you to change the "
            + "current date and time of your campaign.");
    party = (PartyFragment) getChildFragmentManager().findFragmentById(R.id.party);
    encounter = (EncounterFragment) getChildFragmentManager().findFragmentById(R.id.encounter);
    // Directly hiding the fragment does not work, as it uses the wrong fragment mamnager.
    getChildFragmentManager().beginTransaction().hide(encounter).commit();

    clearActions();
    addAction(R.drawable.ic_arrow_back_black_24dp, "Back", "Go back to main overview.")
        .onClick(this::goBack);
    editAction = addAction(R.drawable.ic_mode_edit_black_24dp,
        "Edit", "Change the basic information of the campaign.").onClick(this::edit).hide();
    calendarAction = addAction(R.drawable.ic_today_black_24dp, "Calendar",
        "Open the calendarAction for the campaign to allow you to change the "
            + "current date and time of your campaign.").onClick(this::editDate).hide();
    encounterGroup = addActionGroup(R.drawable.ic_sword_cross_black_24dp, "Start Encounter",
        "Start an encounter (combat).").onClick(this::startEncounter).shrink();
    stopEncounter = encounterGroup.addAction(R.drawable.ic_stop_black_24dp, "End Encounter", "Stop the encounter")
        .onClick(this::endEncounter);
    nextEncounter = encounterGroup.addAction(R.drawable.arrow_down_bold, "Next Participant",
        "Finish the current participants round and go to the next participant")
        .onClick(this::nextInEncounter);
    addMonster = encounterGroup.addAction(R.drawable.noun_monster_693507, "Add Monster",
        "Add a monster to the running encounter.")
        .onClick(this::addMonsterInEncounter);
    delayEncounter = encounterGroup.addAction(R.drawable.ic_hourglass_full_black_24dp, "Delay",
        "Delay the current creatures turn.")
        .onClick(this::delayInEncounter);
    addConditionAction = addAction(R.drawable.icons8_treatment_100, "Set a Condition",
        "Set a special condition on one ore multiple characters.")
        .onClick(this::addCondition);
    xpAction = addAction(R.drawable.noun_experience_1705256, "Award XP",
        "Award experience points to your characters.")
        .onClick(this::awardXP);
    inviteAction = addAction(R.drawable.account_plus, "Invite",
        "Invite players to create characters in this campaign.")
        .onClick(this::invite);
    sendMessageAction = addAction(R.drawable.ic_message_text_black_24dp, "Send Message",
        "Send a message to other characters and the DM")
        .onClick(this::sendMessage);
    deleteAction = addAction(R.drawable.ic_delete_black_24dp, "Delete",
        "Delete this campaign. This action cannot be undone and will send "
            + "a deletion request to players to delete this campaign on their devices too. "
            + "You cannot delete a campaign that is currently published or that has local "
            + "characters.")
        .onClick(this::deleteCampaign).hide();

    return view;
  }

  public void showCampaign(Campaign campaign) {
    this.campaign = Optional.of(campaign);
    party.show(campaign);
    encounter.show(campaign);
    encounterGroup.show(campaign.amDM());
    calendarAction.show(campaign.amDM());
    editAction.show(campaign.amDM());
    inviteAction.show(campaign.amDM());
    xpAction.show(campaign.amDM());
    sendMessageAction.show(campaign.amDM());
    stopEncounter.show(campaign.amDM());
    addMonster.show(campaign.amDM());
    nextEncounter.show(campaign.amDM());
    delayEncounter.show(campaign.amDM());

    if (campaign.amDM()) {
      title.setAction(this::edit);
      date.onClick(this::editDate);
    } else {
      title.removeAction();
      date.removeClick();
    }


    characters().addPlayers(campaign);
    monsters().addCampaign(campaign.getId());

    images().observe(this, title::refresh);
    messages().observe(this, title::refresh);
    campaign.observe(this, this::update);

    // The following should be included in the campaign observation above....
    //refresh(Documents.FULL_UPDATE);
  }

  public boolean shows(String campaignId) {
    return campaign.isPresent() && campaign.get().getId().equals(campaignId);
  }

  private void addCondition() {
    if (campaign.isPresent()) {
      if (campaign.get().getBattle().amCurrentPlayer()) {
        TimedConditionDialog.newInstance(campaign.get().getBattle().getCurrentCreatureId(),
            campaign.get().getBattle().getTurn()).display();
      } else if (campaign.get().amDM()) {
        TimedConditionDialog.newInstance(campaign.get().getId(),
            campaign.get().getBattle().getTurn()).display();
      }
    }
  }

  private void addMonsterInEncounter() {
    if (campaign.isPresent() && campaign.get().amDM()) {
      MonsterInitiativeDialog.newInstance(campaign.get().getId(), -1).display();
    }
  }

  private void awardXP() {
    if (campaign.isPresent()) {
      XPDialog.newInstance(campaign.get().getId()).display();
    }
  }

  protected boolean canDeleteCampaign() {
    return campaign.isPresent() && campaign.get().amDM();
  }

  private void delayInEncounter() {
    if (campaign.isPresent()) {
      campaign.get().getBattle().creatureWait();
    }
  }

  protected void deleteCampaign() {
    ConfirmationPrompt.create(getContext())
        .title(getResources().getString(R.string.campaign_delete_title))
        .message(getResources().getString(R.string.campaign_delete_message_remote))
        .yes(this::deleteCampaignOk)
        .show();
  }

  protected void deleteCampaignOk() {
    if (campaign.isPresent()) {
      campaigns().delete(campaign.get());
      Toast.makeText(getActivity(), getString(R.string.campaign_deleted), Toast.LENGTH_SHORT).show();
      show(Type.campaigns);
    }
  }

  private void edit() {
    if (campaign.isPresent()) {
      EditCampaignDialog.newInstance(campaign.get().getId()).display();
    }
  }

  private void editDate() {
    if (campaign.isPresent()) {
      DateDialog.newInstance(campaign.get().getId()).display();
    }
  }

  private void editImage() {
    Intent intent = new Intent();
    // Show only images, no videos or anything else
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    // Always show the chooser (if there are multiple options available)
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
  }

  private void endEncounter() {
    if (campaign.isPresent()) {
      campaign.get().getBattle().end();
    }
  }

  private void invite() {
    if (campaign.isPresent()) {
      InviteDialog.newInstance(campaign.get().getId()).display();
    }
  }

  private void nextInEncounter() {
    if (campaign.isPresent()) {
      campaign.get().getBattle().creatureDone();
    }
  }


  protected void refresh(Documents.Update update) {
    Status.log("CampaignF refresh: " + update);

    // Campaigns.
    if (campaign.isPresent()) {
      title.update(campaign.get());
      title.refresh(update);
      date.text(campaign.get().getCalendar().format(campaign.get().getDate()));
      deleteAction.show(canDeleteCampaign());

      if (campaign.get().getBattle().inBattle()) {
        Log.d("CampaignF", "show encounter, hide party");
        encounter.showAndHide(party);
        encounter.refresh(update);
        encounterGroup.expand();
        addConditionAction.show(campaign.get().amDM()
            || campaign.get().getBattle().amCurrentPlayer());
      } else {
        Log.d("CampaignF", "show party, hide encounter");
        party.showAndHide(encounter);
        encounterGroup.shrink();
        addConditionAction.show();
      }
    }
  }

  private void sendMessage() {
    if (campaign.isPresent()) {
      MessageDialog.newInstance(campaign.get().getId(), me().getId()).display();
    }
  }

  private void startEncounter() {
    if (campaign.isPresent()) {
      if (!campaign.get().amDM()
          || characters().getCampaignCharacters(campaign.get().getId()).isEmpty()) {
        Status.error("You have to be DM of a campaign with characters to start an startEncounter.");
        return;
      }

      campaign.get().getBattle().setup();
    }
  }

  private void update(Campaign campaign) {
    Status.log("CampaignF update: " + campaign.getId());
    refresh();
  }
}
