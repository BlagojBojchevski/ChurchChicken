package com.tts.royrogers.interfaces

import com.tts.olosdk.models.Reward

interface SelectedLoyaltyRewardInterface {
    fun onRewardSelected(reward: Reward)
}