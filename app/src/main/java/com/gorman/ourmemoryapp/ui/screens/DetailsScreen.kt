package com.gorman.ourmemoryapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.flowlayout.FlowRow
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.viewModel.DetailsViewModel
import com.gorman.ourmemoryapp.ui.fonts.mulishFont
import com.gorman.ourmemoryapp.data.Veteran
import com.gorman.ourmemoryapp.data.VeteranUiState

@Composable
fun DetailsScreen(id: String, navigateToMainScreen: (String) -> Unit)
{
    val detailsViewModel: DetailsViewModel = hiltViewModel()
    val veteranState = detailsViewModel.veteranState

    LaunchedEffect(Unit) {
        detailsViewModel.loadVeteranById(id)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    )
    {
        when (val state = veteranState.value)
        {
            is VeteranUiState.Error -> {
                Text("Error occurred")
            }
            is VeteranUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center),
                    color = colorResource(R.color.dark_red))
            }
            is VeteranUiState.Success -> {
                val veteran = state.veterans.first()
                detailsViewModel.loadRewards(veteran)
                detailsViewModel.loadAdditionalInfo(veteran)

                val pagerState = rememberPagerState(pageCount = {
                    3
                })
                HorizontalPager(state = pagerState) { page ->
                    when(page){
                        0 -> DetailsContent(
                            veteran = veteran,
                            viewModel = detailsViewModel
                        )
                        1 -> BioContent(
                            viewModel = detailsViewModel
                        )
                        2 -> DocContent(
                            viewModel = detailsViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsContent(veteran: Veteran, viewModel: DetailsViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedReward by remember { mutableIntStateOf(0) }
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
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
            Column (
                modifier = Modifier.weight(1.8f)
            ){
                Card (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, start = 8.dp, top = 8.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = colorResource(R.color.dark_red))
                ){
                    Column {
                        Text(text = veteran.name,
                            color = colorResource(R.color.white),
                            fontSize = 16.sp,
                            fontFamily = mulishFont(),
                            style = TextStyle(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(8.dp)
                        )
                        Text(text = veteran.years,
                            color = colorResource(R.color.white),
                            fontSize = 14.sp,
                            fontFamily = mulishFont(),
                            style = TextStyle(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                FlowRow (
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    mainAxisSpacing = 4.dp,
                    crossAxisSpacing = 4.dp
                ){
                    viewModel.rewardsState.value.forEach { rewardId ->
                        val rewardResId = when (rewardId){
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
                        Image(
                            painter = painterResource(rewardResId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(
                                    size = if (viewModel.rewardsState.value.size > 4) 40.dp
                                    else if (viewModel.rewardsState.value.size > 3) 45.dp else 60.dp
                                )
                                .padding(end = 4.dp)
                                .clickable(onClick = {
                                    selectedReward = rewardId
                                    showDialog = true
                                })
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column (
            modifier = Modifier.fillMaxSize()
        ){
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3.7f)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
            ){
                Text(text = veteran.allInfo,
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
    RewardsDisplay(showDialog = showDialog, selectedReward = selectedReward, onDismiss = {showDialog = false})
}

@Composable
fun SwipeAnimation(){
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("swipe_left.json")
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress = {progress},
        modifier = Modifier
            .size(250.dp)
            .padding(bottom = 16.dp)
    )
}

@Composable
fun RewardsDisplay(showDialog: Boolean, selectedReward: Int, onDismiss: () -> Unit){
    if(showDialog)
    {
        val rewardRes = when(selectedReward){
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
        val rewardName = when(selectedReward){
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
            onDismissRequest = {onDismiss()},
            confirmButton = {},
            text = {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Image(painterResource(rewardRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(320.dp)
                            .padding(8.dp))
                    Text(text = stringResource(rewardName),
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
fun BioContent(viewModel: DetailsViewModel) {
    val infoText by viewModel.additionalText
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.biography),
            style = TextStyle(
                fontFamily = mulishFont(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colorResource(R.color.dark_red)
            ))
        if (infoText.isNotEmpty()){
            TextItem(infoText.first())
        }
    }
}

@Composable
fun TextItem(infoText: String){
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 26.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ){
        Text(text = infoText,
            style = TextStyle(
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = mulishFont()),
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .verticalScroll(rememberScrollState()),
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun DocContent(viewModel: DetailsViewModel){
    val infoRes by viewModel.additionalRes
    val directedUrls by viewModel.directUrls
    var isPlaying by remember { mutableStateOf(false) }
    val pagerResState = rememberPagerState(pageCount = { directedUrls.size })
    LaunchedEffect(infoRes) {
        viewModel.loadDirectedUrl(infoRes)
    }
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
            HorizontalPager(state = pagerResState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
                verticalAlignment = Alignment.CenterVertically) { page ->
                val entry = directedUrls.entries.elementAt(page)
                val url = entry.key
                val describe = entry.value
                ResImageItem(url = url, describe = describe)
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
            thickness = DividerDefaults.Thickness, color = colorResource(R.color.dark_white)
        )
        Spacer(Modifier.height(12.dp))
        AudioTrack(isPlaying = isPlaying) { isPlaying = !isPlaying }
    }
}

@Composable
fun AudioTrack(isPlaying: Boolean, onPlay: () -> Unit){
    Text(
        text = "Аудиоэкскурсия",
        style = TextStyle(
            fontFamily = mulishFont(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colorResource(R.color.dark_red)
        )
    )
    Card (
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp, start = 32.dp, end = 32.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.dark_white))
    ){
        Row (
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Button(onClick = { onPlay() },
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.dark_red),
                    contentColor = colorResource(R.color.white)
                )) {
                if (!isPlaying) {
                    Icon(Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(24.dp),
                        tint = colorResource(R.color.white))
                }
                else {
                    Icon(painterResource(R.drawable.pause),
                        contentDescription = "Play",
                        modifier = Modifier.size(24.dp),
                        tint = colorResource(R.color.white))
                }
            }
            Spacer(Modifier.width(16.dp))
            Text("Биография",
                style = TextStyle(
                    fontFamily = mulishFont(),
                    color = colorResource(R.color.black)
                )
            )
        }
    }
}

@Composable
fun ResImageItem(url: String, describe: String){
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image(painter = rememberAsyncImagePainter(
            model = url,
            placeholder = painterResource(R.drawable.image_info_placeholder)),
            contentDescription = describe,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop)
        if (describe.isNotEmpty()){
            Text(text = describe,
                style = TextStyle(
                    fontFamily = mulishFont(),
                    fontSize = 16.sp,
                    color = colorResource(R.color.dark_red),
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center)
        }
    }
}