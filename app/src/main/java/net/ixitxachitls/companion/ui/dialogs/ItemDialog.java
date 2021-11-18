/*
 * Copyright (c) 2017-2020 Peter Balsiger
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

package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.enums.Size;
import net.ixitxachitls.companion.data.values.History;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.data.values.Substance;
import net.ixitxachitls.companion.ui.views.FormattedTextView;
import net.ixitxachitls.companion.ui.views.ItemView;
import net.ixitxachitls.companion.ui.views.Views;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.util.Strings;
import net.ixitxachitls.companion.util.Texts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A dialog to show the item summary.
 */
public class ItemDialog extends Dialog {

  private static final String ARG_ITEM_ID = "item_id";
  private static final String ARG_OWNER_ID = "owner_id";
  private static final String ARG_CAMPAIGN_ID = "campaign_id";

  // The following are only set after onCreate.
  private Item.Owner owner;
  private Item item;
  private Campaign campaign;

  private TextWrapper<TextView> debugId;
  private TextWrapper<TextView> appearance;
  private FormattedTextView shortDescription;
  private FormattedTextView description;
  private TextWrapper<TextView> notes;
  private TextView notesLabel;
  private TextWrapper<TextView> dmNotes;
  private TextView dmNotesLabel;
  private TextWrapper<TextView> dmValue;
  private TextWrapper<TextView> weight;
  private TextWrapper<TextView> substance;
  private TextView substanceLabel;
  private TextWrapper<TextView> weapon;
  private TextView weaponLabel;
  private TextWrapper<TextView> wearable;
  private TextView wearableLabel;
  private TextWrapper<TextView> counted;
  private TextView countedLabel;
  private TextWrapper<TextView> multiple;
  private TextView multipleLabel;
  private TextWrapper<TextView> multiuse;
  private TextView multiuseLabel;
  private TextWrapper<TextView> dmMagic;
  private TextView dmMagicLabel;
  private TextWrapper<TextView> size;
  private TextWrapper<TextView> hp;
  private TextWrapper<TextView> dmIdentified;
  private TextWrapper<TextView> dmTime;
  private TextView dmTimeLabel;
  private TextWrapper<TextView> dmBase;
  private TextWrapper<TextView> dmProbability;
  private TextWrapper<TextView> dmCategories;
  private TextWrapper<TextView> dmSynonyms;
  private TextView dmSynonymsLabel;
  private TextWrapper<TextView> dmReferences;
  private TextView dmReferencesLabel;
  private TextWrapper<TextView> dmWorlds;
  private TextWrapper<TextView> dmIncomplete;
  private TextView contentsLabel;
  private LinearLayout contents;
  private LinearLayout history;

  @Override
  public void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);

    if (getArguments() != null) {
      String ownerId = getArguments().getString(ARG_OWNER_ID);
      String itemId = getArguments().getString(ARG_ITEM_ID);
      String campaignId = getArguments().getString(ARG_CAMPAIGN_ID);

      Optional<? extends Item.Owner> owner = Item.findOwner(ownerId);
      if (owner.isPresent()) {
        this.owner = owner.get();
      } else {
        Status.error("Cannot find owner for item " + itemId);
        close();
        return;
      }

      Optional<Item> item = this.owner.getItem(itemId);
      if (item.isPresent()) {
        this.item = item.get();
      } else {
        Status.error("Cannot find item " + itemId);
        close();
        return;
      }

      campaign = campaigns().get(campaignId);
    }
  }

  @Override
  protected void createContent(View view) {
    debugId = TextWrapper.wrap(view, R.id.debug_id);
    appearance = TextWrapper.wrap(view, R.id.appearance);
    shortDescription = view.findViewById(R.id.short_description);
    description = view.findViewById(R.id.description);
    notes = TextWrapper.wrap(view, R.id.notes);
    notesLabel = view.findViewById(R.id.label_notes);
    dmNotes = TextWrapper.wrap(view, R.id.dm_notes);
    dmNotesLabel = view.findViewById(R.id.label_dm_notes);
    dmValue = TextWrapper.wrap(view, R.id.dm_value);
    weight = TextWrapper.wrap(view, R.id.weight);
    hp = TextWrapper.wrap(view, R.id.hp);
    substanceLabel = view.findViewById(R.id.label_substance);
    substance = TextWrapper.wrap(view, R.id.substance);
    weapon = TextWrapper.wrap(view, R.id.weapon);
    weaponLabel = view.findViewById(R.id.label_weapon);
    wearable = TextWrapper.wrap(view, R.id.wearable);
    wearableLabel = view.findViewById(R.id.label_wearable);
    counted = TextWrapper.wrap(view, R.id.counted);
    countedLabel = view.findViewById(R.id.label_counted);
    multiple = TextWrapper.wrap(view, R.id.multiple);
    multipleLabel = view.findViewById(R.id.label_multiple);
    multiuse = TextWrapper.wrap(view, R.id.multiuse);
    multiuseLabel = view.findViewById(R.id.label_multiuse);
    dmMagic = TextWrapper.wrap(view, R.id.dm_magic);
    dmMagicLabel = view.findViewById(R.id.label_dm_magic);
    size = TextWrapper.wrap(view, R.id.size);
    dmIdentified = TextWrapper.wrap(view, R.id.dm_identified);
    dmTime = TextWrapper.wrap(view, R.id.dm_time);
    dmTimeLabel = view.findViewById(R.id.label_dm_time);
    dmBase = TextWrapper.wrap(view, R.id.dm_base);
    dmProbability = TextWrapper.wrap(view, R.id.dm_probability);
    dmCategories = TextWrapper.wrap(view, R.id.dm_categories);
    dmSynonyms = TextWrapper.wrap(view, R.id.dm_synonyms);
    dmSynonymsLabel = view.findViewById(R.id.dm_label_synonyms);
    dmReferences = TextWrapper.wrap(view, R.id.dm_references);
    dmReferencesLabel = view.findViewById(R.id.dm_label_references);
    dmWorlds = TextWrapper.wrap(view, R.id.dm_worlds);
    dmIncomplete = TextWrapper.wrap(view, R.id.dm_incomplete);
    contentsLabel = view.findViewById(R.id.label_contents);
    contents = view.findViewById(R.id.contents);
    history = view.findViewById(R.id.history);

    update(view);
  }

  private String formatCategories() {
    Set<String> categories = item.getCategories();

    if (item.isMonetary()) {
      categories.add("monetary");
    }

    if (item.hasWeaponFiness()) {
      categories.add("weapon finuess");
    }

    if (item.isAmmunition()) {
      categories.add("ammunition");
    }

    return Strings.COMMA_JOINER.join(categories);
  }

  private String formatHp() {
    List<String> parts = new ArrayList<>();
    if (owner.amDM()) {
      parts.add(item.getHp() + " of " + item.computeMaxHp());
    } else {
      parts.add(String.valueOf(item.getHp()));
    }

    int hardness = item.getHardness();
    if (hardness > 0) {
      parts.add("hardness " + hardness);
    }

    int breakDC = item.getBreakDC();
    if (breakDC > 0) {
      parts.add("break DC " + breakDC);
    }

    return Strings.COMMA_JOINER.join(parts);
  }

  private String formatSubstance() {
    Substance substance = item.getSubstance();
    if (substance.getThickness().isZero()) {
      return "";
    }

    return substance.getThickness() + " of " + substance.getMaterial();
  }

  private void update(View view) {
    if (item == null || owner == null) {
      return;
    }

    if (owner.amDM() && !item.getName().equals(item.getPlayerName())) {
      setTitle(item.getName() + " (" + item.getPlayerName() + ")");
    } else {
      setTitle(item.getPlayerName());
    }

    Texts.Values formattingValues = new Texts.Values();
    debugId.text(item.getId());
    appearance.text(item.getAppearance());
    shortDescription.text(item.getShortDescription(owner.amDM()), formattingValues);
    description.text(item.getDescription(owner.amDM()), formattingValues);
    Views.setOrHide(notes, item.getPlayerNotes(), notesLabel);
    Views.setOrHide(dmNotes, item.getPlayerNotes(), dmNotesLabel);
    if (item.getMultiple() <= 1 && !item.hasContents()) {
      dmValue.text(item.getValue().toString());
      weight.text(item.getWeight().toString());
    } else if (item.getMultiple() > 1 && !item.hasContents()) {
      dmValue.text(item.getValue().toString() +
          " (" + item.getMultiple() + " x " + item.getRawValue() + ")");
      weight.text(item.getWeight().toString() +
          " (" + item.getMultiple() + " x " + item.getRawWeight() + ")");
    } else if (item.getMultiple() <= 1 && item.hasContents()) {
      dmValue.text(item.getValue().toString() +
          " (without contents " + item.getRawValue() + ")");
      weight.text(item.getWeight().toString() +
          " (without contents " + item.getRawWeight() + ")");
    } else {
      dmValue.text(item.getValue().toString() +
          " (" + item.getMultiple() + " x " + item.getRawValue() + ", without contents)");
      weight.text(item.getWeight().toString() +
          " (" + item.getMultiple() + " x " + item.getRawWeight() + ", without contents)");
    }
    hp.text(formatHp());
    Views.setOrHide(substance, formatSubstance(), substanceLabel);
    Views.setOrHide(weapon, item.formatWeapon(), weaponLabel);
    Views.setOrHide(wearable, item.formatWearable(), wearableLabel);
    Views.setOrHide(counted, item.formatCounted(), countedLabel);
    Views.setOrHide(multiple, item.formatAmount(), multipleLabel);
    Views.setOrHide(multiuse, item.formatUses(), multiuseLabel);
    Views.setOrHide(dmMagic, item.formatMagic(), dmMagicLabel);
    Size wielderSize = item.getWielderSize();
    size.text(item.getSize().toString()
        + (wielderSize == Size.UNKNOWN ? "" : " (wielder " + wielderSize + ")"));
    dmIdentified.text(item.isIdentified() ? "Yes" : "No");
    Views.setOrHide(dmTime, item.formatTime(), dmTimeLabel);
    dmBase.text(Strings.COMMA_JOINER.join(item.getTemplateNames()));
    dmProbability.text(item.getProbability().toString());
    dmCategories.text(formatCategories());
    Views.setOrHide(dmSynonyms, Strings.COMMA_JOINER.join(item.getSynonyms()), dmSynonymsLabel);
    Views.setOrHide(dmReferences, Strings.COMMA_JOINER.join(item.getReferences()),
        dmReferencesLabel);
    dmWorlds.text(Strings.COMMA_JOINER.join(item.getWorlds()));
    Views.setOrHide(dmIncomplete, item.getIncomplete());

    contents.removeAllViews();
    if (item.getContents().isEmpty()) {
      contentsLabel.setVisibility(View.GONE);
    } else {
      contentsLabel.setVisibility(View.VISIBLE);
      for (Item content : item.getContents()) {
        contents.addView(new ItemView(getContext(), campaign, owner, content, true));
      }
    }

    history.removeAllViews();
    for (History.Entry entry : item.getHistory().getEntries()) {
      TextWrapper<TextView> text = TextWrapper.wrap(new TextView(getContext()));
      text.text(entry.format());
      history.addView(text.get());
    }

    Views.updateVisibility((ViewGroup) view, Status.isShowing(), owner.amDM());
  }

  protected static Bundle arguments(String itemId, String ownerId, String campaignId) {
    Bundle arguments = Dialog.arguments(R.layout.dialog_item, R.string.dialog_item_title,
        R.color.item, R.color.itemText);
    arguments.putString(ARG_ITEM_ID, itemId);
    arguments.putString(ARG_OWNER_ID, ownerId);
    arguments.putString(ARG_CAMPAIGN_ID, campaignId);
    return arguments;
  }

  public static ItemDialog newInstance(String itemId, String ownerId, String campaignid) {
    ItemDialog dialog = new ItemDialog();
    dialog.setArguments(arguments(itemId, ownerId, campaignid));
    return dialog;
  }
}
