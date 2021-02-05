package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.view.View;

import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.SpellTemplate;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.util.Strings;

import java.util.Optional;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * A dialog to show spell information.
 */
public class SpellDialog extends Dialog {

  private static final String ARG_SPELL = "spell";
  private static final String ARG_CASTER_LEVEL = "caster_level";
  private static final String ARG_ABILITY_BONUS = "ability_bonus";
  private static final String ARG_SPELL_CLASS = "spell_class";

  SpellTemplate spell = new SpellTemplate(Template.SpellTemplateProto.getDefaultInstance(),
      "dummy");
  int casterLevel;
  int abilityBonus;
  Value.SpellClass spellClass;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    String name = getArguments().getString(ARG_SPELL);
    casterLevel = getArguments().getInt(ARG_CASTER_LEVEL);
    abilityBonus = getArguments().getInt(ARG_ABILITY_BONUS);
    spellClass = Value.SpellClass.forNumber(getArguments().getInt(ARG_SPELL_CLASS));

    Optional<SpellTemplate> template = Templates.get().getSpellTemplates().get(name);
    if (template.isPresent()) {
      spell = template.get();
    } else {
      spell = new SpellTemplate(Template.SpellTemplateProto.getDefaultInstance(), name);
    }
  }

  @Override
  protected void createContent(View view) {
    TextWrapper.wrap(view, R.id.name).text(spell.getName());
    TextWrapper.wrap(view, R.id.school).text(spell.formatSchool());
    TextWrapper.wrap(view, R.id.level).text(spell.formatLevel());
    TextWrapper.wrap(view, R.id.components).text(spell.formatComponents());
    TextWrapper.wrap(view, R.id.casting_time).text(spell.getCastingTime().toString());
    TextWrapper.wrap(view, R.id.range).text(spell.formatRange(casterLevel).toString());
    String effect = spell.formatEffect();
    TextWrapper.wrap(view, R.id.label_effect).visible(!effect.isEmpty());
    TextWrapper.wrap(view, R.id.effect).text(effect).visible(!effect.isEmpty());
    TextWrapper.wrap(view, R.id.target).text(spell.getTarget());
    TextWrapper.wrap(view, R.id.duration).text(spell.formatDuration(casterLevel));
    TextWrapper.wrap(view, R.id.saving_throw).text(spell.getSavingThrow());
    TextWrapper.wrap(view, R.id.spell_resistance)
        .text(spell.formatSavingThrow(spellClass, abilityBonus));
    TextWrapper.wrap(view, R.id.references).text(Strings.COMMA_JOINER.join(spell.getReferences()));
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String spell, int casterLevel,
                                    int abilityBonus, Value.SpellClass spellClass) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_SPELL, spell);
    arguments.putInt(ARG_CASTER_LEVEL, casterLevel);
    arguments.putInt(ARG_ABILITY_BONUS, abilityBonus);
    arguments.putInt(ARG_SPELL_CLASS, spellClass.getNumber());
    return arguments;
  }

  public static SpellDialog newInstance(String spell, int casterLevel, int abilityBonus,
                                        Value.SpellClass spellClass) {
    SpellDialog fragment = new SpellDialog();
    fragment.setArguments(arguments(R.layout.dialog_spell,
        R.string.spell, R.color.spell, spell, casterLevel, abilityBonus, spellClass));
    return fragment;
  }
}
