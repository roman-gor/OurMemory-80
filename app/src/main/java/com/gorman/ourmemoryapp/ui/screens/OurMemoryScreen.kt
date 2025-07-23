package com.gorman.ourmemoryapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.gorman.ourmemoryapp.ui.fonts.mulishFont
import com.gorman.ourmemoryapp.viewModel.OurMemoryViewModel
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.data.Veteran
import com.gorman.ourmemoryapp.data.VeteranUiState

@Composable
fun MainScreen(onItemClick: (String) -> Unit)
{
    val ourMemoryViewModel: OurMemoryViewModel = viewModel()
    val veteranState = ourMemoryViewModel.veteranState

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFFF0F0F0))
    )
    {
        when(val state = veteranState.value)
        {
            is VeteranUiState.Error -> {
                Text("Error occurred!")
            }
            VeteranUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is VeteranUiState.Success -> {
                OurMemoryScreen(
                    veterans = state.veterans,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Composable
fun OurMemoryScreen(
    veterans: List<Veteran>,
    onItemClick: (String) -> Unit)
{
    LazyColumn (modifier = Modifier.fillMaxSize()
        .padding(top = 8.dp))
        {
        items(veterans)
        {
            veteran ->
            VeteranItem(veteran, onClick = { onItemClick(veteran.id) })
        }
    }
}

@Composable
fun VeteranItem(
    veteran: Veteran,
    onClick: () -> Unit)
{
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row (
            modifier = Modifier
                .background(color = Color.LightGray)
        ){
            Image(
                painter = rememberAsyncImagePainter(
                    model = veteran.portrait,
                    placeholder = painterResource(R.drawable.placeholder)
                ),
                contentDescription = "Portrait of veteran",
                modifier = Modifier
                    .weight(1.2f)
                    .aspectRatio(1f)
                    .padding(8.dp)
                    .align(Alignment.CenterVertically)
            )
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.8f)
                    .padding(top = 8.dp)
            ){
                Text(text = veteran.name,
                    color = Color.Black,
                    fontFamily = mulishFont(),
                    fontSize = 12.sp,
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(8.dp)
                )
                Text(text = veteran.baseInfo,
                    color = Color.Black,
                    fontFamily = mulishFont(),
                    fontSize = 11.sp,
                    style = TextStyle(fontWeight = FontWeight.Normal),
                    modifier = Modifier.padding(start = 8.dp, bottom = 16.dp, end = 8.dp)
                )
            }
        }
    }
}