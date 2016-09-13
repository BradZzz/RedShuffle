package com.bradz.dotdashdot.randomreddit.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

/**
 * Created by drewmahrt on 3/6/16.
 */
public class ProfileSub {
    /*private String display_name;
    private boolean over18;

    public ProfileSub(String display_name, boolean over18){
        this.display_name = display_name;
        this.over18 = over18;
    }*/

    public static class SortBasedOnMessageId implements Comparator<JSONObject> {
        /*
        * (non-Javadoc)
        *
        * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
        * lhs- 1st message in the form of json object. rhs- 2nd message in the form
        * of json object.
        */
        @Override
        public int compare(JSONObject lhs, JSONObject rhs) {
            try {
                return lhs.getString("display_name").compareToIgnoreCase(rhs.getString("display_name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;

        }
    }
}
