/*
 * Copyright (c) 2017-{2018} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Player Companion.
 *
 * The Player Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Player Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.rules;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.data.values.Condition;
import net.ixitxachitls.companion.data.values.Duration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Rules for conditions.
 */
public class Conditions {

  public static final Condition FLAT_FOOTED = new Condition("Flat-Footed",
      "A character who has not yet acted during a combat is flat-footed, not yet reacting "
          + "normally to the situation. A flat-footed character loses his Dexterity bonus to "
          + "AC (if any) and cannot make attacks of opportunity.",
      "Not yet acted, no Dex to AC, no attacks of opprtunity.");
  public static final Condition SURPRISED = new Condition("Surprised",
      "A character who is surprised in the encounter and thus can't initially act.",
      "Cannot act in the surprise round.");

  public static ImmutableList<Condition> CONDITIONS = new ImmutableList.Builder<Condition>()
      .add(new Condition("Ability Damaged",
          "The character has temporarily lost 1 or more ability score points. Lost points return "
              + "at a rate of 1 per day unless noted otherwise by the condition dealing the "
              + "damage. A character with Strength 0 falls to the ground and is helpless. A "
              + "character with Dexterity 0 is paralyzed. A character with Constitution 0 is dead. "
              + "A character with Intelligence, Wisdom, or Charisma 0 is unconscious. (See Ability "
              + "Score Loss under Special Abilities earlier in this chapter.)\n"
              + "Ability damage is different from penalties to ability scores, which go away when "
              + "the conditions causing them (fatigue, entanglement, and so on) go away.",
          "Lost one or more ability score points (special effects if score is 0)."))
      .add(new Condition("Ability Drained",
          "The character has permanently lost 1 or more ability score points. The character can "
              + "regain these points only through magical means. A character with Strength 0 falls "
              +"to the ground and is helpless. A character with Dexterity 0 is paralyzed. A "
              + "character with Constitution 0 is dead. A character with Intelligence, Wisdom, or "
              + "Charisma 0 is unconscious. (See Ability Score Loss under Special Abilities "
              + "earlier in this chapter.)",
          "Permanently lost one or more ability score points (special effects if score is 0)."))
      .add(new Condition("Blinded",
          "The character cannot see. He takes a –2 penalty to Armor Class, loses his Dexterity "
              + "bonus to AC (if any), moves at half speed, and takes a –4 penalty on Search "
              + "checks and on most Strength- and Dexterity-based skill checks. All checks and "
              + "activities that rely on vision (such as reading and Spot checks) automatically "
              + "fail. All opponents are considered to have total concealment (50% miss chance) "
              + "to the blinded character.\n"
              + "Characters who remain blinded for a long time grow accustomed to these drawbacks "
              + "and can overcome some of them (DM’s discretion).",
          "Cannot see, -2 AC, no Dex bonus to AC, half speed, -4 on Search, Str and Dex checks, "
              + "50% miss chance."))
      .add(new Condition("Blown Away",
          "Depending on its size, a creature can be blown away by winds of high velocity (see "
              + "Table 3–24, page 95). A creature on the ground that is blown away is knocked "
              + "down and rolls 1d4×10 feet, taking 1d4 points of nonlethal damage per 10 feet. "
              + "A flying creature that is blown away is blown back 2d6×10 feet and takes 2d6 points "
              + "of nonlethal damage due to battering and buffering.",
          "Ground: knocked down, rolled 1d4x10 ft and 1d4 nonlethal per 10 feet, "
              + "Flying: blow back 2d6x10 ft and 2d6 nonlethal."))
      .add(new Condition("Checked",
          "Prevented from achieving forward motion by an applied force, such as wind. Checked "
              + "creatures on the ground merely stop. Checked flying creatures move back a "
              + "distance specified in the description of the effect.",
          "Unabled to move forward (ground) or moved back (flying)."))
      .add(new Condition("Confused",
          "A confused character’s actions are determined by rolling d% at the beginning of his "
              + "turn: 01–10, attack caster with melee or ranged weapons (or close with caster "
              + "if attacking is not possible); 11–20, act normally; 21–50, do nothing but babble "
              + "incoherently; 51–70, flee away from caster at top possible speed; 71–100, attack "
              + "nearest creature (for this purpose, a familiar counts as part of the subject’s "
              + "self ). A confused character who can’t carry out the indicated action does "
              + "nothing but babble incoherently. Attackers are not at any special advantage when "
              + "attacking a confused character. Any confused character who is attacked "
              + "automatically attacks its attackers on its next turn, as long as it is still "
              + "confused when its turn comes. A confused character does not make attacks of "
              + "opportunity against any creature that it is not already devoted to attacking "
              + "(either because of its most recent action or because it has just been attacked).",
          "Character's actions are determined randomly."))
      .add(new Condition("Cowering",
          "The character is frozen in fear and can take no actions. A cowering character takes a "
              + "–2 penalty to Armor Class and loses her Dexterity bonus (if any).",
          "Cannot take actions, AC -2, no Dex to AC."))
      .add(new Condition("Dazed",
          "The creature is unable to act normally. A dazed creature can take no actions, but has "
              + "no penalty to AC. A dazed condition typically lasts 1 round.",
          "No action, but no AC penalty.", Duration.rounds(1)))
      .add(new Condition("Dazzled",
          "The creature is unable to see well because of overstimulation of the eyes. A dazzled "
              + "creature takes a –1 penalty on attack rolls, Search checks, and Spot checks.",
          "Not see well, -1 Attack, Search, Spot."))
      .add(new Condition("Dead",
          "The character’s hit points are reduced to –10, his Constitution drops to 0, or he is "
              + "killed outright by a spell or effect. The character’s soul leaves his body. Dead "
              + "characters cannot benefit from normal or magical healing, but they can be "
              + "restored to life via magic. A dead body decays normally unless magically "
              + "preserved, but magic that restores a dead character to life also restores the "
              + "body either to full health or to its condition at the time of death (depending "
              + "on the spell or device). Either way, resurrected characters need not worry about "
              + "rigor mortis, decomposition, and other conditions that affect dead bodies.",
          "He's dead, Jim."))
      .add(new Condition("Deafened",
          "A deafened character cannot hear. She takes a –4 penalty on initiative checks, "
              + "automatically fails Listen checks, and has a 20% chance of spell failure when "
              + "casting spells with verbal components.\n"
              + "Characters who remain deafened for a long time grow accustomed to these drawbacks "
              + "and can overcome some of them (DM’s discretion).",
          "Cannot hear, -4 Init, fail Listen, 20% spell failure (V)."))
      .add(new Condition("Disabled",
          "A character with 0 hit points, or one who has negative hit points but has become stable "
              + "and conscious, is disabled. A disabled character may take a single move action or "
              + "standard action each round (but not both, nor can she take full-round actions). "
              + "She moves at half speed. Taking move actions doesn’t risk further injury, but "
              + "performing any standard action (or any other action the DM deems strenuous, "
              + "including some free actions such as casting a quickened spell) deals 1 point of "
              + "damage after the completion of the act. Unless the action increased the disabled "
              + "character’s hit points, she is now in negative hit points and dying. A disabled "
              + "character with negative hit points recovers hit points naturally if she is being "
              + "helped. Otherwise, each day she has a 10% chance to start recovering hit points "
              + "naturally starting with that day); otherwise, she loses 1 hit point. Once an "
              + "unaided character starts recovering hit points naturally, she is no longer in "
              + "danger of losing hit points (even if her current hit points are negative).",
          "Single move or standard action (might cause damage) each round."))
      .add(new Condition("Dying",
          "A dying character is unconcious and near death. She has –1 to –9 current hit points. "
              + "A dying character can take no actions and is unconscious. At the end of each "
              + "round starting with the round in which the character dropped below 0 hit points), "
              + "the character rolls d% to see whether she becomes stable. She has a 10% chance to "
              + "become stable. If she does not, she loses 1 hit point. If a dying character "
              + "reaches –10 hit points, she is dead.",
          "Unconcious and near death, taking no actions but loses 1 hit point per round."))
      .add(new Condition("Energy Drained",
          "The character gains one or more negative levels, which might permanently drain the "
              + "character’s levels. If the subject has at least as many negative levels as "
              + "Hit Dice, he dies. Each negative level gives a creature the following penalties: "
              + "–1 penalty on attack rolls, saving throws, skill checks, ability checks; loss of "
              + "5 hit points; and –1 to effective level (for determining the power, duration, DC, "
              + "and other details of spells or special abilities). In addition, a spellcaster "
              + "loses one spell or spell slot from the highest spell level castable.",
          "One ore more negative levels (might be permanent), per level -1 Attack, Saves, Skills, "
              + "Ability, -5 hp, -1 level, highest level spell."))
      .add(new Condition("Entangled",
          "The character is ensnared. Being entangled impedes movement, but does not entirely "
              + "prevent it unless the bonds are anchored to an immobile object or tethered by "
              + "an opposing force. An entangled creature moves at half speed, cannot run or "
              + "charge, and takes a –2 penalty on all attack rolls and a –4 penalty to "
              + "Dexterity. An entangled character who attempts to cast a spell must make a "
              + "Concentration check (DC 15 + the spell’s level) or lose the spell.",
          "Movement impeded, half speed, -2 Attack, -4 Dex, Spell DC 15 + level."))
      .add(new Condition("Exhausted",
          "An exhausted character moves at half speed and takes a –6 penalty to Strength and "
              + "Dexterity. After 1 hour of complete rest, an exhausted character becomes "
              + "fatigued. A fatigued character becomes exhausted by doing something else that "
              + "would normally cause fatigue.",
          "Move half speed, -6 Str and Dex."))
      .add(new Condition("Fascinated",
          "A fascinated creature is entranced by a supernatural or spell effect. The creature "
              + "stands or sits quietly, taking no actions other than to pay attention to the "
              + "fascinating effect, for as long as the effect lasts. It takes a –4 penalty on "
              + "skill checks made as reactions, such as Listen and Spot checks. Any potential "
              + "threat, such as a hostile creature approaching, allows the fascinated creature a "
              + "new saving throw against the fascinating effect. Any obvious threat, such as "
              + "someone drawing a weapon, casting a spell, or aiming a ranged weapon at the "
              + "fascinated creature, automatically breaks the effect. A fascinated creature’s "
              + "ally may shake it free of the spell as a standard action.",
          "Entranced, quiet and passive, -4 skill reactions."))
      .add(new Condition("Fatigued",
          "A fatigued character can neither run nor charge and takes a –2 penalty to Strength and "
              + "Dexterity. Doing anything that would normally cause fatigue causes the fatigued "
              + "character to become exhausted. After 8 hours of complete rest, fatigued "
              + "characters are no longer fatigued.",
          "Cannot run or charge, -2 Str and Dex."))
      .add(FLAT_FOOTED)
      .add(new Condition("Frightened",
          "A frightened creature flees from the source of its fear as best it can. If unable to "
              + "flee, it may fight. A frightened creature takes a –2 penalty on all attack rolls, "
              + "saving throws, skill checks, and ability checks. A frightened creature can use "
              + "special abilities, including spells, to flee; indeed, the creature must use "
              + "such means if they are the only way to escape. Frightened is like shaken, "
              + "except that the creature must flee if possible. Panicked is a more extreme state "
              + "of fear.",
          "Flees from source of fear, -2 Attack, saves, skills, ability checks."))
      .add(new Condition("Grappling",
          "Engaged in wrestling or some other form of hand-to-hand struggle with one or more "
              + "attackers. A grappling character can undertake only a limited number of actions. "
              + "He does not threaten any squares, and loses his Dexterity bonus to AC (if any) "
              + "against opponents he isn’t grappling.",
          "Hand-to-hand struggle, limited actions, does not threaten, no Dex to AC."))
      .add(new Condition("Helpless",
          "A helpless character is paralyzed, held, bound, sleeping, unconscious, or otherwise "
              + "completely at an opponent’s mercy. A helpless target is treated as having a "
              + "Dexterity of 0 (–5 modifier). Melee attacks against a helpless target get a +4 "
              + "bonus (equivalent to attacking a prone target). Ranged attacks gets no "
              + "special bonus against helpless targets. Rogues can sneak attack helpless targets. "
              + "As a full-round action, an enemy can use a melee weapon to deliver a coup de "
              + "grace to a helpless foe. An enemy can also use a bow or crossbow, provided he is "
              + "adjacent to the target. The attacker automatically hits and scores a critical "
              + "hit. (A rogue also gets her sneak attack damage bonus against a helpless foe "
              + "when delivering a coup de grace.) If the defender survives, he must make a "
              + "Fortitude save (DC 10 + damage dealt) or die. Delivering a coup de grace provokes "
              + "attacks of opportunity. Creatures that are immune to critical hits do not take "
              + "critical damage, nor do they need to make Fortitude saves to avoid being killed "
              + "by a coup de grace.",
          "Completely at mercy, melee AC -4, can be sneaked, coup the graced."))
      .add(new Condition("Incorporeal",
          "Having no physical body. Incorporeal creatures are immune to all nonmagical attack "
              + "forms. They can be harmed only by other incorporeal creatures, +1 or better "
              + "magic weapons, spells, spell-like effects, or supernatural effects. "
              + "(See Incorporeality under Special Abilities, earlier in this chapter.)",
          "No physical body, immunte to nonmagical attacks."))
      .add(new Condition("Invisible",
          "Visually undetectable. An invisible creature gains a +2 bonus on attack rolls against "
              + "sighted opponents, and ignores its opponents’ Dexterity bonuses to AC (if any). "
              + "(See Invisibility, under Special Abilities, earlier in this chapter.)",
          "Visually undetectable, +2 attack, ignore Dex to AC."))
      .add(new Condition("Knocked Down",
          "Depending on their size, creatures can be knocked down by winds of high velocity "
              + "(see Table 3–24: Wind Effects, page 95). Creatures on the ground are knocked "
              + "prone by the force of the wind. Flying creatures are instead blown back "
              + "1d6×10 feet.",
          "Knocked prone (ground) or blown back (flying)."))
      .add(new Condition("Nauseated",
          "Experiencing stomach distress. Nauseated creatures are unable to attack, cast spells, "
              + "concentrate on spells, or do  anything else requiring attention. The only action "
              + "such a character can take is a single move action per turn.",
          "Stomach distress, unable to attack, cast spells, single move only."))
      .add(new Condition("Panicked",
          "A panicked creature must drop anything it holds and flee at top speed from the source "
              + "of its fear, as well as any other dangers it encounters, along a random path. It "
              + "can’t take any other actions. In addition, the creature takes a –2 penalty on "
              + "all saving throws, skill checks, and ability checks. If cornered, a panicked "
              + "creature cowers and does not attack, typically using the total defense action in "
              + "combat. A panicked creature can use special abilities, including spells, to flee; "
              + "indeed, the creature must use such means if they are the only way to escape.\n"
              + "Panicked is a more extreme state of fear than shaken or frightened.,",
          "Drop anything and flee at top speed, -2 on saves, skills, ability checks."))
      .add(new Condition("Paralyzed",
          "A paralyzed character is frozen in place and unable to move or act, such as by the hold "
              + "person spell. A paralyzed character has effective Dexterity and Strength scores "
              + "of 0 and is helpless, but can take purely mental actions. A winged creature "
              + "flying in the air at the time that it becomes paralyzed cannot flap its wings "
              + "and falls. A paralyzed swimmer can’t swim and may drown. A creature can move "
              + "through a space occupied by a paralyzed creature—ally or not. Each square "
              + "occupied by a paralyzed creature, however, counts as 2 squares.",
          "Frozen, unable to act, Dex Str to 0 and helpless."))
      .add(new Condition("Petrified",
          "A petrified character has been turned to stone and is considered unconcious. If a "
              + "petrified character cracks or breaks, but the broken pieces are joined with the "
              + "body as he returns to flesh, he is unharmed. If the character’s petrified body is "
              + "incomplete when it returns to flesh, the body is likewise incomplete and the "
              + "DM must assign some amount of permanent hit point loss and/or debilitation.",
          "Turned to stone, considered unconcious."))
      .add(new Condition("Pinned", "Held immobile (but not helpless) in a grapple.",
          "Held immobile, but not helpless."))
      .add(new Condition("Prone",
          "The character is on the ground. An attacker who is prone has a –4 penalty on melee "
              + "attack rolls and cannot use a ranged weapon (except for a crossbow). A defender "
              + "who is prone gains a +4 bonus to Armor Class against ranged attacks, but takes "
              + "a -4 penalty to AC against melee attacks. Standing up is a move-equivalent "
              + "action that provokes an attack of opportunity.",
          "On the ground, -4 Attack (melee), cannot use ranged (except crossbow), +4 AC (ranged), "
              + "-4 AC (melee)."))
      .add(new Condition("Shaken",
          "A shaken character takes a –2 penalty on attack rolls, saving throws, skill checks, "
              + "and ability checks. Shaken is a less severe state of fear than frightened or "
              + "panicked.",
          "-2 Attack, saves, skills, ability checks."))
      .add(new Condition("Sickened",
          "The character takes a –2 penalty on all attack rolls, weapon damage rolls, saving "
              + "throws, skill checks, and ability checks.",
          "-2 attack, damage, saves, skill, ability checks."))
      .add(new Condition("Stable",
          "A character who was dying but who has stopped losing hit points and still has "
              + "negative hit points is stable. The character is no longer dying, but is still "
              + "unconscious. If the character has become stable because of aid from another "
              + "character (such as a Heal check or magical healing), then the character no "
              + "longer loses hit points. He has a 10% chance each hour of becoming conscious and "
              + "disabled (even though his hit points are still negative). If the character "
              + "became stable on his own and hasn’t had help, he is still at risk of losing hit "
              + "points. Each hour, he has a 10% chance of becoming conscious and disabled. "
              + "Otherwise he loses 1 hit point.",
          "Negative hit points but no more losing any."))
      .add(new Condition("Staggered",
          "A character whose nonlethal damage exactly equals his current hit points is staggered. "
              + "A staggered character may take a single move action or standard action each round "
              + "(but not both, nor can she take full-round actions). A character whose current "
              + "hit points exceed his nonlethal damage is no longer staggered; a character whose "
              + "nonlethal damage exceeds his hit points becomes unconscious.",
          "Nonlethal equal current hp, single move or standrd action only."))
      .add(new Condition("Stunned",
          "A stunned creature drops everything held, can’t take actions, takes a –2 penalty to AC, "
              + "and loses his Dexterity bonus to AC (if any).",
          "Drop everything, can't take actions, -2 AC, not Dex to AC."))
      .add(SURPRISED)
      .add(new Condition("Turned",
          "Affected by a turn undead attempt. Turned undead flee for 10 rounds (1 minute) by the "
              + "best and fastest means available to them. If they cannot flee, they cower.",
          "Fleed for 10 rounds by fastest means."))
      .add(new Condition("Unconscious",
          "Knocked out and helpless. Unconsciousness can result from having current hit points "
              + "between –1 and –9, or from nonlethal damage in excess of current hit points.",
          "Knocked out and helpless."))
      .build();

  private Conditions() {}

  public static List<String> getNames() {
    return CONDITIONS.stream().map(Condition::getName).collect(Collectors.toList());
  }
}
