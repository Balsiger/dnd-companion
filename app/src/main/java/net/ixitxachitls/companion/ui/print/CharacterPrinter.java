package net.ixitxachitls.companion.ui.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Level;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.values.Distance;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.data.values.ModifiedValue;
import net.ixitxachitls.companion.data.values.Modifier;

import java.util.Optional;

/**
 * Class encapsulating printing of a character.
 */
public class CharacterPrinter extends BasePrinter<Character> {

  public CharacterPrinter(Context context) {
    super(context);
  }

  private void printAbility(Canvas canvas, String abbreviation, String ability,
                            ModifiedValue score, int x, int y) {
    canvas.drawText(abbreviation, x, y, largeStyle);
    canvas.drawText(ability, x, y + 6, tinyStyle);

    printSmallValue(canvas, score.totalFormatted(), "TOTAL", Optional.empty(), x + 30, y,
        x + 48);

    canvas.drawText("=", x + 50, y, valueStyle);

    printSmallValue(canvas, String.valueOf(score.getBase()
            + score.get(Modifier.Type.RACIAL).stream().mapToInt(m -> m.getValue()).max().orElse(0)),
        "BASE +", Optional.of("RACIAL"), x + 60, y, x + 80);

    canvas.drawText("+", x + 82, y, valueStyle);

    printSmallValue(canvas, String.valueOf(score.get(Modifier.Type.ENHANCEMENT).stream()
            .mapToInt(m -> m.getValue()).max().orElse(0)), "ENHA.", Optional.empty(), x + 90, y,
        x + 105);

    canvas.drawText("+", x + 107, y, valueStyle);

    printSmallValue(canvas, String.valueOf(
        score.getNotTotal(Modifier.Type.RACIAL, Modifier.Type.ENHANCEMENT)), "MISC",
        Optional.empty(), x + 115, y, x + 130);

    canvas.drawLine(x + 140, y - 15, x + 165, y - 15, lineStyle);
    canvas.drawLine(x + 165, y - 15, x + 165, y + 10, lineStyle);
    canvas.drawLine(x + 140, y + 10, x + 165, y + 10, lineStyle);
    canvas.drawLine(x + 140, y - 15, x + 140, y + 10, lineStyle);
    canvas.drawText(ability, x + 142, y + 15, tinyStyle);
    canvas.drawText("MODIFIER", x + 142, y + 19, tinyStyle);
    canvas.drawText(String.valueOf(Ability.modifier(score.total())), x + 149, y + 2, valueStyle);
  }

  @Override
  protected boolean printPage(Character character, Canvas canvas, int count) {
    switch (count) {
      case 1:
        printPage1(character, canvas);
        return true;

      case 2:
        printPage2(character, canvas);
        return false;

      default:
        return false;
    }
  }

  private void printPage1(Character character, Canvas canvas) {
    // Title.
    Bitmap title = BitmapFactory.decodeResource(context.getResources(),
        R.drawable.character_sheet_title);
    Bitmap and = BitmapFactory.decodeResource(context.getResources(),
        R.drawable.character_sheet_and);
    canvas.drawBitmap(title, null, new Rect(20, 10, 565, 57), null);

    // General section.
    printValue(canvas, character.getName(), "CHARACTER NAME", 25, 75, 250);
    printValue(canvas, character.getPlayer().getNickname(), "PLAYER NAME", 260, 75, 565);

    printValue(canvas, Level.summarized(character.getLevels()), "CLASS AND LEVEL", 25, 105, 250);
    printValue(canvas, "" + character.getLevel(), "ECL", 260, 105, 290);
    printValue(canvas,
        character.getRace().isPresent() ? character.getRace().get().getName() : "",
        "RACE/TEMPLATE", 300, 105, 400);
    printValue(canvas, character.getSize().getName(), "SIZE", 410, 105, 490);
    printValue(canvas, character.getGender().getName(), "GENDER", 500, 105, 565);

    printValue(canvas, character.getAlignment().getShortName(), "ALIGNMENT", 25, 135, 70);
    printValue(canvas, character.getReligion(), "RELIGION/PATRON DEITY", 80, 135, 200);
    printValue(canvas, character.getHeight(), "HEIGHT", 210, 135, 250);
    printValue(canvas, character.getWeight(), "WEIGHT", 260, 135, 300);
    printValue(canvas, character.getLooks(), "LOOKS", 310, 135, 565);

    printTitle(canvas, Optional.of(and), "ABILITY SCORES", 25, 160, 190);
    printAbility(canvas, "STR", "STRENGTH", character.getStrength(), 20, 205);
    printAbility(canvas, "DEX", "DEXTERITY", character.getDexterity(), 20, 242);
    printAbility(canvas, "CON", "CONSTITUTION", character.getConstitution(), 20, 279);
    printAbility(canvas, "INT", "INTELLIGENCE", character.getIntelligence(), 20, 316);
    printAbility(canvas, "WIS", "WISDOM", character.getWisdom(), 20, 353);
    printAbility(canvas, "CHA", "CHARISMA", character.getCharisma(), 20, 390);

    printTitle(canvas, Optional.of(and), "COMBAT OPTIONS", 200, 160, 450);
    canvas.drawText("BASE ATTACK BONUS", 200, 200, largeStyle);
    canvas.drawText("+" + character.getBaseAttackBonus(), 332, 200, valueStyle);
    canvas.drawLine(330, 205, 450, 205, lineStyle);

    int count = 0;
    for (Item item : character.getItems()) {
      if (count < 4 && item.isWeapon() && character.isWearing(item)) {
        printWeapon(canvas, character, item, 200, 225 + 50 * count++);
      }
    }

    printTitle(canvas, Optional.empty(), "HIT POINTS", 460, 160, 565);
    canvas.drawLine(455, 160, 455, 600, lineStyle);
    canvas.drawLine(565, 160, 565, 600, lineStyle);
    canvas.drawLine(455, 600, 565, 600, lineStyle);

    canvas.drawText(String.valueOf(character.getMaxHp()), 500, 200, largeStyle);

    int speed = character.getSpeed().total();
    canvas.drawText("SPEED", 20, 430, largeStyle);
    canvas.drawText((speed * 5) + " feet (" + speed + " squares)", 67, 427, valueStyle);
    canvas.drawLine(65, 430, 250, 430, lineStyle);

    canvas.drawText("INITIATIVE", 260, 430, largeStyle);
    canvas.drawText(character.getInitiative().totalFormatted(), 332, 427, valueStyle);
    canvas.drawLine(330, 430, 450, 430, lineStyle);

    ModifiedValue grapple = character.getGrapple();
    canvas.drawText("GRAPPLE", 20, 460, largeStyle);
    canvas.drawText(grapple.totalFormatted(), 87, 457, valueStyle);
    canvas.drawLine(85, 460, 110, 460, lineStyle);

    canvas.drawText("=", 115, 460, valueStyle);

    printSmallValue(canvas, String.valueOf(character.getBaseAttackBonus()), "BASE",
        Optional.empty(), 130, 455, 160);

    canvas.drawText("+", 165, 460, valueStyle);

    printSmallValue(canvas, String.valueOf(character.getStrengthModifier()), "STRENGTH",
        Optional.empty(), 180, 455, 210);

    canvas.drawText("+", 215, 460, valueStyle);

    printSmallValue(canvas, String.valueOf(character.getSize().getModifier()), "SIZE",
        Optional.empty(), 230, 455, 260);

    canvas.drawText("+", 265, 460, valueStyle);

    printSmallValue(canvas, "", "MISC", Optional.empty(), 280, 455, 310);

    printTitle(canvas, Optional.of(and), "SAVING THROWS", 25, 480, 450);
    printSave(canvas, "FORTITUDE", "(CONSTITUTION)", character.getFortitude(), 20, 520);
    printSave(canvas, "REFLEX", "(DEXTERITY)", character.getReflex(), 20, 550);
    printSave(canvas, "WILL", "(WISDOM)", character.getWill(), 20, 580);

    printTitle(canvas, Optional.of(and), "ARMOR CLASS", 25, 600, 450);
    canvas.drawText("AC", 20, 640, largeStyle);
    ModifiedValue ac = character.getNormalArmorClass();
    printSmallValue(canvas, ac.totalFormatted(), "TOTAL", Optional.empty(), 40, 640, 60);

    canvas.drawText("= 10", 65, 640, valueStyle);

    canvas.drawText("+", 90, 647, valueStyle);

    int armor = ac.total(m -> m.getType() == Modifier.Type.ARMOR);
    printSmallValue(canvas, String.valueOf(armor), "ARMOR", Optional.empty(), 100, 640, 120);

    canvas.drawText("+", 125, 647, valueStyle);

    int shield = ac.total(m -> m.getType() == Modifier.Type.SHIELD);
    printSmallValue(canvas, String.valueOf(shield), "SHIELD", Optional.empty(), 135, 640, 155);

    canvas.drawText("+", 160, 647, valueStyle);

    int dex = ac.total(m -> m.getSource().equals("Dexterity"));
    printSmallValue(canvas, String.valueOf(dex), "DEX", Optional.empty(), 170, 640, 190);

    canvas.drawText("+", 195, 647, valueStyle);

    int size = ac.total(m -> m.getSource().equals("Size"));
    printSmallValue(canvas, String.valueOf(size), "SIZE", Optional.empty(), 205, 640, 225);

    canvas.drawText("+", 230, 647, valueStyle);

    int natural = ac.total(m -> m.getType() == Modifier.Type.NATURAL_ARMOR);
    printSmallValue(canvas, String.valueOf(natural), "NATURAL", Optional.of("ARMOR"), 240, 640,
        260);

    canvas.drawText("+", 265, 647, valueStyle);

    int deflection = ac.total(m -> m.getType() == Modifier.Type.DEFLECTION);
    printSmallValue(canvas, String.valueOf(deflection), "NATURAL", Optional.of("ARMOR"), 275,
        640, 295);

    canvas.drawText("+", 300, 647, valueStyle);

    int misc = ac.total() - armor - shield - dex - size - natural - deflection;
    printSmallValue(canvas, String.valueOf(deflection), "MISC", Optional.empty(), 310,
        640, 330);

    canvas.drawText("TOUCH AC", 20, 670, largeStyle);
    printSmallValue(canvas, character.getTouchArmorClass().totalFormatted(), "",
        Optional.empty(), 90, 670, 110);

    canvas.drawText("FLAT-FOOTED AC", 120, 670, largeStyle);
    printSmallValue(canvas, character.getTouchArmorClass().totalFormatted(), "",
        Optional.empty(), 230, 670, 250);

    printTitle(canvas, Optional.empty(), "SPECIAL DEFENSES", 460, 600, 565);
    canvas.drawLine(455, 600, 455, 750, lineStyle);
    canvas.drawLine(565, 600, 565, 750, lineStyle);
    canvas.drawLine(455, 750, 565, 750, lineStyle);
  }

  private void printPage2(Character character, Canvas canvas) {

  }

  private void printSave(Canvas canvas, String label, String subLabel, ModifiedValue value,
                         int x, int y) {
    canvas.drawText(label, x, y, largeStyle);
    canvas.drawText(subLabel, x, y + 10, smallStyle);

    printSmallValue(canvas, value.totalFormatted(), "TOTAL", Optional.empty(), x + 80, y,
        x + 110);

    canvas.drawText("=", x + 115, y + 7, valueStyle);

    int base = value.total(t -> Templates.get().getLevelTemplates().get(t.getSource()).isPresent());
    printSmallValue(canvas, String.valueOf(base), "BASE", Optional.empty(),
        x + 135, y, x + 155);

    canvas.drawText("+", x + 160, y + 7, valueStyle);

    int ability = value.total(t -> t.getSource().equals("Constitution")
        || t.getSource().equals("Wisdom") || t.getSource().equals("Dexterity"));
    printSmallValue(canvas, String.valueOf(ability), "ABILITY", Optional.empty(),
        x + 175, y, x + 195);

    canvas.drawText("+", x + 200, y + 7, valueStyle);

    int misc = value.total() - base - ability;
    printSmallValue(canvas, String.valueOf(misc), "MISC", Optional.empty(),
        x + 215, y, x + 235);

    canvas.drawText("+", x + 240, y + 7, valueStyle);

    printSmallValue(canvas, "", "TEMPORARY", Optional.empty(),
        x + 255, y, x + 300);
  }

  private void printWeapon(Canvas canvas, Character character, Item item, int x, int y) {
    canvas.drawText(item.getName(), x + 2, y, smallStyle);
    canvas.drawLine(x, y + 3, x + 150, y + 3, lineStyle);
    canvas.drawText("WEAPON", x, y + 8, tinyStyle);

    canvas.drawText(item.getWeaponType().getShortName(), x + 157, y, smallStyle);
    canvas.drawLine(x + 155, y + 3, x + 175, y + 3, lineStyle);
    canvas.drawText("TYPE", x + 155, y + 8, tinyStyle);

    Optional<Distance> range = item.range();
    canvas.drawText(range.isPresent() ? range.get().toString() : "-", x + 182, y, smallStyle);
    canvas.drawLine(x + 180, y + 3, x + 250, y + 3, lineStyle);
    canvas.drawText("RANGE", x + 180, y + 8, tinyStyle);

    canvas.drawText(character.formatAttacks(item), x + 2, y + 20, smallStyle);
    canvas.drawLine(x, y + 23, x + 150, y + 23, lineStyle);
    canvas.drawText("ATTACK", x, y + 28, tinyStyle);

    canvas.drawText(character.computeDamage(item).toString(), x + 157, y + 20, smallStyle);
    canvas.drawLine(x + 155, y + 23, x + 200, y + 23, lineStyle);
    canvas.drawText("DAMAGE", x + 155, y + 28, tinyStyle);

    canvas.drawText(character.formatCritical(item), x + 207, y + 20, smallStyle);
    canvas.drawLine(x + 205, y + 23, x + 250, y + 23, lineStyle);
    canvas.drawText("CRITICAL", x + 205, y + 28, tinyStyle);
  }

}
