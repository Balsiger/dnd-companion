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

package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.data.templates.ItemTemplate;
import net.ixitxachitls.companion.data.values.Duration;
import net.ixitxachitls.companion.data.values.History;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.data.values.Money;
import net.ixitxachitls.companion.ui.views.LabelledAutocompleteTextView;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledMultiAutocompleteTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Misc;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * A dialog to add a new baseTemplate.
 */
public class EditItemDialog extends Dialog {

  private static final String ARG_OWNER_ID = "owner_id";
  private static final String ARG_ITEM_ID = "item_id";

  private Optional<ItemTemplate> baseTemplate = Optional.empty();
  private List<ItemTemplate> templates = Collections.emptyList();

  private LabelledAutocompleteTextView itemSelection;
  private LabelledMultiAutocompleteTextView templatesSelection;
  private LabelledEditTextView name;
  private LabelledTextView dmName;
  private LabelledEditTextView value;
  private LabelledTextView weight;
  private LabelledEditTextView hp;
  private LabelledEditTextView appearance;
  private LabelledEditTextView multiple;
  private LabelledEditTextView multiuse;
  private LabelledEditTextView count;
  private LabelledEditTextView timeLeft;
  private LabelledEditTextView playerNotes;
  private LabelledEditTextView dmNotes;
  private Wrapper<Button> add;
  private Wrapper<Button> save;

  // The following fields are only set after onCreate.
  private Item.Owner owner;
  private Optional<Item> item;

  public EditItemDialog() {
    this.item = Optional.empty();
  }

  @Override
  public void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);

    if (getArguments() != null) {
      String ownerId = getArguments().getString(ARG_OWNER_ID);
      Optional<? extends Item.Owner> owner = Item.findOwner(ownerId);
      if (owner.isPresent()) {
        this.owner = owner.get();
      } else {
        Status.error("Cannot find an owner for the item!");
        close();
      }

      String itemId = getArguments().getString(ARG_ITEM_ID);
      //if (itemId.isEmpty()) {
      //  Status.error("No ID for item found");
      //  close();
      //}

      if (!itemId.isEmpty()) {
        item = this.owner.getItem(itemId);
      }
    }
  }

  @Override
  protected void createContent(View view) {
    itemSelection = view.findViewById(R.id.item);
    itemSelection.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        Templates.get().getItemTemplates().realItems()));
    itemSelection.onChange(this::selectItem);

    templatesSelection = view.findViewById(R.id.templates);
    templatesSelection.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        Templates.get().getItemTemplates().templates()));
    templatesSelection.onChange(this::selectItem);

    TextWrapper.wrap(view, R.id.id).text(item.isPresent() ? item.get().getId() : "")
        .visible(Misc.onEmulator());
    name = view.findViewById(R.id.name);
    dmName = view.findViewById(R.id.dm_name);
    value = view.findViewById(R.id.value);
    value.validate(Money::validate);
    weight = view.findViewById(R.id.weight);
    hp = view.findViewById(R.id.hp);
    appearance = view.findViewById(R.id.appearance);
    multiple = view.findViewById(R.id.multiple);
    multiuse = view.findViewById(R.id.multiuse);
    count = view.findViewById(R.id.count);
    timeLeft = view.findViewById(R.id.time_left);
    playerNotes = view.findViewById(R.id.player_notes);
    dmNotes = view.findViewById(R.id.dm_notes);
    add = Wrapper.<Button>wrap(view, R.id.add).onClick(() -> store(true)).disabled();
    save = Wrapper.<Button>wrap(view, R.id.save).onClick(() -> store(false));
    if (item.isPresent()) {
      add.gone();
      update(item.get());
    } else {
      save.gone();
      name.gone();
    }
  }

  private int parseCount() {
    if (count.isEmpty()) {
      return 1;
    }

    return Integer.parseInt(count.getText());
  }

  private int parseHp() {
    if (hp.isEmpty()) {
      return 0;
    }

    return Integer.parseInt(hp.getText());
  }

  private int parseMultiple() {
    if (multiple.isEmpty()) {
      return 1;
    }

    return Integer.parseInt(multiple.getText());
  }

  private int parseMultiuse() {
    if (multiuse.isEmpty()) {
      return 1;
    }

    return Integer.parseInt(multiuse.getText());
  }

  private Duration parseTimeLeft() {
    Optional<Duration> parsed = Duration.parse(timeLeft.getText());
    if (parsed.isPresent()) {
      return parsed.get();
    }

    return Duration.ZERO;
  }

  private void selectItem() {
    baseTemplate = Templates.get().getItemTemplates().get(itemSelection.getText());

    if (baseTemplate.isPresent()) {
      templates = new ArrayList<>();
      templates.add(baseTemplate.get());
      for (String name : templatesSelection.getText().split("\\s*,\\s*")) {
        Optional<ItemTemplate> template = Templates.get().getItemTemplates().get(name);
        if (template.isPresent()) {
          templates.add(template.get());
        }
      }
      templates = Item.expandBaseTemplates(templates);
      update(templates);
      add.enabled();
    } else {
      name.text("");
      dmName.text("");
      value.text("");
      weight.text("");
      hp.text("");
      appearance.text("");
      playerNotes.text("");
      dmNotes.text("");
      multiuse.text("1");
      multiple.text("1");
      timeLeft.text("");
      add.disabled();
    }

    multiple.text("");
    multiuse.text("");
  }

  private void store(boolean create) {
    if (!baseTemplate.isPresent()) {
      return;
    }

    if (create) {
      // Create the corresponding item.
      Optional<Money> itemValue = Money.parse(value.getText());
      if (!itemValue.isPresent()) {
        Status.error("Cannot parse item value!");
      } else {
        item = Optional.of(new Item(
            Item.generateId(CompanionApplication.get(getContext()).context()),
            baseTemplate.get().getName(),
            templates, parseHp(),
            itemValue.get(), appearance.getText(), "", "", "", parseMultiple(), parseMultiuse(),
            parseCount(), Duration.ZERO, false, Collections.emptyList(),
            History.create(me().getId(),
                campaigns().getCampaignFromNestedId(owner.getId()).getDate())));
      }
    }

    if (!item.isPresent()) {
      return;
    }

    Optional<Money> itemValue = Money.parse(value.getText());
    if (!itemValue.isPresent()) {
      Status.error("Cannot parse item value!");
    } else {
      if (owner.amDM()) {
        item.get().setName(name.getText());
      } else {
        item.get().setPlayerName(name.getText());
      }

      item.get().setPlayerName(name.getText());
      item.get().setValue(itemValue.get());
      item.get().setTemplates(templates);
      item.get().setHp(Integer.parseInt(hp.getText()));
      item.get().setAppearance(appearance.getText());
      item.get().setMultiple(parseMultiple());
      item.get().setMultiuse(parseMultiuse());
      item.get().setTimeLeft(parseTimeLeft());
      item.get().setPlayerNotes(playerNotes.getText());
      item.get().setDMNotes(dmNotes.getText());

      if (owner.amDM() && owner.isCharacter()) {
        Message.createForItemAdd(
            CompanionApplication.get().context(), me().getId(), owner.getId(), item.get());
      } else {
        if (create) {
          owner.add(item.get());
        } else {
          owner.updated(item.get());
        }
      }

      save();
    }
  }

  private void update(List<ItemTemplate> templates) {
    String fullName = Item.composeName(templates);
    if (owner.amDM()) {
      name.text(fullName);
    } else if (item.isPresent()) {
      name.text(item.get().getPlayerName());
    }
    dmName.text(fullName);
    value.text(Item.value(templates).toString());
    weight.text(Item.weight(templates).toString());
    hp.text(String.valueOf(Item.hp(templates)));
    appearance.text(Item.appearance(templates));
  }

  private void update(Item item) {
    baseTemplate = item.getBaseTemplate();
    if (baseTemplate.isPresent()) {
      // Setting the item selection with actually select the item and thus overwrite
      // existing templates, thus we do it first.
      itemSelection.text(baseTemplate.get().getName());
      templates = item.getTemplates();
      update(templates);
      templatesSelection.text(Strings.COMMA_JOINER.join(templates.stream().skip(1)
          .map(ItemTemplate::getName)
          .collect(Collectors.toList())));
    }

    dmName.setVisibility(owner.amDM() ? View.VISIBLE : View.GONE);
    dmNotes.setVisibility(owner.amDM() ? View.VISIBLE : View.GONE);

    // We assume that if we have a value, we have everything else as well.
    if (!item.getValue().isZero()) {
      hp.text(String.valueOf(item.getHp()));
      value.text(item.getRawValue().toString());
      weight.text(item.getWeight().toString());
      appearance.text(item.getAppearance());
      name.text(item.getPlayerName());
      dmName.text(item.getName());
      multiple.text(item.getMultiple() > 0 ? String.valueOf(item.getMultiple()) : "");
      multiuse.text(item.getMultiuse() > 0 ? String.valueOf(item.getMultiuse()) : "");
      timeLeft.text(item.getTimeLeft().isNone() ? "" : item.getTimeLeft().toString());
      // identified
      playerNotes.text(item.getPlayerNotes());
      dmNotes.text(item.getDMNotes());
    }
  }

  protected static Bundle arguments(String ownerId, String itemId,
                                    @LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_OWNER_ID, ownerId);
    arguments.putString(ARG_ITEM_ID, itemId);
    return arguments;
  }

  public static EditItemDialog newInstance(String ownerId, String itemId) {
    EditItemDialog dialog = new EditItemDialog();
    dialog.setArguments(arguments(ownerId, itemId, R.layout.dialog_edit_item, R.string
            .character_edit_item,
        R.color.item));

    return dialog;
  }

  public static EditItemDialog newInstance(String ownerId) {
    EditItemDialog dialog = new EditItemDialog();
    dialog.setArguments(arguments(ownerId, "", R.layout.dialog_edit_item,
        R.string.character_add_item, R.color.item));
    return dialog;
  }
}
