package com.bradz.dotdashdot.randomreddit.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bradz.dotdashdot.randomreddit.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Mauve3 on 9/3/16.
 */
public class SubredditAdapter extends BaseAdapter {

    private static String ERROR = "Parsing Error";

    List<JSONObject> subs;
    LayoutInflater inflater;
    Context self;

    public SubredditAdapter(Context self, List<JSONObject> subs){
        this.self = self;
        inflater = LayoutInflater.from(self);
        this.subs = subs;
    }

    @Override
    public int getCount() {
        return subs.size();
    }

    @Override
    public Object getItem(int i) {
        return subs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        View convertView = view;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_subreddits, null, true);
        }

        TextView subby = (TextView) convertView.findViewById(R.id.text_subreddit);
        View remove = convertView.findViewById(R.id.remove_subreddit);

        JSONObject jsonObject = subs.get(position);
        try {
            Boolean over_18 = jsonObject.getBoolean("over18");
            String display_name = jsonObject.getString("display_name");

            if (over_18) {
                subby.setTextColor(Color.RED);
            } else {
                subby.setTextColor(Color.BLACK);
            }

            subby.setText(display_name);

        } catch (JSONException e) {
            e.printStackTrace();
            subby.setTextColor(Color.RED);
            subby.setText(ERROR);
        }

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(self);
                alertDialog2.setTitle("Confirm Remove");
                alertDialog2.setMessage("Are you sure you want remove this favorite?");
                alertDialog2.setIcon(R.drawable.ic_delete);
                alertDialog2.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(self.getApplicationContext(), "This will remove the favorite", Toast.LENGTH_SHORT).show();
                            }
                        });
                alertDialog2.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog2.show();
            }
        });

        return convertView;
    }
}
