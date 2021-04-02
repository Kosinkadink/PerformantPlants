package me.kosinkadink.performantplants.util;

import me.clip.placeholderapi.PlaceholderAPI;
import me.kosinkadink.performantplants.PerformantPlants;
import me.kosinkadink.performantplants.scripting.ExecutionContext;

public class PlaceholderHelper {

    static class ReplaceResult {
        private final int index;
        private final String replacement;
        private final boolean replaced;

        public ReplaceResult(int index, String replacement, boolean replaced) {
            this.index = index;
            this.replacement = replacement;
            this.replaced = replaced;
        }

        public int getIndex() {
            return index;
        }

        public String getReplacement() {
            return replacement;
        }

        public boolean isNotReplaced() {
            return !replaced;
        }
    }

    public static String setVariablesAndPlaceholders(ExecutionContext context, String stringInput) {
        stringInput = replaceVariables(context, stringInput);
        return replacePlaceholders(context, stringInput);
    }

    public static String replaceVariables(ExecutionContext context, String fullString) {
        int index = 0;
        StringBuilder finalString = new StringBuilder();
        while (index < fullString.length()) {
            char character = fullString.charAt(index);
            if (character == '[') {
                ReplaceResult result = replaceVariableInner(context, fullString, index+1);
                // if index -1, then no matching parentheses to close; should just add remaining characters and return resulting string
                if (result.getIndex() < 0) {
                    finalString.append(fullString.substring(index));
                    break;
                }
                if (result.isNotReplaced()) {
                    finalString.append(character);
                }
                finalString.append(result.getReplacement());
                index = result.getIndex();
            } else {
                finalString.append(character);
            }
            // increment index
            index++;
        }
        return finalString.toString();
    }

    public static ReplaceResult replaceVariableInner(ExecutionContext context, String fullString, int startIndex) {
        int index = startIndex;
        StringBuilder finalString = new StringBuilder();
        while (index < fullString.length()) {
            char character = fullString.charAt(index);
            if (character == '[') {
                ReplaceResult result = replaceVariableInner(context, fullString, index+1);
                // if index -1, then no matching parentheses to close; should just add remaining characters and return resulting string
                if (result.getIndex() < 0) {
                    finalString.append(fullString.substring(index));
                    index = result.getIndex();
                    break;
                }
                if (result.isNotReplaced()) {
                    finalString.append(character);
                }
                finalString.append(result.getReplacement());
                index = result.getIndex();
            } else if (character == ']') {
              String currentString = finalString.toString();
              currentString = replacePlaceholders(context, currentString);
              currentString = ScriptHelper.getVariableValue(context, currentString);
              // if variable value for back, return with index
              if (currentString != null) {
                  return new ReplaceResult(index, currentString, true);
              }
              // otherwise, add character and break
              else {
                  finalString.append(character);
                  break;
              }
            } else {
                finalString.append(character);
            }
            // increment index
            index++;
        }
        return new ReplaceResult(index, finalString.toString(), false);
    }

    public static String replacePlaceholders(ExecutionContext context, String stringInput) {
        if (context.isPlayerSet()) {
            if (PerformantPlants.getInstance().hasPlaceholderAPI()) {
                stringInput = PlaceholderAPI.setBracketPlaceholders(context.getPlayer(), stringInput);
                stringInput = PlaceholderAPI.setPlaceholders(context.getPlayer(), stringInput);
            }
        }
        return stringInput;
    }

}
