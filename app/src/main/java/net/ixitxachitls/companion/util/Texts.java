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

package net.ixitxachitls.companion.util;

import android.content.Context;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableMap;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.enums.SpellClass;
import net.ixitxachitls.companion.ui.dialogs.SpellDialog;
import net.ixitxachitls.companion.util.commands.AlignmentCommand;
import net.ixitxachitls.companion.util.commands.BoldCommand;
import net.ixitxachitls.companion.util.commands.ClickableCommand;
import net.ixitxachitls.companion.util.commands.ColorCommand;
import net.ixitxachitls.companion.util.commands.CompoundCommand;
import net.ixitxachitls.companion.util.commands.ItalicsCommand;
import net.ixitxachitls.companion.util.commands.ListCommand;
import net.ixitxachitls.companion.util.commands.MessageCommand;
import net.ixitxachitls.companion.util.commands.SizeCommand;
import net.ixitxachitls.companion.util.commands.TableCommand;
import net.ixitxachitls.companion.util.commands.TextCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import androidx.annotation.VisibleForTesting;

/**
 * Utility methods dealing with texts (strings including commands).
 */
public class Texts {

  private static final char COMMAND = '\\';
  private static final char START = '{';
  private static final char MARKER_START = '\001';
  private static final char END = '}';
  private static final char MARKER_END = '\002';
  private static final char OPTIONAL_START = '[';
  private static final char MARKER_OPTIONAL_START = '\003';
  private static final char OPTIONAL_END = ']';
  private static final char MARKER_OPTONAL_END = '\004';
  private static final char ESCAPE = '\\';
  private static final char MARKER_ESCAPE_START = '\005';
  private static final char MARKER_ESCAPE_END = '\006';
  private static final String SPECIAL = "<>=!~*#$%@?+|";
  // Expressions.
  private static final Pattern EXPRESSSION_PATTERN = Pattern.compile("\\[\\[(.*?)\\]\\]");
  private static final Pattern EXPRESSION_BRACKET =
      Pattern.compile("\\s*(\\w+)?\\s*\\(\\s*([^\\(\\)]*)\\s*\\)\\s*");
  private static final Pattern EXPRESSION_VARIABLE = Pattern.compile("\\s*\\$(\\w+)\\s*");
  private static final Pattern EXPRERSSION_NUMBER = Pattern.compile("\\s*([\\+\\-]?\\d+)\\s*");
  private static final Pattern EXPRESSION_MULTDIV = Pattern.compile("(.*)\\s*([\\*\\/])\\s*(.*?)");
  private static final Pattern EXPRESSION_ADDSUB = Pattern.compile("(.*)\\s*([\\+\\-])\\s*(.*?)");
  private static Map<String, TextCommand> COMMANDS = ImmutableMap.<String, TextCommand>builder()
      .put("Class", new ColorCommand(R.color.classNameDark))
      .put("Place", new ColorCommand(R.color.campaignDark))
      .put("Spell", new ClickableCommand(R.color.spellDark, (name, values) -> {
        SpellDialog.newInstance(name, values.get(SpellDialog.VALUE_CASTER_LEVEL, 1),
            values.get(SpellDialog.VALUE_SPELL_ABILITY_BONUS, 0),
            SpellClass.fromName(values.get(SpellDialog.VALUE_SPELL_CLASS, "Wizard")),
            Lists.newArrayList())
            .display();
      }))
      .put("Monster", new ColorCommand(R.color.monsterDark))
      .put("Quality", new ColorCommand(R.color.qualityDark))
      .put("Item", new ColorCommand(R.color.itemDark))
      .put("Feat", new ColorCommand(R.color.featDark))
      .put("Product", new ColorCommand(R.color.productDark))
      .put("Skill", new ColorCommand(R.color.skillDark))
      .put("par", new MessageCommand("\n\n"))
      .put("bold", new BoldCommand())
      .put("emph", new ItalicsCommand())
      .put("table", new TableCommand())
      .put("list", new ListCommand())
      .put("part", new CompoundCommand(new AlignmentCommand(Layout.Alignment.ALIGN_CENTER),
          new ColorCommand(R.color.grey), new SizeCommand(24), new MessageCommand("\n")))
      .build();

  protected static String clean(String text) {
    return text.replaceAll(MARKER_START + "<\\d+>", "" + START)
        .replaceAll(MARKER_END + "<\\d+>", "" + END)
        .replaceAll(MARKER_OPTIONAL_START + "<\\d+>", "" + OPTIONAL_START)
        .replaceAll(MARKER_OPTONAL_END + "<\\d+>", "" + OPTIONAL_END);
  }

  private static String evaluateExpression(String expression, Values values) {
    for (StringBuffer buffer = new StringBuffer();
         evaluateExpressionBrackets(buffer, expression, values);
         buffer = new StringBuffer()) {
      expression = buffer.toString();
    }

    return evaluateExpressionPart(expression, values).toString();
  }

  private static boolean evaluateExpressionBrackets(StringBuffer buffer, String expression,
                                                    Values values) {

    // Evaluate all brackets.
    Matcher matcher = EXPRESSION_BRACKET.matcher(expression);
    boolean found = false;
    while (matcher.find()) {
      if (matcher.group(1) == null) {
        matcher.appendReplacement(buffer, Matcher.quoteReplacement(
            evaluateExpressionPart(matcher.group(2), values).toString()));
      } else {
        matcher.appendReplacement(buffer,
            Matcher.quoteReplacement(evaluateFunction(matcher.group(1),
                Arrays.asList(matcher.group(2).split("\\s*,\\s*")).stream()
                    .map(a -> evaluateExpressionPart(a, values))
                    .collect(Collectors.toList())).toString()));
      }
      found = true;
    }
    matcher.appendTail(buffer);
    return found;
  }

  private static Value evaluateExpressionPart(String expression, Values values) {
    // The given expression will not have any brackets.

    // Number.
    Matcher matcher = EXPRERSSION_NUMBER.matcher(expression);
    if (matcher.matches()) {
      return new IntegerValue(Integer.parseInt(matcher.group(1)));
    }

    // Variable.
    matcher = EXPRESSION_VARIABLE.matcher(expression);
    if (matcher.matches()) {
      String key = matcher.group(1);
      if (values.containsKey(key)) {
        return values.get(key);
      } else {
        return new StringValue("<$" + key + ">");
      }
    }

    // Operations.
    matcher = EXPRESSION_ADDSUB.matcher(expression);
    if (matcher.matches()) {
      Value left = evaluateExpressionPart(matcher.group(1), values);
      String operator = matcher.group(2);
      Value right = evaluateExpressionPart(matcher.group(3), values);

      if (left instanceof IntegerValue && right instanceof IntegerValue) {
        if (operator.equals("+")) {
          return new IntegerValue(((IntegerValue) left).value + ((IntegerValue) right).value);
        } else if (operator.equals("-")) {
          return new IntegerValue(((IntegerValue) left).value - ((IntegerValue) right).value);
        }
      }

      return new StringValue("<" + left + operator + right + ">");
    }

    matcher = EXPRESSION_MULTDIV.matcher(expression);
    if (matcher.matches()) {
      Value left = evaluateExpressionPart(matcher.group(1), values);
      String operator = matcher.group(2);
      Value right = evaluateExpressionPart(matcher.group(3), values);

      if (left instanceof IntegerValue && right instanceof IntegerValue) {
        if (operator.equals("*")) {
          return new IntegerValue(((IntegerValue) left).value * ((IntegerValue) right).value);
        } else if (operator.equals("/")) {
          return new IntegerValue(((IntegerValue) left).value / ((IntegerValue) right).value);
        }
      }

      return new StringValue("<" + left + operator + right + ">");
    }

    return new StringValue(expression);
  }

  private static Value evaluateFunction(String name, List<Value> arguments) {
    switch (name) {
      case "identity":
        return new StringValue(Strings.COMMA_JOINER.join(arguments));

      case "nth":
        if (arguments.size() == 1 && arguments.get(0) instanceof IntegerValue) {
          switch (((IntegerValue) arguments.get(0)).value) {
            case 1:
              return new StringValue("1st");
            case 2:
              return new StringValue("2nd");
            case 3:
              return new StringValue("3rd");
            default:
              return new StringValue(((IntegerValue) arguments.get(0)).value + "th");
          }
        } else {
          break;
        }

      case "ranges":
        if (arguments.size() >= 1 && arguments.get(0) instanceof IntegerValue) {
          int value = ((IntegerValue) arguments.get(0)).value;
          for (int i = 1; i < arguments.size(); i++) {
            if (!(arguments.get(i) instanceof StringValue)) {
              return arguments.get(i);
            }

            String[] parts = ((StringValue) arguments.get(i)).value.split("\\s*:\\s*");
            if (parts.length == 2) {
              if (value <= Integer.parseInt(parts[0])) {
                return new StringValue(parts[1]);
              }
            } else {
              return arguments.get(i);
            }
          }

          return new StringValue("no match for ranges");
        } else {
          break;
        }
    }

    return new StringValue(name + "<" + Strings.COMMA_JOINER.join(arguments) + ">");
  }

  @VisibleForTesting
  protected static String markBrackets(String text, char escape, char start, char end,
                                       char markerStart, char markerEnd) {
    // Remove all escaped markers.
    text = text.replaceAll("\\" + escape + "\\" + start, "" + MARKER_ESCAPE_START);
    text = text.replaceAll("\\" + escape + "\\" + end, "" + MARKER_ESCAPE_END);

    // We mark all bracket markers for easier replacement.
    Pattern pattern = Pattern.compile("\\" + start + "([^\\" + start + "\\" + end + "]*?)\\" + end,
        Pattern.DOTALL);

    int i = 0;
    for (Matcher matcher = pattern.matcher(text); matcher.find(0); matcher =
        pattern.matcher(text)) {
      // Replace the nested brackets.
      text = matcher.replaceAll(markerStart + "<#" + i + "#>$1" + markerEnd + "<#" + i++ + "#>");
    }

    // 'Invert' the number to make sure that really the nesting level starts
    // at 0 on the outside not on the inside
    // with the above, {{}} is {<1>{<0>}<0>}<1> instead of {<0>{<1>}<1>}<0>
    pattern = Pattern.compile(markerStart + "<#(\\d+)#>(.*?)" + markerEnd + "<#\\1#>",
        Pattern.DOTALL);

    i = 0;
    for (Matcher matcher = pattern.matcher(text); matcher.find(0); matcher =
        pattern.matcher(text)) {
      // Replace the nested brackets.
      text = matcher.replaceAll(markerStart + "<" + i + ">$2" + markerEnd + "<" + i++ + ">");
    }

    // Replace all non 0 markers (we can't leave nested markers or parsing
    // of multiple arguments may fail
    text = text.replaceAll(markerStart + "<[1-9]\\d*>", "" + start);
    text = text.replaceAll(markerEnd + "<[1-9]\\d*>", "" + end);

    // Replace removed escaped markers.
    text = text.replaceAll("" + MARKER_ESCAPE_START, "\\" + escape + start);
    text = text.replaceAll("" + MARKER_ESCAPE_END, "\\" + escape + end);

    return text;
  }

  private static String markBrackets(String text) {
    return markBrackets(text, ESCAPE, START, END, MARKER_START, MARKER_END);
  }

  private static String markOptionalBrackets(String text) {
    return markBrackets(text, ESCAPE, OPTIONAL_START, OPTIONAL_END, MARKER_OPTIONAL_START,
        MARKER_OPTONAL_END);
  }

  public static SpannableStringBuilder processCommands(Context context, String text) {
    return processCommands(context, text, new Values());
  }

  @Deprecated
  public static SpannableStringBuilder processCommands(Context context, String text,
                                                       Map<String, Value> values) {
    return processCommands(context, text, new Values(values));
  }

  public static SpannableStringBuilder processCommands(Context context, String text,
                                                       Values values) {
    if (text.isEmpty()) {
      return new SpannableStringBuilder(text);
    }

    // Allow \n\n for paragraphs.
    text = text.replace("\n\n", "\\par ");

    // Process expressions first.
    text = processExpressions(text, values);

    // Do we have any commands at all?
    if (text.indexOf(COMMAND) < 0) {
      return new SpannableStringBuilder(text);
    }

    // Mark the brackets in the text
    SpannableStringBuilder builder = new SpannableStringBuilder();
    text = markOptionalBrackets(markBrackets(text));

    TextCommand.RenderingContext renderingContext =
        new TextCommand.RenderingContext(context, values);

    for (int start = 0; start < text.length(); ) {
      // Search the first command (don't accept escaped commands)
      int pos = -1;
      for (pos = text.indexOf(COMMAND, start); pos >= 0;
           pos = text.indexOf(COMMAND, pos + 1))
        if ((pos == 0 || text.charAt(pos - 1) != ESCAPE)
            && (ESCAPE != COMMAND || text.charAt(pos + 1) != COMMAND)
            && (Character.isLetterOrDigit(text.charAt(pos + 1))
            || SPECIAL.indexOf(text.charAt(pos + 1)) >= 0))
          break;

      if (pos < 0) {
        // No commands any more.
        builder.append(clean(text.substring(start)));
        break;
      }

      // Intermediate text.
      if (pos > 0) {
        builder.append(clean(text.substring(start, pos)));
      }

      // Ok, we really have a command now.
      int end = pos + 1;
      if (Character.isLetterOrDigit(text.charAt(end))) {
        for (; end < text.length(); end++) {
          if (!Character.isLetterOrDigit(text.charAt(end))) {
            break;
          }
        }
      } else {
        for (; end < text.length(); end++) {
          if (SPECIAL.indexOf(text.charAt(end)) < 0) {
            break;
          }
        }
      }

      String name = text.substring(pos + 1, end);

      // Extract the optional arguments.
      List<SpannableStringBuilder> optionals = new ArrayList<>();

      Pattern pattern = Pattern.compile("^\\s*" + MARKER_OPTIONAL_START
          + "<(\\d+)>((?:[^" + MARKER_OPTONAL_END
          + "]*?" + MARKER_OPTONAL_END
          + "<\\1>\\s*" + MARKER_OPTIONAL_START
          + "<\\1>)*[^" + MARKER_OPTONAL_END
          + "]*?)" + MARKER_OPTONAL_END + "<\\1>");

      Matcher matcher = pattern.matcher(text.substring(end));
      if (matcher.find()) {
        String[] args = matcher.group(2).split(MARKER_OPTONAL_END + "<"
            + matcher.group(1) + ">\\s*"
            + MARKER_OPTIONAL_START + "<"
            + matcher.group(1) + ">");

        end += matcher.end();

        // now we have all the arguments, we need to parse them as well
        for (int i = 0; i < args.length; i++)
          optionals.add(processCommands(context, args[i], values));
      }

      // Extract the arguments (we have to copy the above because we need to
      // update the position in the String as well as extracting the arguments;
      // to solve that, the method would not be able to be static).
      List<SpannableStringBuilder> arguments = new ArrayList<>();

      pattern = Pattern.compile("^\\s*" + MARKER_START + "<(\\d+)>((?:[^"
          + MARKER_END + "]*?" + MARKER_END
          + "<\\1>\\s*" + MARKER_START
          + "<\\1>)*[^" + MARKER_END + "]*?)"
          + MARKER_END + "<\\1>");

      matcher = pattern.matcher(text.substring(end));
      if (matcher.find()) {
        String[] args = matcher.group(2).split(MARKER_END + "<"
            + matcher.group(1) + ">\\s*"
            + MARKER_START + "<"
            + matcher.group(1) + ">");

        end += matcher.end();

        // now we have all the arguments, we need to parse them as well
        for (int i = 0; i < args.length; i++) {
          arguments.add(processCommands(context, args[i], values));
        }
      }

      // Now we try to render the command (we have to use an additional
      // template to avoid exceptions because a rendering is already in place)
      builder.append(render(renderingContext, name, optionals, arguments));

      // If no arguments were found, then skip the character directly following
      // the command (must be a white space)
      if (arguments.size() == 0 && optionals.size() == 0
          && text.length() > end
          && Character.isWhitespace(text.charAt(end)))
        end++;

      start = end;
    }

    return builder;
  }

  @VisibleForTesting
  protected static String processExpressions(String text, Values values) {
    Matcher matcher = EXPRESSSION_PATTERN.matcher(text);
    StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(buffer,
          Matcher.quoteReplacement(evaluateExpression(matcher.group(1), values)));
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

  public static String processWhitespace(String text) {
    return text.replaceAll("\n\n", "\\\\par ").replaceAll("\n", "").replaceAll(" +", " ");
  }

  private static SpannableStringBuilder rebuildCommand(String command,
                                                       List<SpannableStringBuilder> optionals,
                                                       List<SpannableStringBuilder> arguments) {
    SpannableStringBuilder builder = new SpannableStringBuilder(command);
    for (Spanned optional : optionals) {
      builder.append("[");
      builder.append(optional);
      builder.append("]");
    }
    for (Spanned argument : arguments) {
      builder.append("{");
      builder.append(argument);
      builder.append("}");
    }

    return builder;
  }

  private static Spanned render(TextCommand.RenderingContext context, String command,
                                List<SpannableStringBuilder> optionals,
                                List<SpannableStringBuilder> arguments) {
    if (COMMANDS.containsKey(command)) {
      return COMMANDS.get(command).render(context, optionals, arguments);
    }

    Status.error("Command '" + command + "' not supported, yet!");
    return rebuildCommand(command, optionals, arguments);
  }

  public static Spanned toSpanned(Context context, String text) {
    return toSpanned(context, text, ImmutableMap.of());
  }

  public static Spanned toSpanned(Context context, String text, Map<String, Value> values) {
    return processCommands(context, processWhitespace(text), values);
  }

  public static class Values {
    Map<String, Value> values = new HashMap<>();

    public Values() {
    }

    public Values(Map<String, Value> values) {
      this.values = new HashMap<>(values);
    }

    public Values add(List<String> texts) {
      Values values = new Values(this.values);
      for (String text : texts) {
        String[] parts = text.split("\\s*=\\s*");
        if (parts.length == 2) {
          try {
            values.put(parts[0], Integer.parseInt(parts[1]));
          } catch (NumberFormatException e) {
            values.put(parts[0], parts[1]);
          }
        }
      }

      return values;
    }

    public boolean containsKey(String key) {
      return values.containsKey(key);
    }

    public int get(String key, int defaultValue) {
      if (values.get(key) instanceof IntegerValue) {
        return ((IntegerValue) values.get(key)).value;
      }

      return defaultValue;
    }

    public @Nullable
    Value get(String key) {
      return values.get(key);
    }

    public String get(String key, String defaultValue) {
      if (values.get(key) instanceof StringValue) {
        return ((StringValue) values.get(key)).value;
      }

      return defaultValue;
    }

    public Values put(String key, Value value) {
      values.put(key, value);

      return this;
    }

    public Values put(String key, int value) {
      return put(key, new IntegerValue(value));
    }

    public Values put(String key, String value) {
      return put(key, new StringValue(value));
    }

    @Override
    public String toString() {
      return values.entrySet().stream()
          .map(e -> e.getKey() + ": " + e.getValue())
          .collect(Collectors.joining("\n"));
    }
  }

  public static class Value {
  }

  public static class IntegerValue extends Value {
    final int value;

    public IntegerValue(int value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  public static class StringValue extends Value {
    final String value;

    public StringValue(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }
}
