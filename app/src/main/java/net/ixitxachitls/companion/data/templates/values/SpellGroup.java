package net.ixitxachitls.companion.data.templates.values;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.data.enums.MetaMagic;
import net.ixitxachitls.companion.data.enums.SpellClass;
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
  private SpellClass spellClass;
  private final ImmutableList<SpellReference> spells;

  public SpellGroup(String name, int casterLevel, int abilityBonus, SpellClass spellClass,
                    ImmutableList<SpellReference> spells) {
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

  public SpellClass getSpellClass() {
    return spellClass;
  }

  public ImmutableList<SpellReference> getSpells() {
    return spells;
  }

  public void setSpellClass(SpellClass spellClass) {
    this.spellClass = spellClass;
  }

  public static SpellGroup fromProto(
      Template.AdventureTemplateProto.Encounter.SpellGroup proto) {
    return new SpellGroup(proto.getName(), proto.getCasterLevel(), proto.getAbilityBonus(),
        SpellClass.fromProto(proto.getSpellClass()),
        proto.getSpellList().stream()
            .map(SpellReference::fromProto)
            .collect(ImmutableList.toImmutableList()));
  }

  public static class SpellReference {
    private final String name;
    private final ImmutableList<MetaMagic> metaMagics;

    public SpellReference(String name, ImmutableList<MetaMagic> metaMagics) {
      this.name = name;
      this.metaMagics = metaMagics;
    }

    public ImmutableList<MetaMagic> getMetaMagics() {
      return metaMagics;
    }

    public String getName() {
      return name;
    }

    public static SpellReference fromProto(
        Template.AdventureTemplateProto.Encounter.SpellGroup.SpellReference proto) {
      return new SpellReference(proto.getName(),
          proto.getMetaMagicList().stream()
              .map(MetaMagic::fromProto)
              .collect(ImmutableList.toImmutableList()));
    }
  }
}
