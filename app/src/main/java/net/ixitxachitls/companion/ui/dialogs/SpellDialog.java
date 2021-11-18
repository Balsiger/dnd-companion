package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.view.View;

import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.enums.MetaMagic;
import net.ixitxachitls.companion.data.enums.SpellClass;
import net.ixitxachitls.companion.data.templates.SpellTemplate;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.ui.views.FormattedTextView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.util.Strings;
import net.ixitxachitls.companion.util.Texts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import androidx.annotation.LayoutRes;

/**
 * A dialog to show spell information.
 */
public class SpellDialog extends Dialog {

  public static final String VALUE_CASTER_LEVEL = "caster_level";
  public static final String VALUE_SPELL_ABILITY_BONUS = "spell_ability_bonus";
  public static final String VALUE_SPELL_CLASS = "spell_class";

  private static final String ARG_SPELL = "spell";
  private static final String ARG_CASTER_LEVEL = "caster_level";
  private static final String ARG_ABILITY_BONUS = "ability_bonus";
  private static final String ARG_SPELL_CLASS = "spell_class";
  private static final String ARG_META_MAGIC = "meta_magic";

  SpellTemplate spell = new SpellTemplate(Template.SpellTemplateProto.getDefaultInstance(),
      "dummy");
  int casterLevel;
  int abilityBonus;
  SpellClass spellClass;
  List<MetaMagic> metaMagics;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    String name = getArguments().getString(ARG_SPELL);
    casterLevel = getArguments().getInt(ARG_CASTER_LEVEL);
    abilityBonus = getArguments().getInt(ARG_ABILITY_BONUS);
    spellClass = SpellClass.fromName(getArguments().getString(ARG_SPELL_CLASS));
    metaMagics = getArguments().getStringArrayList(ARG_META_MAGIC).stream()
        .map(MetaMagic::fromName).collect(Collectors.toList());

    Optional<SpellTemplate> template = Templates.get().getSpellTemplates().get(name);
    if (template.isPresent()) {
      spell = template.get();
    } else {
      spell = new SpellTemplate(Template.SpellTemplateProto.getDefaultInstance(), name);
    }
  }

  @Override
  protected void createContent(View view) {
    if (metaMagics.isEmpty()) {
      setTitle(spell.getName());
    } else {
      setTitle(spell.getName() + " [" +
          Strings.COMMA_JOINER.join(metaMagics.stream()
              .map(MetaMagic::getName).collect(Collectors.toList())) + "]");
    }
    TextWrapper.wrap(view, R.id.school).text(spell.formatSchool());
    TextWrapper.wrap(view, R.id.level).text(spell.formatLevel());
    TextWrapper.wrap(view, R.id.components).text(spell.formatComponents());
    TextWrapper.wrap(view, R.id.casting_time).text(spell.getCastingTime().toString());
    TextWrapper.wrap(view, R.id.range).text(spell.formatRange(casterLevel).toString());

    String effect = spell.formatEffect();
    TextWrapper.wrap(view, R.id.label_effect).visible(!effect.isEmpty());
    TextWrapper.wrap(view, R.id.effect).text(effect).visible(!effect.isEmpty());

    String target = spell.getTarget();
    TextWrapper.wrap(view, R.id.label_target).visible(!target.isEmpty());
    TextWrapper.wrap(view, R.id.target).text(target).visible(!target.isEmpty());

    TextWrapper.wrap(view, R.id.duration).text(spell.formatDuration(casterLevel));
    TextWrapper.wrap(view, R.id.saving_throw)
        .text(spell.formatSavingThrow(spellClass, abilityBonus));
    TextWrapper.wrap(view, R.id.spell_resistance).text(spell.getSpellResistance());
    TextWrapper.wrap(view, R.id.references).text(Strings.COMMA_JOINER.join(spell.getReferences()));

    String incomplete = spell.getIncomplete();
    TextWrapper.wrap(view, R.id.incomplete).text(spell.getIncomplete())
        .visible(!incomplete.isEmpty());

    ((FormattedTextView) view.findViewById(R.id.description))
        .text(spell.getDescription(), new Texts.Values()
            .put(VALUE_CASTER_LEVEL, casterLevel)
            .put(VALUE_SPELL_ABILITY_BONUS, abilityBonus)
            .put(VALUE_SPELL_CLASS, spellClass.getName()));
  }

  protected static Bundle arguments(@LayoutRes int layoutId, String spell, int casterLevel,
                                    int abilityBonus, SpellClass spellClass,
                                    List<MetaMagic> metaMagics) {
    Bundle arguments = Dialog.arguments(layoutId, R.string.spell, R.color.spell, R.color.spellText);
    arguments.putString(ARG_SPELL, spell);
    arguments.putInt(ARG_CASTER_LEVEL, casterLevel);
    arguments.putInt(ARG_ABILITY_BONUS, abilityBonus);
    arguments.putString(ARG_SPELL_CLASS, spellClass.getName());
    arguments.putStringArrayList(ARG_META_MAGIC, metaMagics.stream()
        .map(MetaMagic::getName)
        .collect(Collectors.toCollection(ArrayList::new)));
    return arguments;
  }

  public static SpellDialog newInstance(String spell, int casterLevel, int abilityBonus,
                                        SpellClass spellClass, List<MetaMagic> metaMagics) {
    SpellDialog fragment = new SpellDialog();
    fragment.setArguments(arguments(R.layout.dialog_spell,
        spell, casterLevel, abilityBonus, spellClass, metaMagics));
    return fragment;
  }
}
