package com.bradz.dotdashdot.randomreddit.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;
import com.bradz.dotdashdot.randomreddit.utils.Statics;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.koushikdutta.ion.Ion;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Mauve3 on 10/3/16.
 */

public class SwipeFavoriteAdapter extends BaseSwipeAdapter {

  private Context self;
  private Cursor cursor;
  private Handler handler;

  public SwipeFavoriteAdapter(Context mContext, Cursor cursor, Handler handler) {
    this.self = mContext;
    this.cursor = cursor;
    this.handler = handler;
  }

  @Override
  public int getSwipeLayoutResourceId(int position) {
    return R.id.swipe;
  }

  @Override
  public View generateView(int position, ViewGroup parent) {
    return LayoutInflater.from(self).inflate(R.layout.list_swipe_favorite, null);
  }

  public void swapList(Cursor cursor){
    this.cursor.close();
    this.cursor = cursor;
  }

  @Override
  public void fillValues(int position, View view) {
    cursor.moveToPosition(position);

    final TextView sub = (TextView) view.findViewById(R.id.sub);
    TextView titleText = (TextView) view.findViewById(R.id.title);
    //TextView nsfwText = (TextView) view.findViewById(R.id.nsfw);
    TextView usersText = (TextView) view.findViewById(R.id.users);

    ImageView sub_image = (ImageView) view.findViewById(R.id.sub_image);
    sub_image.setClipToOutline(true);

    view.setBackgroundResource(android.R.color.background_light);

    final String subreddit = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_SUB));
    String title = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_DESCRIPTION));
    int nsfw = cursor.getInt(cursor.getColumnIndex(StockDBHelper.COLUMN_NSFW));
    String image = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_IMAGE));
    String created = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_CREATED));

    if (nsfw > 0) {
      LinearLayout outline = (LinearLayout) view.findViewById(R.id.image_outline);
      outline.setBackground(self.getResources().getDrawable(R.drawable.red_border));
    }

    sub.setText(subreddit);
    SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yy HH:mm:ss");//dd/MM/yyyy
    Date now = new Date(Long.valueOf(created));
    String strDate = sdfDate.format(now);
    //Calendar calendar = Calendar.getInstance();
    //calendar.setTimeInMillis(Long.valueOf(created));
    //String createdString = calendar.getTime().toString();
    usersText.setText(strDate);
    titleText.setText(title);
    Ion.with(sub_image)
      .placeholder(R.drawable.reddit_logo)
      .error(R.drawable.reddit_logo)
      .load(image);

    View remove = view.findViewById(R.id.remove_fav);
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
              Message msg = Message.obtain();
              Bundle b = new Bundle();
              b.putInt("type", Statics.REDDIT_SUBREDDIT_FAVORITE_UNSUBSCRIBE);
              b.putLong("request_time", System.currentTimeMillis());
              b.putString("sub",subreddit);
              b.putBoolean("error", false);
              msg.setData(b);
              handler.sendMessage(msg);
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

    View subscribe = view.findViewById(R.id.subscribe_sub);
    subscribe.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Message msg = Message.obtain();
        Bundle b = new Bundle();
        b.putInt("type", Statics.REDDIT_SUBREDDIT_PRESUBSCRIBE);
        b.putLong("request_time", System.currentTimeMillis());
        b.putString("sub",subreddit);
        b.putBoolean("error", false);
        msg.setData(b);
        handler.sendMessage(msg);
      }
    });

    View url = view.findViewById(R.id.url_button);
    url.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Message msg = Message.obtain();
        Bundle b = new Bundle();
        b.putInt("type", Statics.REDDIT_SUBREDDIT_NAV);
        b.putLong("request_time", System.currentTimeMillis());
        b.putString("sub",subreddit);
        b.putBoolean("error", false);
        msg.setData(b);
        handler.sendMessage(msg);
      }
    });
  }

  @Override
  public int getCount() {
    return cursor.getCount();
  }

  @Override
  public Object getItem(int position) {
    cursor.moveToPosition(position);
    return cursor;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public void closeAllItems() {
    cursor.close();
  }
}
