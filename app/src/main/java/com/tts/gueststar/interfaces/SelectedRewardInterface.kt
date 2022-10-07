package com.tts.gueststar.interfaces

import ncruser.models.Reward

interface SelectedRewardInterface {
    fun onRewardSelected(reward: Reward, userPoints: Int, isFromHomeFragment: Boolean)
}