package com.tts.gueststar.adapters

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.com.relevantsdk.sdk.models.Reward
import com.tts.gueststar.R
import com.tts.gueststar.utility.RoundedCornersTransformation
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.inflater_rewards.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RewardsAdapter(
    private val context: Context,
    private var list: List<Reward>,
    private var userPoints: Int
) : RecyclerView.Adapter<RewardsAdapter.RewardsViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): RewardsViewHolder {
        return RewardsViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.inflater_rewards,
                viewGroup,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RewardsViewHolder, position: Int) {
        val reward = list[position]
        holder.init(reward, context, userPoints)

    }


    class RewardsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgReward = itemView.imgReward
        private val tvRewardName = itemView.tvRewardName
        private val tvRewardPoint = itemView.tvRewardPoints
        private val tvRewardExpiration = itemView.tvRewardExpiration
        val layoutText = itemView.layoutRewardText

        @SuppressLint("SetTextI18n")
        fun init(reward: Reward, context: Context, userPoints: Int) {
            if (reward.image_url.isNotEmpty()) {
                val radius = context.resources.getDimension(R.dimen.dimen_5).toInt()
                val transformation = RoundedCornersTransformation(radius, 0)
                Picasso.get().load(reward.image_url).transform(transformation).into(imgReward)
            }
            tvRewardName.text = reward.name
            if (reward.points > 0) {

                if (!reward.expiryDate.isNullOrEmpty()) {
                    try {
                        val sdf = SimpleDateFormat("M/dd/yy", Locale.US)
                        val date = Date(reward.expiryDate.toLong() * 1000)
                        tvRewardExpiration.text =
                            context.getString(R.string.in_app_only) + " " + context.getString(
                                R.string.expires_value,
                                sdf.format(date)
                            )
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                } else {
                    tvRewardExpiration.text =
                        context.getString(R.string.in_app_only)
                }
            } else {
                if (!reward.expiryDate.isNullOrEmpty()) {
                    try {
                        val sdf = SimpleDateFormat("M/dd/yy", Locale.US)
                        val date = Date(reward.expiryDate.toLong() * 1000)

                        tvRewardExpiration.text = context.getString(R.string.in_app_only) + " " +
                                context.getString(R.string.expires_value, sdf.format(date))
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                } else {
                    tvRewardExpiration.text =
                        context.getString(R.string.in_app_only)
                }

                //   tvRewardPoint.text = context.getString(R.string.rewards_free)
//                tvRewardExpiration.text = ""
            }

            if (reward.fineprint.isNotEmpty())
                tvRewardPoint.text = reward.fineprint
            else
                tvRewardPoint.visibility = View.GONE

            if (reward.points > userPoints) {
                imgReward.alpha = 0.5F
                layoutText.alpha = 0.5F
            }

        }
    }

}