/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

package net.ixitxachitls.companion.rules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.values.AbilityAdjustment;
import net.ixitxachitls.companion.data.values.AbilityCheckAdjustment;
import net.ixitxachitls.companion.data.values.ConditionData;
import net.ixitxachitls.companion.data.values.Duration;
import net.ixitxachitls.companion.data.values.GenericAdjustment;
import net.ixitxachitls.companion.data.values.InitiativeAdjustment;
import net.ixitxachitls.companion.data.values.Modifier;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Rules for conditions.
 */
public class Conditions {

  public static final ConditionData ABILITY_DAMAGED = ConditionData.newBuilder("Ability Damaged")
      .description("The character has temporarily lost 1 or more ability score points. Lost "
          + "points return at a rate of 1 per day unless noted otherwise by the condition "
          + "dealing the damage. A character with Strength 0 falls to the ground and is "
          + "helpless. A character with Dexterity 0 is paralyzed. A character with "
          + "Constitution 0 is dead. A character with Intelligence, Wisdom, or Charisma 0 "
          + "is unconscious. (See Ability Score Loss under Special Abilities.)\n"
          + "Ability damage is different from penalties to ability scores, "
          + "which go away when the conditions causing them (fatigue, entanglement, and "
          + "so on) go away.")
      .adjustment("-x ability")
      .adjustment("recover 1 point per day")
      .predefined()
      .dmOnly()
      .icon(R.drawable.icons8_fragile_48)
      .build();
  public static final ConditionData ABILITY_DRAINED = ConditionData.newBuilder("Ability Drained")
      .description("The character has permanently lost 1 or more ability score points. "
          + "The character can regain these points only through magical means. A "
          + "character with Strength 0 falls to the ground and is helpless. A character "
          + "with Dexterity 0 is paralyzed. A character with Constitution 0 is dead. A "
          + "character with Intelligence, Wisdom, or Charisma 0 is unconscious. (See "
          + "Ability Score Loss under Special Abilities earlier in this chapter.)")
      .adjustment("-x ability")
      .predefined()
      .dmOnly()
      .icon(R.drawable.icons8_amputee_48)
      .build();
  public static final ConditionData BLINDED = ConditionData.newBuilder("Blinded")
      .description("The character cannot see. He takes a –2 penalty to Armor Class, loses his"
          + " Dexterity bonus to AC (if any), moves at half speed, and takes a –4 penalty"
          + " on Search checks and on most Strength- and Dexterity-based skill checks. "
          + "All checks and activities that rely on vision (such as reading and Spot "
          + "checks) automatically fail. All opponents are considered to have total "
          + "concealment (50% miss chance) to the blinded character.\n"
          + "Characters who remain blinded for a long time grow accustomed to these "
          + "drawbacks and can overcome some of them (DM’s discretion).")
      .adjustment("cannot see")
      .adjustment("-2 AC")
      .adjustment("no Dexterity bonus to AC")
      .adjustment("half speed")
      .adjustment("-4 on Search checks")
      .adjustment(new AbilityCheckAdjustment(Ability.STRENGTH,
          new Modifier(-4, Modifier.Type.GENERAL, "Blinded")))
      .adjustment(new AbilityCheckAdjustment(Ability.DEXTERITY,
          new Modifier(-4, Modifier.Type.GENERAL, "Blinded")))
      .adjustment("50% miss chance")
      .predefined()
      .icon(R.drawable.eye_off)
      .build();
  public static final ConditionData BLOWN_AWAY = ConditionData.newBuilder("Blown Away")
      .description("Depending on its size, a creature can be blown away by winds of high "
          + "velocity (see Table 3–24, page 95). A creature on the ground that is blown "
          + "away is knocked down and rolls 1d4×10 feet, taking 1d4 points of nonlethal "
          + "damage per 10 feet. A flying creature that is blown away is blown back "
          + "2d6×10 feet and takes 2d6 points of nonlethal damage due to battering and "
          + "buffering.")
      .adjustment("Ground: knocked down")
      .adjustment("Ground: rolled 1d4 x 10 ft, 1d4 nonlethal per 10 ft")
      .adjustment("Flying: blown back 2d6 x 10 ft")
      .adjustment("Flying: 2d6 nonlethal")
      .predefined()
      .icon(R.drawable.icons8_air_48)
      .build();
  public static final ConditionData CHECKED = ConditionData.newBuilder("Checked")
      .description("Prevented from achieving forward motion by an applied force, such as wind"
          + ". Checked creatures on the ground merely stop. Checked flying creatures move "
          + "back a distance specified in the description of the effect.")
      .adjustment("Ground: Unable to move forward")
      .adjustment("Flying: Moved back")
      .predefined()
      .icon(R.drawable.checkerboard)
      .build();
  public static final ConditionData COLD = ConditionData.newBuilder("Cold (unprotected)")
      .description("At temperatures from 5° to -20°C, unprotected characters take nonlethal "
          + "damage every hour.")
      .adjustment("For DC 15 (+1 per previous check) every hour or take 1d6 nonlethal cold damage.")
      .predefined()
      .icon(R.drawable.noun_cold_1421098)
      .dmOnly()
      .build();
  public static final ConditionData COLD_SEVERE_UNPROTECTED =
      ConditionData.newBuilder("Severe Cold (unprotected)")
          .description("At temperatures from -20° to -30°C, unprotected characters may take nonlethal "
              + "damage every 10 minutes.")
          .adjustment("For DC 15 (+1 per previous check) every 10 minutes or 1d6 nonlethal damage.")
          .predefined()
          .icon(R.drawable.noun_cold_1421098)
          .color(R.color.moderate)
          .dmOnly()
          .build();
  public static final ConditionData COLD_SEVERE_LEVEL_1 =
      ConditionData.newBuilder("Severe Cold (protection 1)")
          .description("At temperatures from -20° to -30°C, level 1 protected characters may take "
              + "nonlethal damage every hour.")
          .adjustment("For DC 15 (+1 per previous check) every hour or 1d6 nonlethal damage.")
          .predefined()
          .icon(R.drawable.noun_cold_1421098)
          .color(R.color.moderate)
          .dmOnly()
          .build();
  public static final ConditionData COLD_EXTREME_UNPROTECTED =
      ConditionData.newBuilder("Extreme Cold (unprotected)")
          .description("At temperatures from -30° to -50°C, unprotected characters take cold "
              + "damage every 10 minutes.")
          .adjustment("1d6 cold damage per 10 min, For DC 15 (+1 per previous check) every 10 "
              + "minutes or take 1d4 nonlethal damage.")
          .predefined()
          .icon(R.drawable.noun_cold_1421098)
          .color(R.color.severe)
          .dmOnly()
          .build();
  public static final ConditionData COLD_EXTREME_LEVEL_1 =
      ConditionData.newBuilder("Extreme Cold (protection 1)")
          .description("At temperatures from -30° to -50°C, level 1 protected characters take cold "
              + "damage every 10 minutes.")
          .adjustment("1d6 cold damage per 10 min, For DC 15 (+1 per previous check) every 10 "
              + "minutes or take 1d4 nonlethal damage.")
          .predefined()
          .icon(R.drawable.noun_cold_1421098)
          .dmOnly()
          .build();
  public static final ConditionData COLD_EXTREME_LEVEL_2 =
      ConditionData.newBuilder("Extreme Cold (protection 2)")
          .description("At temperatures from -30° to -50°C, level 2 protected characters take cold "
              + "damage every 10 minutes.")
          .adjustment("1d6 cold damage per 10 min, For DC 15 (+1 per previous check) every hour "
              + "or take 1d4 nonlethal damage.")
          .predefined()
          .icon(R.drawable.noun_cold_1421098)
          .dmOnly()
          .build();
  public static final ConditionData COLD_UNEARTHLY_UNPROTECTED =
      ConditionData.newBuilder("Unearthly Cold (unprotected)")
          .description("At temperatures below -50°C, unprotected characters take cold "
              + "damage every minute.")
          .adjustment("1d6 cold damage and 1d4 nonlethal damage per min.")
          .predefined()
          .icon(R.drawable.noun_cold_1421098)
          .color(R.color.extreme)
          .dmOnly()
          .build();
  public static final ConditionData COLD_UNEARTHLY_LEVEL_1 =
      ConditionData.newBuilder("Unearthly Cold (protected 1)")
          .description("At temperatures below -50°C, level 1 protected characters take cold "
              + "damage every minute.")
          .adjustment("1d6 cold damage and 1d4 nonlethal damage per min.")
          .predefined()
          .icon(R.drawable.noun_cold_1421098)
          .color(R.color.extreme)
          .dmOnly()
          .build();
  public static final ConditionData COLD_UNEARTHLY_LEVEL_2 =
      ConditionData.newBuilder("Unearthly Cold (protected 2)")
          .description("At temperatures below -50°C, level 2 protected characters take cold "
              + "damage every 10 minutes.")
          .adjustment("1d6 cold damage and 1d4 nonlethal damage per 10 min.")
          .predefined()
          .icon(R.drawable.noun_cold_1421098)
          .color(R.color.extreme)
          .dmOnly()
          .build();
  public static final ConditionData COLD_UNEARTHLY_LEVEL_3 =
      ConditionData.newBuilder("Unearthly Cold (protected 3)")
          .description("At temperatures below -50°C, level 3 protected characters take cold "
              + "damage every 10 minutes.")
          .adjustment("1d6 cold damage and 1d4 nonlethal damage per 10 min.")
          .predefined()
          .icon(R.drawable.noun_cold_1421098)
          .color(R.color.extreme)
          .dmOnly()
          .build();
  public static final ConditionData CONFUSED = ConditionData.newBuilder("Confused")
      .description("A confused character’s actions are determined by rolling d% at the "
          + "beginning of his turn: 01–10, attack caster with melee or ranged weapons (or "
          + "close with caster if attacking is not possible); 11–20, act normally; 21–50, do "
          + "nothing but babble incoherently; 51–70, flee away from caster at top possible "
          + "speed; 71–100, attack nearest creature (for this purpose, a familiar counts as "
          + "part of the subject’s self ). A confused character who can’t carry out the "
          + "indicated action does nothing but babble incoherently. Attackers are not at any "
          + "special advantage when attacking a confused character. Any confused character "
          + "who is attacked automatically attacks its attackers on its next turn, as long as"
          + " it is still confused when its turn comes. A confused character does not make "
          + "attacks of opportunity against any creature that it is not already devoted to "
          + "attacking (either because of its most recent action or because it has just been "
          + "attacked).")
      .adjustment("Character's actions are determined randomly.")
      .predefined()
      .icon(R.drawable.icons8_puzzled_48)
      .build();
  public static final ConditionData COWERING = ConditionData.newBuilder("Cowering")
      .description("The character is frozen in fear and can take no actions. A cowering "
          + "character takes a –2 penalty to Armor Class and loses her Dexterity bonus (if "
          + "any).")
      .adjustment("Cannot take actions")
      .adjustment("-2 AC")
      .adjustment("No Dex to AC")
      .predefined()
      .icon(R.drawable.icons8_ostrich_head_in_sand_48)
      .build();
  public static final ConditionData DAZED = ConditionData.newBuilder("Dazed")
      .description("The creature is unable to act normally. A dazed creature can take no "
          + "actions, but has no penalty to AC. A dazed condition typically lasts 1 round.")
      .adjustment("No action (but no AC penalty)")
      .duration(Duration.rounds(1))
      .predefined()
      .icon(R.drawable.icons8_confused_48)
      .build();
  public static final ConditionData DAZZLED = ConditionData.newBuilder("Dazzled")
      .description("The creature is unable to see well because of overstimulation of the "
          + "eyes. A dazzled creature takes a –1 penalty on attack rolls, Search checks, and "
          + "Spot checks.")
      .adjustment("Not see well")
      .adjustment("-1 Attack")
      .adjustment("-1 Search")
      .adjustment("-1 Spot")
      .predefined()
      .icon(R.drawable.sunglasses)
      .build();
  public static final ConditionData DEAD = ConditionData.newBuilder("Dead")
      .description("The character’s hit points are reduced to –10, his Constitution drops to "
          + "0, or he is killed outright by a spell or effect. The character’s soul leaves "
          + "his body. Dead characters cannot benefit from normal or magical healing, but "
          + "they can be restored to life via magic. A dead body decays normally unless "
          + "magically preserved, but magic that restores a dead character to life also "
          + "restores the body either to full health or to its condition at the time of death "
          + "(depending on the spell or device). Either way, resurrected characters need not "
          + "worry about rigor mortis, decomposition, and other conditions that affect dead "
          + "bodies.")
      .adjustment("He's dead, Jim.")
      .predefined()
      .dmOnly()
      .notDismissable()
      .icon(R.drawable.ic_skull_black_36dp)
      .build();
  public static final ConditionData DEAD_CONSTITUTION_0 =
      ConditionData.newBuilder("Dead (Con 0)", DEAD)
          .build();
  public static final ConditionData DEAFENED = ConditionData.newBuilder("Deafened")
      .description("A deafened character cannot hear. She takes a –4 penalty on initiative "
          + "checks, automatically fails Listen checks, and has a 20% chance of spell failure"
          + " when casting spells with verbal components.\n"
          + "Characters who remain deafened for a long time grow accustomed to these "
          + "drawbacks and can overcome some of them (DM’s discretion).")
      .adjustment("Cannot hear")
      .adjustment(new InitiativeAdjustment(-4))
      .adjustment("-100 Listen")
      .adjustment("20% spell failure (V)")
      .predefined()
      .icon(R.drawable.icons8_deaf_48)
      .build();
  public static final ConditionData DISABLED = ConditionData.newBuilder("Disabled")
      .description("A character with 0 hit points, or one who has negative hit points but has"
          + " become stable and conscious, is disabled. A disabled character may take a "
          + "single move action or standard action each round (but not both, nor can she take"
          + " full-round actions). She moves at half speed. Taking move actions doesn’t risk "
          + "further injury, but performing any standard action (or any other action the DM "
          + "deems strenuous, including some free actions such as casting a quickened spell) "
          + "deals 1 point of damage after the completion of the act. Unless the action "
          + "increased the disabled character’s hit points, she is now in negative hit points"
          + " and dying. A disabled character with negative hit points recovers hit points "
          + "naturally if she is being helped. Otherwise, each day she has a 10% chance to "
          + "start recovering hit points naturally starting with that day); otherwise, she "
          + "loses 1 hit point. Once an unaided character starts recovering hit points "
          + "naturally, she is no longer in danger of losing hit points (even if her current "
          + "hit points are negative).")
      .adjustment("Single move or standard action (might cause damage) each round.")
      .predefined()
      .icon(R.drawable.icons8_wheelchair_48)
      .build();
  public static final ConditionData DYING = ConditionData.newBuilder("Dying")
      .description("A dying character is unconcious and near death. She has –1 to –9 current "
          + "hit points. A dying character can take no actions and is unconscious. At the end"
          + " of each round starting with the round in which the character dropped below 0 hit "
          + "points), the character rolls d% to see whether she becomes stable. She has a 10% "
          + "chance to become stable. If she does not, she loses 1 hit point. If a dying "
          + "character reaches –10 hit points, she is dead.")
      .adjustment("Unconscious and near death")
      .adjustment("No actions")
      .adjustment("Lose 1 hp per round")
      .dmOnly()
      .notDismissable()
      .predefined()
      .build();
  public static final ConditionData ENERGY_DRAINED = ConditionData.newBuilder("Energy Drained")
      .description("The character gains one or more negative levels, which might permanently "
          + "drain the character’s levels. If the subject has at least as many negative "
          + "levels as Hit Dice, he dies. Each negative level gives a creature the following "
          + "penalties: –1 penalty on attack rolls, saving throws, skill checks, ability "
          + "checks; loss of 5 hit points; and –1 to effective level (for determining the "
          + "power, duration, DC, and other details of spells or special abilities). I"
          + ".addition, a spellcaster loses one spell or spell slot from the highest spell "
          + "level castable. Apply this condition once per level lost.")
      .adjustment("1 negative levels")
      .adjustment("-1 Attack")
      .adjustment("-1 Saves")
      .adjustment("-1 Skills")
      .adjustment("-1 Ability")
      .adjustment("-5 hp")
      .adjustment("-1 level")
      .adjustment("highest level spell lost")
      .predefined()
      .icon(R.drawable.battery_10)
      .build();
  public static final ConditionData ENTANGLED = ConditionData.newBuilder("Entangled")
      .description("The character is ensnared. Being entangled impedes movement, but does not"
          + " entirely prevent it unless the bonds are anchored to an immobile object or "
          + "tethered by an opposing force. An entangled creature moves at half speed, cannot"
          + " run or charge, and takes a –2 penalty on all attack rolls and a –4 penalty to "
          + "Dexterity. An entangled character who attempts to cast a spell must make a "
          + "Concentration check (DC 15 + the spell’s level) or lose the spell.")
      .adjustment("movement impeded")
      .adjustment("half speed")
      .adjustment("-2 Attack")
      .adjustment(new AbilityAdjustment(Ability.DEXTERITY,
          new Modifier(-4, Modifier.Type.GENERAL, "Entangled")))
      .adjustment("Spell DC 15 + level or lose spell")
      .predefined()
      .icon(R.drawable.icons8_handcuffs_48)
      .build();
  public static final ConditionData EXHAUSTED = ConditionData.newBuilder("Exhausted")
      .description("An exhausted character moves at half speed and takes a –6 penalty to "
          + "Strength and Dexterity. After 1 hour of complete rest, an exhausted character "
          + "becomes fatigued. A fatigued character becomes exhausted by doing something else"
          + " that would normally cause fatigue.")
      .adjustment(new GenericAdjustment("half speed"))
      .adjustment(new AbilityAdjustment(Ability.STRENGTH,
          new Modifier(-6, Modifier.Type.GENERAL, "exhausted")))
      .adjustment(new AbilityAdjustment(Ability.DEXTERITY,
          new Modifier(-6, Modifier.Type.GENERAL, "exhausted")))
      .predefined()
      .notDismissable()
      .icon(R.drawable.icons8_insomnia_48)
      .build();
  public static final ConditionData FASCINATED = ConditionData.newBuilder("Fascinated")
      .description("A fascinated creature is entranced by a supernatural or spell effect. The"
          + " creature stands or sits quietly, taking no actions other than to pay attention "
          + "to the fascinating effect, for as long as the effect lasts. It takes a –4 "
          + "penalty on skill checks made as reactions, such as Listen and Spot checks. Any "
          + "potential threat, such as a hostile creature approaching, allows the fascinated "
          + "creature a new saving throw against the fascinating effect. Any obvious threat, "
          + "such as someone drawing a weapon, casting a spell, or aiming a ranged weapon at "
          + "the fascinated creature, automatically breaks the effect. A fascinated creature’s "
          + "ally may shake it free of the spell as a standard action.")
      .adjustment("Entranced")
      .adjustment("quiet and passive")
      .adjustment("-4 skills as reactions")
      .predefined()
      .icon(R.drawable.icons8_galaxy_48)
      .build();
  public static final ConditionData FATIGUED = ConditionData.newBuilder("Fatigued")
      .description("A fatigued character can neither run nor charge and takes a –2 penalty to"
          + " Strength and Dexterity. Doing anything that would normally cause fatigue causes"
          + " the fatigued character to become exhausted. After 8 hours of complete rest, "
          + "fatigued characters are no longer fatigued.")
      .adjustment("Cannot run")
      .adjustment("Cannot charage")
      .adjustment(new AbilityAdjustment(Ability.STRENGTH,
          new Modifier(-2, Modifier.Type.GENERAL, "Fatigued")))
      .adjustment(new AbilityAdjustment(Ability.DEXTERITY,
          new Modifier(-2, Modifier.Type.GENERAL, "Fatigued")))
      .predefined()
      .notDismissable()
      .icon(R.drawable.icons8_slouch_filled_50)
      .build();
  public static final ConditionData FLAT_FOOTED = ConditionData.newBuilder("Flat-footed")
      .description("A character who has not yet acted during a combat is flat-footed, not yet "
          + "reacting normally to the situation. A flat-footed character loses his Dexterity "
          + "bonus to AC (if any) and cannot make attacks of opportunity.")
      .adjustment("Not yet acted")
      .adjustment("No Dex to AC")
      .adjustment("No attacks of opportunity")
      .predefined()
      .endsBeforeTurn()
      .dmOnly()
      .icon(R.drawable.icons8_foot_50)
      .build();
  public static final ConditionData FRIGHTENED = ConditionData.newBuilder("Frightened")
      .description("A frightened creature flees from the source of its fear as best it can. "
          + "If unable to flee, it may fight. A frightened creature takes a –2 penalty on all"
          + " attack rolls, saving throws, skill checks, and ability checks. A frightened "
          + "creature can use special abilities, including spells, to flee; indeed, the "
          + "creature must use such means if they are the only way to escape. Frightened is "
          + "like shaken, except that the creature must flee if possible. Panicked is a more "
          + "extreme state of fear.")
      .adjustment("Flee from source")
      .adjustment("-2 Attack")
      .adjustment("-2 Saves")
      .adjustment("-2 Skills")
      .adjustment("-2 Ability checks")
      .predefined()
      .icon(R.drawable.icons8_scream_50)
      .build();
  public static final ConditionData FROSTBITTEN = ConditionData.newBuilder("Frostbite")
      .description("A frostbitten character can neither run nor charge and takes a –2 penalty "
          + "to Strength and Dexterity. These penalties end when the character recovers the"
          + "nonlethal damage she took from the cold and exposure.")
      .adjustment("Cannot run")
      .adjustment("Cannot charge")
      .adjustment(new AbilityAdjustment(Ability.STRENGTH,
          new Modifier(-2, Modifier.Type.GENERAL, "Frostbitten")))
      .adjustment(new AbilityAdjustment(Ability.DEXTERITY,
          new Modifier(-2, Modifier.Type.GENERAL, "Frostbitten")))
      .predefined()
      .icon(R.drawable.noun_ice_cube_154854)
      .dmOnly()
      .build();
  public static final ConditionData GRAPPLING = ConditionData.newBuilder("Grappling")
      .description("Engaged in wrestling or some other form of hand-to-hand struggle with one"
          + " or more attackers. A grappling character can undertake only a limited number of "
          + "actions. He does not threaten any squares, and loses his Dexterity bonus to AC "
          + "(if any) against opponents he isn’t grappling.")
      .adjustment("Hand-to-hand struggle")
      .adjustment("Limited actions")
      .adjustment("Not threatening")
      .adjustment("No attacks of opportunity")
      .adjustment("No flanking")
      .adjustment("No Dex to AC")
      .predefined()
      .icon(R.drawable.icons8_wrestling_48)
      .build();
  public static final ConditionData HELPLESS = ConditionData.newBuilder("Helpless")
      .description("A helpless character is paralyzed, held, bound, sleeping, unconscious, or"
          + " otherwise completely at an opponent’s mercy. A helpless target is treated as "
          + "having a Dexterity of 0 (–5 modifier). Melee attacks against a helpless target "
          + "get a +4 bonus (equivalent to attacking a prone target). Ranged attacks gets no "
          + "special bonus against ocund action, an enemy can use a melee weapon to deliver a "
          + "coup de grace to a helpless foe. An enemy can also use a bow or crossbow, "
          + "provided he is adjacent to the target. The attacker automatically hits and "
          + "scores a critical hit. (A rogue also gets her sneak attack damage bonus against "
          + "a helpless foe when delivering a coup de grace.) If the defender survives, he "
          + "must make a Fortitude save (DC 10 + damage dealt) or die. Delivering a coup de "
          + "grace provokes attacks of opportunity. Creatures that are immune to critical "
          + "hits do not take critical damage, nor do they need to make Fortitude saves to "
          + "avoid being killed by a coup de grace.")
      .adjustment("Completely at mercy")
      .adjustment("-4 AC melee")
      .adjustment("can be sneaked")
      .adjustment("can be coup the graced")
      .predefined()
      .icon(R.drawable.icons8_oppression_48)
      .build();
  public static final ConditionData HELPLESS_STRENGTH_0 =
      ConditionData.newBuilder("Helpless (STR 0)", HELPLESS)
      .noShow()
      .build();
  public static final ConditionData HYPOTHERMIA_MILD = ConditionData.newBuilder("Mild Hypothermia")
      .description("A character with mild hypothermia can neither run nor charge and takes a "
          + "–2 penalty to Strength and Dexterity. A Heal DC 15 check is needed to recover from "
          + "mild hypothermia.")
      .adjustment("Cannot run")
      .adjustment("Cannot charge")
      .adjustment(new AbilityAdjustment(Ability.STRENGTH, new Modifier(-2,
          Modifier.Type.GENERAL, "Mild Hypothermia")))
      .adjustment(new AbilityAdjustment(Ability.DEXTERITY, new Modifier(-2,
          Modifier.Type.GENERAL, "Mild Hypothermia")))
      .predefined()
      .icon(R.drawable.noun_icicles_2062030)
      .dmOnly()
      .build();
  public static final ConditionData HYPOTHERMIA_MODERATE =
      ConditionData.newBuilder("Moderate Hypothermia")
          .description("An character with moderate hypothermia moves at half speed and takes a "
              + "–6 penalty to Strength and Dexterity. A Heal DC 15 check is needed to recover from "
              + "moderate hypothermia to mild hypothermia.")
          .adjustment("Cannot run")
          .adjustment("Cannot charge")
          .adjustment("half speed")
          .adjustment(new AbilityAdjustment(Ability.STRENGTH, new Modifier(-6,
              Modifier.Type.GENERAL, "Mild Hypothermia")))
          .adjustment(new AbilityAdjustment(Ability.DEXTERITY, new Modifier(-6,
              Modifier.Type.GENERAL, "Mild Hypothermia")))
          .predefined()
          .icon(R.drawable.noun_icicles_2062030)
          .color(R.color.moderate)
          .dmOnly()
          .build();
  public static final ConditionData HYPOTHERMIA_SEVERE =
      ConditionData.newBuilder("Severe Hypothermia")
          .description("An character with severate hypothermia is disabled.")
          .adjustment("Single move or standard action")
          .adjustment("Cannot run")
          .adjustment("Cannot charge")
          .adjustment("half speed")
          .adjustment(new AbilityAdjustment(Ability.STRENGTH, new Modifier(-6,
              Modifier.Type.GENERAL, "Mild Hypothermia")))
          .adjustment(new AbilityAdjustment(Ability.DEXTERITY, new Modifier(-6,
              Modifier.Type.GENERAL, "Mild Hypothermia")))
          .predefined()
          .icon(R.drawable.noun_icicles_2062030)
          .color(R.color.severe)
          .dmOnly()
          .build();
  public static final ConditionData INCORPOREAL = ConditionData.newBuilder("Incorporeal")
      .description("Having no physical body. Incorporeal creatures are immune to all "
          + "nonmagical attack forms. They can be harmed only by other incorporeal creatures,"
          + " +1 or better magic weapons, spells, spell-like effects, or supernatural effects. "
          + "(See Incorporeality under Special Abilities, earlier in this chapter.)")
      .adjustment("No physical body")
      .adjustment("Immune to nonmagical attacks")
      .adjustment("50% chance to ignore most magical attacks")
      .predefined()
      .dmOnly()
      .icon(R.drawable.icons8_ghost_48)
      .build();
  public static final ConditionData INVISIBLE = ConditionData.newBuilder("Invisible")
      .description("Visually undetectable. An invisible creature gains a +2 bonus on attack "
          + "rolls against sighted opponents, and ignores its opponents’ Dexterity bonuses to"
          + " AC (if any). (See Invisibility, under Special Abilities, earlier in this "
          + "chapter.)")
      .adjustment("Visually undetectable")
      .adjustment("+2 attack")
      .adjustment("ignore Dex to AC")
      .predefined()
      .icon(R.drawable.icons8_invisible_48)
      .build();
  public static final ConditionData KNOCKED_DOWN = ConditionData.newBuilder("Knocked Down")
      .description("Depending on their size, creatures can be knocked down by winds of high "
          + "velocity (see Table 3–24: Wind Effects, page 95). Creatures on the ground are "
          + "knocked prone by the force of the wind. Flying creatures are instead blown back "
          + "1d6×10 feet.")
      .adjustment("Ground: Knocked prone")
      .adjustment("Flying: Pushed back")
      .predefined()
      .notDismissable()
      .icon(R.drawable.icons8_boxing_glove_48)
      .build();
  public static final ConditionData NAUSEATED = ConditionData.newBuilder("Nauseated")
      .description("Experiencing stomach distress. Nauseated creatures are unable to attack, "
          + "cast spells, concentrate on spells, or do  anything else requiring attention. "
          + "The only action such a character can take is a single move action per turn.")
      .adjustment("Stomach distress")
      .adjustment("Unable to attaack")
      .adjustment("Unable to cast spells")
      .adjustment("Single move only")
      .predefined()
      .icon(R.drawable.icons8_vomited_48)
      .build();
  public static final ConditionData PANICKED = ConditionData.newBuilder("Panicked")
      .description("A panicked creature must drop anything it holds and flee at top speed "
          + "from the source of its fear, as well as any other dangers it encounters, along a"
          + " random path. It can’t take any other actions. I.addition, the creature takes a "
          + "–2 penalty on all saving throws, skill checks, and ability checks. If cornered, "
          + "a panicked creature cowers and does not attack, typically using the total defense "
          + "action in combat. A panicked creature can use special abilities, including "
          + "spells, to flee; indeed, the creature must use such means if they are the only "
          + "way to escape.\n"
          + "Panicked is a more extreme state of fear than shaken or frightened.")
      .adjustment("Drop anything and flee at top speed")
      .adjustment("-2 on saves")
      .adjustment("-2 on skills")
      .adjustment("-2 on abilith checks")
      .predefined()
      .icon(R.drawable.icons8_exercise_48)
      .build();
  public static final ConditionData PARALYZED = ConditionData.newBuilder("Paralyzed")
      .description("A paralyzed character is frozen in place and unable to move or act, such "
          + "as by the hold person spell. A paralyzed character has effective Dexterity and "
          + "Strength scores of 0 and is helpless, but can take purely mental actions. A "
          + "winged creature flying in the air at the time that it becomes paralyzed cannot "
          + "flap its wings and falls. A paralyzed swimmer can’t swim and may drown. A "
          + "creature can move through a space occupied by a paralyzed creature—ally or not. "
          + "Each square occupied by a paralyzed creature, however, counts as 2 squares.")
      .adjustment("Frozen, unable to act")
      .adjustment(new AbilityAdjustment(Ability.STRENGTH,
          new Modifier(-50, Modifier.Type.GENERAL, "Paralyzed")))
      .adjustment(new AbilityAdjustment(Ability.DEXTERITY,
          new Modifier(-50, Modifier.Type.GENERAL, "Paralyzed")))
      .adjustments(HELPLESS)
      .predefined()
      .icon(R.drawable.icons8_no_running_48)
      .build();
  public static final ConditionData PARALYZED_DEXTERITY_0 =
      ConditionData.newBuilder("Paralyzed (Dex 0)", PARALYZED)
      .build();
  public static final ConditionData UNCONSCIOUS = ConditionData.newBuilder("Unconscious")
      .description("Knocked out and helpless. Unconsciousness can result from having current "
          + "hit points between –1 and –9, or from nonlethal damage in excess of current hit "
          + "points.")
      .adjustment("Knocked out")
      .adjustments(HELPLESS)
      .icon(R.drawable.icons8_sleeping_filled_50)
      .predefined()
      .build();
  public static final ConditionData PETRIFIED = ConditionData.newBuilder("Petrified")
      .description("A petrified character has been turned to stone and is considered "
          + "unconcious. If a petrified character cracks or breaks, but the broken pieces are"
          + " joined with the body as he returns to flesh, he is unharmed. If the character’s"
          + " petrified body is incomplete when it returns to flesh, the body is likewise "
          + "incomplete and the DM must assign some amount of permanent hit point loss and/or"
          + " debilitation.")
      .adjustment("Turned to stone")
      .adjustments(UNCONSCIOUS)
      .predefined()
      .icon(R.drawable.icons8_statue_48)
      .build();
  public static final ConditionData PINNED = ConditionData.newBuilder("Pinned")
      .description("Held immobile (but not helpless) in a grapple.")
      .adjustment("Held immobile")
      .adjustment("Not helpless")
      .predefined()
      .icon(R.drawable.icons8_pin_48)
      .build();
  public static final ConditionData PRONE = ConditionData.newBuilder("Prone")
      .description("The character is on the ground. An attacker who is prone has a –4 penalty"
          + " on melee attack rolls and cannot use a ranged weapon (except for a crossbow). A "
          + "defender who is prone gains a +4 bonus to Armor Class against ranged attacks, but "
          + "takes a -4 penalty to AC against melee attacks. Standing up is a move-equivalent "
          + "action that provokes an attack of opportunity.")
      .adjustment("On the ground")
      .adjustment("-4 Melee Attack")
      .adjustment("Cannot use ranged (except crossbow)")
      .adjustment("+4 AC ranged")
      .adjustment("-4 AC melee")
      .predefined()
      .icon(R.drawable.icons8_pushups_48)
      .build();
  public static final ConditionData SHAKEN = ConditionData.newBuilder("Shaken")
      .description("A shaken character takes a –2 penalty on attack rolls, saving throws, "
          + "skill checks, and ability checks. Shaken is a less severe state of fear than "
          + "frightened or panicked.")
      .adjustment("-2 Attack")
      .adjustment("-2 saves")
      .adjustment("-2 skills")
      .adjustment("-2 ability checks")
      .predefined()
      .icon(R.drawable.icons8_cocktail_shaker_50)
      .build();
  public static final ConditionData SICKENED = ConditionData.newBuilder("Sickened")
      .description("The character takes a –2 penalty on all attack rolls, weapon damage "
          + "rolls, saving throws, skill checks, and ability checks.")
      .adjustment("-2 attack")
      .adjustment("-2 damage")
      .adjustment("-2 saves")
      .adjustment("-2 skills")
      .adjustment("-2 ability checks")
      .predefined()
      .icon(R.drawable.icons8_fever_50)
      .build();
  public static final ConditionData SNOW_BLIND = ConditionData.newBuilder("Snow Blind")
      .description("Spending too much time in a snow field or similar area on a sunny day "
          + "can make a character snow blind. A snow blind character suffers -2 to AC, loses any "
          + "Dexterity bonus to AC, moves at half speed only, receives a -4 to Dexterity based skills,"
          + "has -4 on Search, Spot and other visions based skills, and has a 20% miss chance to "
          + "hit opponents.")
      .adjustment("-2 AC")
      .adjustment("No Dex to AC")
      .adjustment("Half speed")
      .adjustment("-4 Dex skills")
      .adjustment("-4 Search")
      .adjustment("-4 Spot")
      .adjustment("-4 vision skills")
      .adjustment("20% miss chance")
      .predefined()
      .icon(R.drawable.noun_goggles_1126061)
      .dmOnly()
      .build();
  public static final ConditionData STABLE = ConditionData.newBuilder("Stable")
      .description("A character who was dying but who has stopped losing hit points and still"
          + " has negative hit points is stable. The character is no longer dying, but is "
          + "still unconscious. If the character has become stable because of aid from another "
          + "character (such as a Heal check or magical healing), then the character no "
          + "longer loses hit points. He has a 10% chance each hour of becoming conscious"
          + " and disabled (even though his hit points are still negative). If the character "
          + "became stable on his own and hasn’t had help, he is still at risk of losing "
          + "hit points. Each hour, he has a 10% chance of becoming conscious and disabled. "
          + "Otherwise he loses 1 hit point.")
      .adjustment("Negative hit points")
      .adjustment("No more losing hp")
      .predefined()
      .icon(R.drawable.icons8_dry_flat_50)
      .build();
  public static final ConditionData STAGGERED = ConditionData.newBuilder("Staggered")
      .description("A character whose nonlethal damage exactly equals his current hit points "
          + "is staggered. A staggered character may take a single move action or standard "
          + "action each round (but not both, nor can she take full-round actions). A "
          + "character whose current hit points exceed his nonlethal damage is no longer "
          + "staggered; a character whose nonlethal damage exceeds his hit points becomes "
          + "unconscious.")
      .adjustment("Nonlethal equal current hp")
      .adjustment("single move or standard action only")
      .predefined()
      .icon(R.drawable.icons8_elderly_person_48)
      .build();
  public static final ConditionData STUNNED = ConditionData.newBuilder("Stunned")
      .description("A stunned creature drops everything held, can’t take actions, takes a –2 "
          + "penalty to AC, and loses his Dexterity bonus to AC (if any).")
      .adjustment("Drop everything")
      .adjustment("Can't take actions")
      .adjustment("-2 AC")
      .adjustment("No Dex to AC")
      .predefined()
      .icon(R.drawable.icons8_action_50)
      .build();
  public static final ConditionData SURPRISED = ConditionData.newBuilder("Surprised")
      .description("A character who is surprised in the startEncounter and thus can't initially act.")
      .adjustment("Cannot act (in surprise round)")
      .predefined()
      .dmOnly()
      .icon(R.drawable.noun_gift_15044)
      .build();
  public static final ConditionData TURNED = ConditionData.newBuilder("Turned")
      .description("Affected by a turn undead attempt. Turned undead flee for 10 rounds (1 "
          + "minute) by the best and fastest means available to them. If they cannot flee, "
          + "they cower.")
      .adjustment("Fee by fastest means")
      .predefined()
      .icon(R.drawable.icons8_cross_50)
      .build();
  public static final ConditionData UNCONSCIOUS_INTELLIGENCE_0 =
      ConditionData.newBuilder("Unconscious (Int 0)", UNCONSCIOUS)
          .build();
  public static final ConditionData UNCONSCIOUS_WISDOM_0 =
      ConditionData.newBuilder("Unconscious (Wis 0)", UNCONSCIOUS)
          .build();
  public static final ConditionData UNCONSCIOUS_CHARISMA_0 =
      ConditionData.newBuilder("Unconscious (Cha 0)", UNCONSCIOUS)
          .build();
  public static final ConditionData WHITEOUT = ConditionData.newBuilder("Whiteout")
      .description("In snowstorms or blizzars with strong or stronger winds, characters get into "
          + "a whiteout. Characters suffer a -2 to AC, no Dexterity to AC, half speed, "
          + "-4 on Dexterity checks, -4 on Search, Spot and other vision based skills, "
          + "and only have a visibility of 5 ft.")
      .adjustment("-2 AC")
      .adjustment("No Dex to AC")
      .adjustment("Half speed")
      .adjustment("-4 on Dex checks")
      .adjustment("-4 Search")
      .adjustment("-4 Spot")
      .adjustment("-4 vision skills")
      .adjustment("visibility 5ft")
      .icon(R.drawable.noun_snow_1967559)
      .predefined()
      .dmOnly()
      .build();

  public static ImmutableList<ConditionData> CONDITIONS =
      ImmutableList.sortedCopyOf(new ImmutableList.Builder<ConditionData>()
          .add(ABILITY_DAMAGED)
          .add(ABILITY_DRAINED)
          .add(BLINDED)
          .add(BLOWN_AWAY)
          .add(CHECKED)
          .add(CONFUSED)
          .add(COLD)
          .add(COLD_SEVERE_UNPROTECTED)
          .add(COLD_SEVERE_LEVEL_1)
          .add(COLD_EXTREME_UNPROTECTED)
          .add(COLD_EXTREME_LEVEL_1)
          .add(COLD_EXTREME_LEVEL_2)
          .add(COLD_UNEARTHLY_UNPROTECTED)
          .add(COLD_UNEARTHLY_LEVEL_1)
          .add(COLD_UNEARTHLY_LEVEL_2)
          .add(COLD_UNEARTHLY_LEVEL_3)
          .add(COWERING)
          .add(DAZED)
          .add(DAZZLED)
          .add(DEAD)
          .add(DEAFENED)
          .add(DISABLED)
          .add(DYING)
          .add(ENERGY_DRAINED)
          .add(ENTANGLED)
          .add(EXHAUSTED)
          .add(FASCINATED)
          .add(FATIGUED)
          .add(FLAT_FOOTED)
          .add(FRIGHTENED)
          .add(FROSTBITTEN)
          .add(GRAPPLING)
          .add(HELPLESS)
          .add(HYPOTHERMIA_MILD)
          .add(HYPOTHERMIA_MODERATE)
          .add(HYPOTHERMIA_SEVERE)
          .add(INCORPOREAL)
          .add(INVISIBLE)
          .add(KNOCKED_DOWN)
          .add(NAUSEATED)
          .add(PANICKED)
          .add(PARALYZED)
          .add(PETRIFIED)
          .add(PINNED)
          .add(PRONE)
          .add(SHAKEN)
          .add(SICKENED)
          .add(SNOW_BLIND)
          .add(STABLE)
          .add(STAGGERED)
          .add(STUNNED)
          .add(SURPRISED)
          .add(TURNED)
          .add(UNCONSCIOUS)
          .add(WHITEOUT)
          .build());

  public static final ImmutableMap<String, ConditionData> CONDITIONS_BY_NAME = ImmutableMap.copyOf(
      CONDITIONS.stream().collect(Collectors.toMap(ConditionData::getName, i -> i)));

  private Conditions() {
  }

  public static List<String> getNames() {
    // Use the list to preserve order.
    return CONDITIONS.stream().map(ConditionData::getName).collect(Collectors.toList());
  }

  public static Optional<ConditionData> get(String name) {
    if (CONDITIONS_BY_NAME == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(CONDITIONS_BY_NAME.get(name));
  }
}
