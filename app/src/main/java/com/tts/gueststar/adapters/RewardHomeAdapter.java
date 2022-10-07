package com.tts.gueststar.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import app.com.relevantsdk.sdk.models.RewardHomeModule;

import com.tts.gueststar.R;
import com.squareup.picasso.Picasso;
import com.tts.gueststar.utility.RoundedCornersTransformation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RewardHomeAdapter extends BaseAdapter {

    private ArrayList<RewardHomeModule> mData = new ArrayList<>(0);
    private Context mContext;
    private int points;

    public RewardHomeAdapter(Context context, int pointss) {
        mContext = context;
        points = pointss;
    }

    public void setData(ArrayList<RewardHomeModule> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int pos) {
        return mData.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_reward_home, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.rewardName = rowView.findViewById(R.id.reward_home_name);
            viewHolder.fineprint = rowView.findViewById(R.id.reward_home_fineprint);
            viewHolder.rewardPoints = rowView.findViewById(R.id.reward_home_points);
            viewHolder.image = rowView.findViewById(R.id.reward_home_image);
            viewHolder.image1 = rowView.findViewById(R.id.reward_home_image_new);
            viewHolder.main = rowView.findViewById(R.id.main);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        if (points < mData.get(position).getPoints()) {
            holder.main.setAlpha(0.5F);
            holder.main.setEnabled(false);
            holder.main.setClickable(false);
        }

        if (mData.get(position).getExpiryDate() != null) {

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy", Locale.US);
                Date date = new Date(Long.parseLong(mData.get(position).getExpiryDate()) * 1000);
                holder.rewardPoints.setText(
                        mContext.getString(R.string.in_app_only) + " " + mContext.getString(
                                R.string.expires_value,
                                sdf.format(date)));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        }

        if (mData.get(position).getImage_url().length() > 0) {
            //       holder.image.setVisibility(View.GONE);
            int radius = (int) mContext.getResources().getDimension(R.dimen.dimen_5);
            RoundedCornersTransformation transformation = new RoundedCornersTransformation(radius, 0);
            Picasso.get()
                    .load(mData.get(position).getImage_url())
                    .transform(transformation)
                    .into(holder.image);
        }
        holder.rewardName.setText(mData.get(position).getName());

        if (mData.get(position).getFineprint() != null) {
            if (mData.get(position).getFineprint().length() > 0) {
                holder.fineprint.setVisibility(View.VISIBLE);
                holder.fineprint.setText(mData.get(position).getFineprint());
            } else {
                holder.fineprint.setVisibility(View.GONE);
            }

        }
        return rowView;
    }


    private static class ViewHolder {
        private RelativeLayout main;
        private TextView rewardName, rewardPoints, fineprint;
        private ImageView image, image1;
    }
}
