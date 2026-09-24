package com.gorman.ourmemoryapp.ui.info.models

import com.gorman.ourmemoryapp.R
import kotlinx.collections.immutable.persistentListOf

object InfoContent {
    val news = persistentListOf(
        NewsUi(
            iconRes = R.drawable.tochka_news,
            title = "Tochka.by",
            url = "https://tochka.by/articles/life/muzey_pod_otkrytym_nebom_chem_vas_mozhet_udivit_voennoe_kladbishche/"
        ),
        NewsUi(
            iconRes = R.drawable.sb_by_news,
            title = "SB.by",
            url = "https://news.sb.by/articles/na-karte-minska-poyavilas-eshche-odna-tsifrovaya-zvezda-" +
                "pamyatnuyu-tablichku-ustanovili-na-voennom-k.html"
        ),
        NewsUi(
            iconRes = R.drawable.minsk_news,
            title = "Minsknews.by",
            url = "https://minsknews.by/lyudi-zhivy-do-teh-por-poka-o-nih-pomnyat-" +
                "istoriya-voennogo-kladbishha-v-minske/"
        )
    )
}
