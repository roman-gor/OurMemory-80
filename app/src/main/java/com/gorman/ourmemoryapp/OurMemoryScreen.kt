package com.gorman.ourmemoryapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

@Composable
fun MainScreen(modifier: Modifier = Modifier)
{
    val ourMemoryViewModel: OurMemoryViewModel = viewModel()
    val veteranState = ourMemoryViewModel.veteranState

    Box(
        modifier = Modifier.fillMaxSize()
    )
    {
        when(val state = veteranState.value)
        {
            is VeteranUiState.Error -> {
                Text("Error occurred!")
            }
            VeteranUiState.Loading -> {
                CircularProgressIndicator(modifier.align(Alignment.Center))
            }
            is VeteranUiState.Success -> {
                OurMemoryScreen(veterans = state.veterans)
            }
        }
    }
}

@Composable
fun OurMemoryScreen(veterans: List<Veteran>)
{
    LazyColumn (modifier = Modifier.fillMaxSize())
        {
        items(veterans)
        {
            veteran ->
            VeteranItem(veteran)
        }
    }
}

@Composable
fun VeteranItem(veteran: Veteran)
{
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(color = Color.LightGray)
    ){
        Image(
            painter = rememberAsyncImagePainter(veteran.portrait),
            contentDescription = "Portrait of veteran",
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .padding(top = 8.dp, bottom = 8.dp)
        )
        Column (
            modifier = Modifier
                .weight(2f)
        ){
            Text(text = veteran.name,
                color = Color.Black,
                fontSize = 12.sp,
                style = TextStyle(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(8.dp)
            )
            Text(text = veteran.baseInfo,
                color = Color.Black,
                fontSize = 11.sp,
                style = TextStyle(fontWeight = FontWeight.Normal),
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
            )
        }
    }
}