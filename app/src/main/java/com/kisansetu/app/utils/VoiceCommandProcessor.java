package com.kisansetu.app.utils;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class VoiceCommandProcessor {

    private static final Map<String, String> SYNONYMS = new HashMap<>();
    private static String lastSearchQuery = "";

    static {
        // Map Hindi/Hinglish to English standard terms
        SYNONYMS.put("seb", "apple");
        SYNONYMS.put("apple", "apple");
        SYNONYMS.put("tamatar", "tomato");
        SYNONYMS.put("tomato", "tomato");
        SYNONYMS.put("aloo", "potato");
        SYNONYMS.put("potato", "potato");
        SYNONYMS.put("pyaz", "onion");
        SYNONYMS.put("onion", "onion");
        SYNONYMS.put("sabzi", "vegetables");
        SYNONYMS.put("vegetables", "vegetables");
        SYNONYMS.put("fal", "fruits");
        SYNONYMS.put("fruits", "fruits");
        SYNONYMS.put("machli", "fish");
        SYNONYMS.put("fish", "fish");
        SYNONYMS.put("anda", "egg");
        SYNONYMS.put("egg", "egg");
        SYNONYMS.put("murgi", "chicken");
        SYNONYMS.put("chicken", "chicken");
        SYNONYMS.put("bakra", "mutton");
        SYNONYMS.put("mutton", "mutton");
        SYNONYMS.put("gehu", "wheat");
        SYNONYMS.put("wheat", "wheat");
        SYNONYMS.put("chawal", "rice");
        SYNONYMS.put("rice", "rice");
        SYNONYMS.put("daal", "pulses");
        SYNONYMS.put("pulses", "pulses");
    }

    public static String process(String command) {
        command = command.toLowerCase().trim();
        JSONObject response = new JSONObject();

        try {
            // Check for navigation
            if (command.contains("profile")) {
                response.put("action", "open_profile");
            } else if (command.contains("cart") || command.contains("tokri")) {
                response.put("action", "open_cart");
            } else if (command.contains("home") || command.contains("dashboard")) {
                response.put("action", "open_home");
            } else if (command.contains("tools") || command.contains("smart") || command.contains("ai")) {
                response.put("action", "open_smart_tools");
            }
            // Check for Farmer specific actions
            else if (command.contains("add product") || command.contains("naya product") || command.contains("product add")) {
                response.put("action", "add_product");
            }
            // Check for price query
            else if (command.contains("price") || command.contains("daam") || command.contains("rate") || command.contains("kimat")) {
                String item = extractItem(command);
                response.put("action", "check_price");
                response.put("product", item);
            }
            // Check for disease detection
            else if (command.contains("disease") || command.contains("bimari") || command.contains("check crop")) {
                response.put("action", "detect_disease");
            }
            // Check for cart actions
            else if (command.contains("add to cart") || command.contains("cart me daal do") || command.contains("isko add karo")) {
                String item = extractItem(command);
                if (item.equals("isko") && !lastSearchQuery.isEmpty()) {
                    item = lastSearchQuery;
                }
                response.put("action", "add_to_cart");
                response.put("product", item);
            }
            // Check for searching
            else if (command.contains("search") || command.contains("dikhado") || command.contains("dhundo") || command.contains("find")) {
                String query = extractItem(command);
                lastSearchQuery = query;
                response.put("action", "search_product");
                response.put("query", query);
            }
            // Default: Try to treat as search if product detected
            else {
                String item = extractItem(command);
                if (!item.isEmpty()) {
                    lastSearchQuery = item;
                    response.put("action", "search_product");
                    response.put("query", item);
                } else {
                    response.put("action", "clarify");
                    response.put("message", "Maaf kijiye, mujhe samajh nahi aaya. Kripya phir se kahein.");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response.toString();
    }

    private static String extractItem(String command) {
        // Simple extraction logic: check for known synonyms in the string
        for (String key : SYNONYMS.keySet()) {
            if (command.contains(key)) {
                return SYNONYMS.get(key);
            }
        }
        
        // If no synonym found, try to extract the main word (naïve approach)
        String[] words = command.split("\\s+");
        if (words.length > 0) {
            // Filter out common action words
            for (String word : words) {
                if (!word.equals("search") && !word.equals("karo") && !word.equals("add") && 
                    !word.equals("dikha") && !word.equals("kholo") && !word.equals("me") && 
                    !word.equals("daal") && !word.equals("do")) {
                    return word;
                }
            }
        }
        return "";
    }
}
