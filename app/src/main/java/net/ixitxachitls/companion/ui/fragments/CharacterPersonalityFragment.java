package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.enums.Alignment;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Collections;
import java.util.List;

/**
 * A nested fragment for a characters personality values.
 */
public class CharacterPersonalityFragment extends NestedCompanionFragment {

  private Character character = Character.DEFAULT;

  // UI Elements.
  private LabelledEditTextView name;
  private LabelledTextView gender;
  private LabelledTextView race;
  private LabelledEditTextView height;
  private LabelledEditTextView weight;
  private LabelledEditTextView age;
  private LabelledEditTextView looks;
  private LabelledTextView alignment;
  private LabelledEditTextView religion;

  public CharacterPersonalityFragment() {
  }

  public void editAlignment() {
    ListSelectDialog edit = ListSelectDialog.newStringInstance(R.string.character_edit_alignment,
        Lists.newArrayList(character.getAlignment().getName()), Alignment.names(),
        R.color.character);
    edit.setSelectListener(this::changeAlignment);
    edit.display();
  }

  public void editGender() {
    ListSelectDialog edit = ListSelectDialog.newStringInstance(R.string.character_edit_gender,
        Lists.newArrayList(character.getGender().getName()), Gender.names(),
        R.color.character);
    edit.setSelectListener(this::changeGender);
    edit.display();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
    super.onCreateView(inflater, container, state);

    View view = inflater.inflate(R.layout.fragment_character_personality, container, false);
    name = view.findViewById(R.id.name);
    name.text(character.getName()).onChange(this::changeName);
    gender = view.findViewById(R.id.gender);
    gender.onClick(this::editGender);
    race = view.findViewById(R.id.race);
    race.onClick(this::editRace);
    looks = view.findViewById(R.id.looks);
    looks.text(character.getLooks()).onChange(this::changeLooks);

    height = view.findViewById(R.id.height);
    height.onChange(this::changeHeight);
    Wrapper.wrap(view, R.id.random_height_weight).onClick(this::randomHeightWeight);
    weight = view.findViewById(R.id.weight);
    weight.onChange(this::changeWeight);
    age = view.findViewById(R.id.age);
    age.onChange(this::changeAge);
    alignment = view.findViewById(R.id.alignment);
    alignment.onClick(this::editAlignment);
    religion = view.findViewById(R.id.religion);
    religion.onChange(this::changeReligion);

    return view;
  }

  @Override
  public void onPause() {
    super.onPause();

    character.store();
  }

  @Override
  public void onResume() {
    super.onResume();

    update();
  }

  public void show(Character character) {
    this.character = character;

    update();
  }

  private void changeAge() {
    try {
      character.setAge(Integer.parseInt(age.getText()));
    } catch (NumberFormatException e) {
      character.setAge(0);
    }
  }

  private boolean changeAlignment(List<String> values) {
    alignment.text(values.get(0));
    character.setAlignment(Alignment.fromName(values.get(0)));

    return true;
  }

  private boolean changeGender(List<String> values) {
    gender.text(values.get(0));
    character.setGender(Gender.fromName(values.get(0)));

    return true;
  }

  private void changeHeight() {
    character.setHeight(height.getText());
  }

  void changeLooks() {
    character.setLooks(looks.getText());
  }

  private void changeName() {
    character.setName(name.getText());
  }

  private boolean changeRace(List<String> values) {
    race.text(values.get(0));
    character.setRace(values.get(0));

    return true;
  }

  private void changeReligion() {
    character.setReligion(religion.getText());
  }

  private void changeWeight() {
    character.setWeight(weight.getText());
  }

  private void editRace() {
    ListSelectDialog edit = ListSelectDialog.newStringInstance(R.string.character_edit_race,
        character.getRace().isPresent()
            ? Lists.newArrayList(character.getRace().get().getName())
            : Collections.emptyList(),
        Templates.get().getMonsterTemplates().primaryRaces(), R.color.character);
    edit.setSelectListener(this::changeRace);
    edit.display();
  }

  private void randomHeightWeight() {
    if (!character.getRace().isPresent()) {
      Status.error("no race selected");
      return;
    }

    character.setRandomHeightAndWeight();
    character.setRandomAge();
    update();
  }

  private void update() {
    if (getView() == null) {
      return;
    }

    name.text(character.getName());
    height.text(character.getHeight());
    weight.text(character.getWeight());
    age.text(character.getAge() > 0 ? String.valueOf(character.getAge()) : "");
    looks.text(character.getLooks());
    religion.text(character.getReligion());

    if (character.getGender() != Gender.UNKNOWN) {
      gender.text(character.getGender().getName());
    }

    if (character.getRace().isPresent()) {
      race.text(character.getRace().get().getName());
    }

    if (character.getAlignment() != Alignment.UNKNOWN) {
      alignment.text(character.getAlignment().getName());
    }
  }
}
