package com.nami.peace.domain.model

import com.nami.peace.R

enum class ReminderCategory(val iconResId: Int) {
    WORK(R.drawable.ic_cat_work),
    HEALTH(R.drawable.ic_cat_health),
    STUDY(R.drawable.ic_cat_study),
    HOME(R.drawable.ic_cat_home),
    GENERAL(R.drawable.ic_cat_general)
}
