package net.ixitxachitls.companion.data.enums;

import net.ixitxachitls.companion.proto.Value;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by user on 2021-02-23.
 */
public enum SpellClass implements Enums.Named, Enums.Proto<Value.SpellClass> {

  UNKNOWN("Unknown", "UNK", Value.SpellClass.UNKNOWN_SPELL_CLASS),
  ASSASSIN("Assassin", "Asn", Value.SpellClass.ASSASSIN),
  BARD("Bard", "Brd", Value.SpellClass.BARD),
  CLERIC("Cleric", "Clr", Value.SpellClass.CLERIC),
  DRUID("Druid", "Drd", Value.SpellClass.DRUID),
  PALADIN("Paladin", "Pal", Value.SpellClass.PALADIN),
  RANGER("Ranger", "Rgr", Value.SpellClass.RANGER),
  SORCERER("Sorcerer", "Sor", Value.SpellClass.SORCERER),
  WIZARD("Wizard", "Wiz", Value.SpellClass.WIZARD),
  AIR("Air", "Air", Value.SpellClass.AIR_SPELL),
  CHAOS("Chaos", "Chs", Value.SpellClass.CHAOS),
  DEATH("Death", "Dth", Value.SpellClass.DEATH),
  DESTRUCTION("Destruction", "Dst", Value.SpellClass.DESTRUCTION),
  DROW("Drow", "Drw", Value.SpellClass.DROW),
  EARTH("Earth", "Eth", Value.SpellClass.EARTH_SPELL),
  EVIL("Evil", "Evl", Value.SpellClass.EVIL_SPELL),
  FIRE("Fire", "Fir", Value.SpellClass.FIRE_SPELL),
  GOOD("Good", "God", Value.SpellClass.GOOD_SPELL),
  HEALING("Healing", "Hln", Value.SpellClass.HEALING),
  KNOWLEDGE("Knowledge", "Knl", Value.SpellClass.KNOWLEDGE),
  LAE("Law", "Law", Value.SpellClass.LAW),
  LUCK("Luck", "Lck", Value.SpellClass.LUCK),
  MAGIC("Magic", "Mag", Value.SpellClass.MAGIC),
  PLANT("Plant", "Pnt", Value.SpellClass.PLANT_SPELL),
  PROTECTION("Protection", "Prt", Value.SpellClass.PROTECTION),
  STRENGTH("Strength", "Str", Value.SpellClass.STRENGTH_SPELL_CLASS),
  SUN("Sun", "Sun", Value.SpellClass.SUN),
  TRAVEL("Travel", "Trl", Value.SpellClass.TRAVEL),
  TRICKERY("Trickery", "Trk", Value.SpellClass.TRICKERY),
  WAR("War", "War", Value.SpellClass.WAR),
  WATER("Water", "Wtr", Value.SpellClass.WATER_SPELL),
  DARKNESS("Darkness", "Drk", Value.SpellClass.DARKNESS);

  public static String PATTERN = Arrays.asList(values()).stream()
      .map(SpellClass::getName).reduce((a, b) -> a + "|" + b).get()
      + "|" + Arrays.asList(values()).stream()
      .map(SpellClass::getShortName).reduce((a, b) -> a + "|" + b).get();

  private final String name;
  private final String shortName;
  private final Value.SpellClass proto;

  SpellClass(String name, String shortName, Value.SpellClass proto) {
    this.name = name;
    this.shortName = shortName;
    this.proto = proto;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getShortName() {
    return shortName;
  }

  @Override
  public Value.SpellClass toProto() {
    return proto;
  }

  public static SpellClass fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static SpellClass fromProto(Value.SpellClass proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
