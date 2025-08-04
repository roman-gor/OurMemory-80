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