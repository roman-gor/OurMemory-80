package com.gorman.ourmemoryapp.data

import com.gorman.ourmemoryapp.R

data class Image(
    val id: Int,
    val res: Int
)

val imagesList = listOf(
    Image(id = 0, res = R.drawable.img1),
    Image(id = 1, res = R.drawable.img2),
    Image(id = 2, res = R.drawable.img3),
    Image(id = 3, res = R.drawable.img4),
    Image(id = 4, res = R.drawable.img5),
    Image(id = 5, res = R.drawable.img6),
    Image(id = 6, res = R.drawable.img7),
    Image(id = 7, res = R.drawable.img8),
    Image(id = 8, res = R.drawable.img9),
    Image(id = 9, res = R.drawable.img10)
    )

data class News(
    val id: Int,
    val icon: Int,
    val text: String,
    val annotation: String
)

val newsList = listOf(
    News(id = 0, icon = R.drawable.tochka_news, text = "Tochka.by", annotation = "https://tochka.by/articles/life/muzey_pod_otkrytym_nebom_chem_vas_mozhet_udivit_voennoe_kladbishche/"),
    News(id = 1, icon = R.drawable.sb_by_news, text = "SB.by", annotation = "https://news.sb.by/articles/na-karte-minska-poyavilas-eshche-odna-tsifrovaya-zvezda-pamyatnuyu-tablichku-ustanovili-na-voennom-k.html"),
    News(id = 2, icon = R.drawable.minsk_news, text = "Minsknews.by", annotation = "https://minsknews.by/lyudi-zhivy-do-teh-por-poka-o-nih-pomnyat-istoriya-voennogo-kladbishha-v-minske/"),
)