package com.bradz.dotdashdot.randomreddit.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.models.Thread;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.koushikdutta.ion.Ion;

import java.util.List;

/**
 * Created by Mauve3 on 10/3/16.
 */

public class SwipeAdapter extends BaseSwipeAdapter {

  private Context mContext;
  private List<Thread> threads;

  public SwipeAdapter(Context mContext, List<Thread> threads) {
    this.mContext = mContext;
    this.threads = threads;
  }

  @Override
  public int getSwipeLayoutResourceId(int position) {
    return R.id.swipe;
  }

  @Override
  public View generateView(int position, ViewGroup parent) {
    return LayoutInflater.from(mContext).inflate(R.layout.list_swipe_main, null);
  }

  public void swapList(List<Thread> threads){
    this.threads = threads;
  }

  @Override
  public void fillValues(int position, View view) {
    TextView text1 = (TextView) view.findViewById(R.id.text1);
    TextView text2 = (TextView) view.findViewById(R.id.text2);
    ImageView sub_image = (ImageView) view.findViewById(R.id.sub_image);
    sub_image.setClipToOutline(true);
    view.setBackgroundResource(android.R.color.background_light);
    String title = threads.get(position).getTitle();
    final String url = threads.get(position).getUrl();
    String image = threads.get(position).getImage();
    final String comment = threads.get(position).getComments();
    int votes = threads.get(position).getVotes();
    boolean nsfw = threads.get(position).isNsfw();
    LinearLayout outline = (LinearLayout) view.findViewById(R.id.image_outline);
    if (nsfw) {
      outline.setBackground(mContext.getResources().getDrawable(R.drawable.red_border));
    } else {
      outline.setBackground(mContext.getResources().getDrawable(R.drawable.black_border));
    }

    view.findViewById(R.id.url_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        String linkUrl = url.replace("amp;", "");
        if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) {
          linkUrl = "http://" + linkUrl;
        }
        i.setData(Uri.parse(linkUrl));
        mContext.startActivity(i);
      }
    });

    view.findViewById(R.id.comments_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        String linkUrl = "http://reddit.com" + comment;
        i.setData(Uri.parse(linkUrl));
        mContext.startActivity(i);
      }
    });

    text1.setText(title);
    String voteString = "Votes: " + votes;
    text2.setText(voteString);
    Ion.with(sub_image)
      .placeholder(R.drawable.reddit_logo)
      .error(R.drawable.reddit_logo)
      .load(image);
  }

  @Override
  public int getCount() {
    return threads.size();
  }

  @Override
  public Object getItem(int position) {return threads.get(position);}

  @Override
  public long getItemId(int position) {
    return position;
  }
}