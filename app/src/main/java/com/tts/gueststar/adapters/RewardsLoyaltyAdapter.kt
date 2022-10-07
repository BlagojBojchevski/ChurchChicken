package com.tts.royrogers.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.tts.gueststar.R
import com.tts.olosdk.models.Reward
import com.tts.royrogers.interfaces.SelectedLoyaltyRewardInterface
import kotlinx.android.synthetic.main.inflater_rewards_loyalty.view.*
import java.text.SimpleDateFormat

class RewardsLoyaltyAdapter(
    private val context: Context,
    private var list: List<Reward>,
    private var listener: SelectedLoyaltyRewardInterface
) : RecyclerView.Adapter<RewardsLoyaltyAdapter.RewardsViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): RewardsViewHolder {
        return RewardsViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.inflater_rewards_loyalty,
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
        holder.init(reward, context)
        holder.layoutText.setOnClickListener {
            listener.onRewardSelected(list[position])
        }
    }


    class RewardsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvRewardName = itemView.tvRewardName
        private val tvRewardExpiration = itemView.tvRewardExpiration

        private val fineprint = itemView.tvRewardPoints

        val layoutText: LinearLayout = itemView.layoutRewardText

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun init(reward: Reward, context: Context) {
//            when {
//                reward.imageurl.isNotEmpty() -> {
//                    val radius = context.resources.getDimension(R.dimen.dimen_2).toInt()
//                    val transformation = RoundedCornersTransformation(radius, 0)
//                    Picasso.get().load(reward.imageurl).transform(transformation).into(imgReward)
//                }
//            }
            tvRewardName.text = reward.label
            if (!reward.fineprint.isNullOrEmpty()) {
                fineprint.visibility = View.VISIBLE
                fineprint.text = reward.fineprint
            } else {
                fineprint.visibility = View.GONE
            }

            if (!reward.expirationdate.isNullOrEmpty()) {
                val parser = SimpleDateFormat("yyyyMMdd")
                val formatter = SimpleDateFormat("M/dd/yy")
                val output: String = formatter.format(parser.parse(reward.expirationdate))

                tvRewardExpiration.text = context.getString(R.string.in_app_only) + " " + output
            }

        }
    }

}