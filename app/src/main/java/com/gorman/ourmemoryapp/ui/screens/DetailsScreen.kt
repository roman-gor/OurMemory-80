package com.gorman.ourmemoryapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.AudioItem
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.ui.fonts.mulishFont
import com.gorman.ourmemoryapp.ui.states.AudioAction
import com.gorman.ourmemoryapp.ui.states.DetailsUiEvent
import com.gorman.ourmemoryapp.ui.states.DetailsUiState
import com.gorman.ourmemoryapp.ui.viewModel.DetailsViewModel

@Composable
fun DetailsScreen(
    detailsViewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState = detailsViewModel.uiState.collectAsStateWithLifecycle()
    val onUiEvent = detailsViewModel::onUiEvent

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        when (val state = uiState.value) {
            is DetailsUiState.Error -> {
                Text("Error occurred")
            }
            is DetailsUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorResource(R.color.dark_red)
                )
            }
            is DetailsUiState.Success -> {
                val veteran = state.veteran

                val pagerState = rememberPagerState(pageCount = { 3 })
                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> DetailsContent(veteran, state.rewards)
                        1 -> BioContent(state.additionalText)
                        2 -> DocContent(
                            infoRes = state.additionalRes,
                            audio = state.audio,
                            directedUrls = state.directUrls,
                            onAudioAction = { onUiEvent(DetailsUiEvent.OnAudioAction(it)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsContent(
    veteran: Veteran,
    rewards: List<Int>
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedReward by remember { mutableIntStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        HeaderDetails(
            veteran = veteran,
            rewards = rewards,
            onClick = { reward, show ->
                showDialog = show
                selectedReward = reward
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3.7f)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
            ) {
                Text(
                    text = veteran.allInfo,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontFamily = mulishFont(),
                    style = TextStyle(fontWeight = FontWeight.Normal),
                    modifier = Modifier
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    textAlign = TextAlign.Justify
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2.3f)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                SwipeAnimation()
            }
        }
    }
    RewardsDisplay(showDialog = showDialog, selectedReward = selectedReward, onDismiss = { showDialog = false })
}

@Composable
private fun HeaderDetails(
    veteran: Veteran,
    rewards: List<Int>,
    onClick: (Int, Boolean) -> Unit
) {
    Row {
        Image(
            painter = rememberAsyncImagePainter(model = veteran.portrait),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .aspectRatio(0.8f)
                .weight(1.2f)
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F0F0))
        )
        Column(
            modifier = Modifier.weight(1.8f)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, start = 8.dp, top = 8.dp, bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.dark_red))
            ) {
                Column {
                    Text(
                        text = veteran.name,
                        color = colorResource(R.color.white),
                        fontSize = 16.sp,
                        fontFamily = mulishFont(),
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = veteran.years,
                        color = colorResource(R.color.white),
                        fontSize = 14.sp,
                        fontFamily = mulishFont(),
                        style = TextStyle(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            FlowRow(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rewards.forEach { rewardId ->
                    val rewardResId = rewardId.getRewardResId()
                    Image(
                        painter = painterResource(rewardResId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(
                                size = if (rewards.size > 4) {
                                    40.dp
                                } else if (rewards.size > 3) 45.dp else 60.dp
                            )
                            .padding(end = 4.dp)
                            .clickable(onClick = {
                                onClick(rewardId, true)
                            })
                    )
                }
            }
        }
    }
}

private fun Int.getRewardResId(): Int {
    return when (this) {
        1 -> R.drawable.red_znamya
        2 -> R.drawable.suvorov_1
        3 -> R.drawable.suvorov_1
        4 -> R.drawable.lenin
        5 -> R.drawable.geroj_sssr
        6 -> R.drawable.partizan
        7 -> R.drawable.za_pobedu_germany
        8 -> R.drawable.otech_war
        9 -> R.drawable.red_star
        10 -> R.drawable.orden_znak_pocheta
        11 -> R.drawable.za_otvagu
        12 -> R.drawable.narodny_artist
        else -> R.drawable.red_star
    }
}

@Composable
fun SwipeAnimation() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("swipe_left.json")
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier
            .size(250.dp)
            .padding(bottom = 16.dp)
    )
}

@Composable
fun RewardsDisplay(showDialog: Boolean, selectedReward: Int, onDismiss: () -> Unit) {
    if (showDialog) {
        val rewardRes = when (selectedReward) {
            1 -> R.drawable.red_znamya
            2 -> R.drawable.suvorov_1
            3 -> R.drawable.suvorov_1
            4 -> R.drawable.lenin
            5 -> R.drawable.geroj_sssr
            6 -> R.drawable.partizan
            7 -> R.drawable.za_pobedu_germany
            8 -> R.drawable.otech_war
            9 -> R.drawable.red_star
            10 -> R.drawable.orden_znak_pocheta
            11 -> R.drawable.za_otvagu
            12 -> R.drawable.narodny_artist
            else -> R.drawable.red_star
        }
        val rewardName = when (selectedReward) {
            1 -> R.string.red_znamya
            2 -> R.string.suvorov_1
            3 -> R.string.suvorov_1
            4 -> R.string.lenin
            5 -> R.string.geroj_sssr
            6 -> R.string.partizan
            7 -> R.string.za_pobedu_germany
            8 -> R.string.otech_war
            9 -> R.string.red_star
            10 -> R.string.orden_znak_pocheta
            11 -> R.string.za_otvagu
            12 -> R.string.narodny_artist
            else -> R.string.red_star
        }
        AlertDialog(
            onDismissRequest = { onDismiss() },
            confirmButton = {},
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painterResource(rewardRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(320.dp)
                            .padding(8.dp)
                    )
                    Text(
                        text = stringResource(rewardName),
                        modifier = Modifier.padding(top = 8.dp),
                        style = TextStyle(
                            color = colorResource(R.color.red),
                            fontFamily = mulishFont(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = colorResource(R.color.dark_white)
        )
    }
}

@Composable
fun BioContent(infoText: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.biography),
            style = TextStyle(
                fontFamily = mulishFont(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colorResource(R.color.dark_red)
            )
        )
        if (infoText.isNotEmpty()) {
            TextItem(infoText.first())
        }
    }
}

@Composable
fun TextItem(infoText: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 26.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Text(
            text = infoText,
            style = TextStyle(
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = mulishFont()
            ),
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .verticalScroll(rememberScrollState()),
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun DocContent(
    infoRes: Map<String, String>,
    directedUrls: Map<String, String>,
    audio: AudioItem?,
    onAudioAction: (AudioAction) -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    val pagerResState = rememberPagerState(pageCount = { directedUrls.size })

    Log.e("YANDEX_DISK", "$directedUrls")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.docs),
            style = TextStyle(
                fontFamily = mulishFont(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colorResource(R.color.dark_red)
            )
        )
        if (infoRes.isNotEmpty()) {
            HorizontalPager(
                state = pagerResState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
                verticalAlignment = Alignment.CenterVertically
            ) { page ->
                val entry = directedUrls.entries.elementAt(page)
                val url = entry.key
                val describe = entry.value
                ResImageItem(url = url, describe = describe)
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = colorResource(R.color.dark_white)
        )
        Spacer(Modifier.height(12.dp))

        AudioTrack(
            audio = audio,
            isPlaying = isPlaying,
            onAudioAction = { action ->
                isPlaying = action == AudioAction.Play
                onAudioAction(action)
            }
        )
    }
}

@Composable
fun AudioTrack(
    audio: AudioItem?,
    isPlaying: Boolean,
    onAudioAction: (AudioAction) -> Unit
) {
    val audioTitle = audio?.title.orEmpty()

    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 32.dp, end = 32.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.dark_white))
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Button(
                    onClick = {
                        if (isPlaying) {
                            onAudioAction(AudioAction.Pause)
                        } else {
                            onAudioAction(AudioAction.Play)
                        }
                    },
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.dark_red),
                        contentColor = colorResource(R.color.white)
                    )
                ) {
                    if (!isPlaying) {
                        Icon(
                            painter = painterResource(R.drawable.play_arrow),
                            contentDescription = "Play",
                            modifier = Modifier.size(24.dp),
                            tint = colorResource(R.color.white)
                        )
                    } else {
                        Icon(
                            painterResource(R.drawable.pause),
                            contentDescription = "Pause",
                            modifier = Modifier.size(24.dp),
                            tint = colorResource(R.color.white)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    audioTitle,
                    style = TextStyle(
                        fontFamily = mulishFont(),
                        color = colorResource(R.color.black)
                    )
                )
            }
        }
    }
}

@Composable
fun ResImageItem(url: String, describe: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(
            model = url,
            placeholder = painterResource(R.drawable.image_info_placeholder)
            ),
            contentDescription = describe,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        if (describe.isNotEmpty()) {
            Text(
                text = describe,
                style = TextStyle(
                    fontFamily = mulishFont(),
                    fontSize = 16.sp,
                    color = colorResource(R.color.dark_red),
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
