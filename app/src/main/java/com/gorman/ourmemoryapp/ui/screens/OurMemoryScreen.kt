package com.gorman.ourmemoryapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.gorman.ourmemoryapp.ui.fonts.mulishFont
import com.gorman.ourmemoryapp.viewModel.OurMemoryViewModel
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.data.Veteran
import com.gorman.ourmemoryapp.data.VeteranUiState
import com.gorman.ourmemoryapp.ui.fonts.inriaFont

@Composable
fun MainScreen(onItemClick: (String) -> Unit, navigateToInfoScreen: () -> Unit)
{
    val ourMemoryViewModel: OurMemoryViewModel = viewModel()
    val veteranState = ourMemoryViewModel.veteranState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                    onItemClick = onItemClick,
                    navigateToInfoScreen = navigateToInfoScreen
                )
            }
        }
    }
}

@Composable
fun OurMemoryScreen(
    veterans: List<Veteran>,
    onItemClick: (String) -> Unit,
    navigateToInfoScreen: () -> Unit)
{
    var searchVeteran by remember { mutableStateOf("") }
    val filteredVeteran = if (searchVeteran.isBlank())
        veterans
    else
        veterans.filter {
            it.name.contains(searchVeteran, ignoreCase = true)
        }
    Column {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            OutlinedTextField(
                value = searchVeteran,
                onValueChange = { searchVeteran = it },
                placeholder = { Text(
                    text = "Поиск",
                    style = TextStyle(
                        fontFamily = mulishFont(),
                        fontSize = 14.sp,
                        color = Color.Gray))},
                textStyle = TextStyle(
                    fontFamily = mulishFont(),
                    fontSize = 14.sp,
                    color = Color.Black),
                modifier = Modifier.weight(5f),
                shape = RoundedCornerShape(32.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.search_icon),
                        contentDescription = null,
                        tint = colorResource(R.color.dark_red))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent
                )
            )
            Button(onClick = {
                navigateToInfoScreen()
            },
                modifier = Modifier.padding(start = 8.dp)
                    .aspectRatio(1f)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0F0F0),
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(0.dp)) {
                Text("i",
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 26.sp,
                        fontFamily = inriaFont()
                    ),
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.dark_red)
                )
            }
        }
        LazyColumn (modifier = Modifier
            .fillMaxSize()
        )
        {
            items(filteredVeteran)
            { veteran ->
                VeteranItem(veteran, onClick = { onItemClick(veteran.id) })
            }
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
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row (
            modifier = Modifier
                .background(color = Color(0xFFF0F0F0))
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
                    .padding(10.dp)
                    .align(Alignment.Top)
            )
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.8f)
                    .padding(top = 8.dp)
            ){
                Text(text = veteran.name,
                    color = colorResource(R.color.dark_red),
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