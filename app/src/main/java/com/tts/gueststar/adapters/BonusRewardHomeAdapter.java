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
import app.com.relevantsdk.sdk.models.RewardX;
import com.tts.gueststar.R;
import com.tts.gueststar.utility.RoundedCornersTransformation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BonusRewardHomeAdapter extends BaseAdapter {

    private ArrayList<RewardX> mData = new ArrayList<>(0);
    private Context mContext;
    private int points;


    public  BonusRewardHomeAdapter(Context context, int poin) {
        mContext = context;
        points = poin;
    }

    public void setData(ArrayList<RewardX> data) {
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
            viewHolder.rewardPoints = rowView.findViewById(R.id.reward_home_points);
            viewHolder.image = rowView.findViewById(R.id.reward_home_image);
            viewHolder.main = rowView.findViewById(R.id.main);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        if (points < mData.get(position).getPoint()) {
            holder.main.setAlpha(0.5F);
            holder.main.setEnabled(false);
            holder.main.setClickable(false);
        }
        if (mData.get(position).getPoint() == 0) {
            holder.main.setAlpha(1.0F);
            holder.main.setEnabled(true);
            holder.main.setClickable(true);
            holder.rewardPoints.setText(mContext.getString(R.string.rewards_free));
        } else {
            holder.rewardPoints.setText(mData.get(position).getPoint() + " Points");
        }


        if (mData.get(position).getImage_url().length() > 0){
            int radius = (int)mContext.getResources().getDimension(R.dimen.dimen_5);
            RoundedCornersTransformation transformation = new RoundedCornersTransformation(radius, 0);
            Picasso.get()
                    .load(mData.get(position).getImage_url())
                    .transform(transformation)
                    .into(holder.image);}
        holder.rewardName.setText(mData.get(position).getNcr_result().getBp_reward());
  //      holder.rewardPoints.setText(mData.get(position).getPoint() + " Points");
        return rowView;
    }


    private class ViewHolder {
        private TextView rewardName, rewardPoints;
        private ImageView image;
        private RelativeLayout main;
    }
}
