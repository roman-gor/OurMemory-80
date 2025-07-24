package com.gorman.ourmemoryapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.gorman.ourmemoryapp.viewModel.DetailsViewModel
import com.gorman.ourmemoryapp.ui.fonts.mulishFont
import com.gorman.ourmemoryapp.data.Veteran
import com.gorman.ourmemoryapp.data.VeteranUiState

@Composable
fun DetailsScreen(id: String, navigateToMainScreen: (String) -> Unit)
{
    val detailsViewModel: DetailsViewModel = viewModel()
    val veteranState = detailsViewModel.veteranState

    LaunchedEffect(Unit) {
        detailsViewModel.loadVeteranById(id)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    )
    {
        when (val state = veteranState.value)
        {
            is VeteranUiState.Error -> {
                Text("Error occurred")
            }
            is VeteranUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is VeteranUiState.Success -> {
                DetailsContent(
                    veteran = state.veterans.first()
                )
            }
        }
    }
}

@Composable
fun DetailsContent(veteran: Veteran)
{

    Column (
        modifier = Modifier.fillMaxSize()
            .padding(top = 16.dp)
    ) {
        Row {
            Image(
                painter = rememberAsyncImagePainter(model = veteran.portrait),
                contentDescription = null,
                modifier = Modifier
                    .width(160.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .align(Alignment.CenterVertically)
            )
            Column {
                Text(text = veteran.name,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontFamily = mulishFont(),
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(8.dp)
                )
                Text(text = veteran.years,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontFamily = mulishFont(),
                    style = TextStyle(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = veteran.allInfo,
            color = Color.Black,
            fontSize = 16.sp,
            fontFamily = mulishFont(),
            style = TextStyle(fontWeight = FontWeight.Normal),
            modifier = Modifier
                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                .verticalScroll(rememberScrollState()),
            textAlign = TextAlign.Justify
        )
    }
}