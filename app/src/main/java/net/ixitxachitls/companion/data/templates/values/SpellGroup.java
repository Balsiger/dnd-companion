package net.ixitxachitls.companion.data.templates.values;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.data.templates.AdventureTemplate;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;

/**
 * A group of spells, for example in an encounter.
 */
public class SpellGroup {
  private final String name;
  private final int casterLevel;
  private final int abilityBonus;
  private Value.SpellClass spellClass;
  private final ImmutableList<String> spells;

  public SpellGroup(String name, int casterLevel, int abilityBonus, Value.SpellClass spellClass,
                    ImmutableList<String> spells) {
    this.name = name;
    this.casterLevel = casterLevel;
    this.abilityBonus = abilityBonus;
    this.spellClass = spellClass;
    this.spells = spells;
  }

  public int getAbilityBonus() {
    return abilityBonus;
  }

  public int getCasterLevel() {
    return casterLevel;
  }

  public String getName() {
    return name;
  }

  public Value.SpellClass getSpellClass() {
    return spellClass;
  }

  public ImmutableList<String> getSpells() {
    return spells;
  }

  public void setSpellClass(Value.SpellClass spellClass) {
    this.spellClass = spellClass;
  }

  public static SpellGroup fromProto(
      Template.AdventureTemplateProto.Encounter.SpellGroup proto) {
    return new SpellGroup(proto.getName(), proto.getCasterLevel(), proto.getAbilityBonus(),
        proto.getSpellClass(),
        proto.getSpellList().stream()
            .map(p -> p.getName())
            .collect(ImmutableList.toImmutableList()));
  }
}
