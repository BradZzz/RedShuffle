package com.bradz.dotdashdot.randomreddit.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;
import com.daimajia.swipe.adapters.CursorSwipeAdapter;
import com.koushikdutta.ion.Ion;

/**
 * Created by Mauve3 on 10/3/16.
 */

public class RedditCursorSwipeAdapter extends CursorSwipeAdapter {

  public RedditCursorSwipeAdapter(Context context, Cursor c, int flags) {
    super(context, c, flags);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    return LayoutInflater.from(context).inflate(R.layout.article_row_layout, parent, false);
  }

  @Override
  public void bindView(View view, final Context context, Cursor cursor) {
    TextView text1 = (TextView) view.findViewById(R.id.text1);
    TextView text2 = (TextView) view.findViewById(R.id.text2);
    ImageView sub_image = (ImageView) view.findViewById(R.id.sub_image);
    sub_image.setClipToOutline(true);

    view.setBackgroundResource(android.R.color.background_light);
    String title = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_TITLE));
    final String url = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_URL));
    String image = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_IMAGE));
    String sub = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_SUB));
    int votes = cursor.getInt(cursor.getColumnIndex(StockDBHelper.COLUMN_VOTES));
    int nsfw = cursor.getInt(cursor.getColumnIndex(StockDBHelper.COLUMN_NSFW));
    LinearLayout outline = (LinearLayout) view.findViewById(R.id.image_outline);
    if (nsfw > 0) {
      outline.setBackground(context.getResources().getDrawable(R.drawable.red_border));
    } else {
      outline.setBackground(context.getResources().getDrawable(R.drawable.black_border));
    }

    /*if (first_image == null && image != null && !image.isEmpty() && image.contains("http")) {
      first_image = image;
    }
    String subby = "/" + sub;
    if (tView != null && !tView.getText().equals(subby)) {
      tView.setText(subby);
      writeEventAnalytics("Sub", "New", sub);
    }*/

    text1.setText(title);
    String voteString = "Votes: " + votes;
    text2.setText(voteString);
    Ion.with(sub_image)
      .placeholder(R.drawable.reddit_logo)
      .error(R.drawable.reddit_logo)
      .load(image);

    //initSwipe(view);

    /*view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        String linkUrl = url.replace("amp;", "");
        if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) {
          linkUrl = "http://" + linkUrl;
        }
        i.setData(Uri.parse(linkUrl));
        context.startActivity(i);
      }
    });*/
  }

  @Override
  public int getSwipeLayoutResourceId(int position) {
    return R.id.swipe;
  }

  @Override
  public void closeAllItems() {

  }
}
