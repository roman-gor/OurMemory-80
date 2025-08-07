package com.gorman.ourmemoryapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
fun MainScreen(onItemClick: (String) -> Unit, navigateToInfoScreen: () -> Unit)
{
    val ourMemoryViewModel: OurMemoryViewModel = viewModel()
    val veteranState = ourMemoryViewModel.veteranState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    )
    {
        when(veteranState.value)
        {
            is VeteranUiState.Error -> {
                Text("Error occurred!")
            }
            VeteranUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center),
                    color = colorResource(R.color.dark_red))
            }
            is VeteranUiState.Success -> {
                OurMemoryScreen(
                    onItemClick = onItemClick,
                    navigateToInfoScreen = navigateToInfoScreen,
                    viewModel = ourMemoryViewModel
                )
            }
        }
    }
}

@Composable
fun OurMemoryScreen(
    onItemClick: (String) -> Unit,
    navigateToInfoScreen: () -> Unit,
    viewModel: OurMemoryViewModel)
{
    val filteredVeteran by viewModel.filteredVeterans
    var expanded by remember { mutableStateOf(false) }
    Column {
        Header(viewModel = viewModel,
            navigateToInfoScreen = navigateToInfoScreen,
            onClickEvent = {expanded = true},
            expanded = expanded,
            onDismiss = {expanded = false}
        )
        if (!filteredVeteran.isEmpty()){
            LazyColumn (modifier = Modifier
                .fillMaxSize()
            )
            {
                items(filteredVeteran)
                { veteran ->
                    VeteranItem(veteran,
                        onClick = { onItemClick(veteran.id) })
                }
            }
        }
        else{
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.chooseCategory),
                    style = TextStyle(
                        fontFamily = mulishFont(),
                        fontSize = 20.sp,
                        color = colorResource(R.color.dark_red),
                        fontWeight = FontWeight.Bold
                    )
                )
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
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1.1f)
                    .aspectRatio(0.8f)
                    .padding(10.dp)
                    .align(Alignment.Top)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.9f)
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

@Composable
fun DropDownMenu(
    expanded: Boolean,
    viewModel: OurMemoryViewModel,
    onDismiss: () -> Unit){
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismiss() },
        containerColor = colorResource(R.color.dark_white)
    ){
        DropdownMenuItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 4.dp),
            onClick = {viewModel.onCheckedWarChange(!viewModel.checkedWarState.value)},
            text = {
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Checkbox(
                        checked = viewModel.checkedWarState.value,
                        modifier = Modifier.size(20.dp),
                        onCheckedChange = {viewModel.onCheckedWarChange(it)},
                        colors = CheckboxDefaults.colors(
                            checkedColor = colorResource(R.color.dark_red),
                            uncheckedColor = Color.Gray,
                            checkmarkColor = colorResource(R.color.white)
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(R.string.heroUSSR),
                        style = TextStyle(
                            fontFamily = mulishFont(),
                            fontSize = 14.sp,
                            color = colorResource(R.color.dark_red)
                        )
                    )
                }
            },
        )
        DropdownMenuItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 4.dp),
            onClick = {viewModel.onCheckedArtChange(!viewModel.checkedArtState.value)},
            text = {
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Checkbox(
                        checked = viewModel.checkedArtState.value,
                        modifier = Modifier.size(20.dp),
                        onCheckedChange = {viewModel.onCheckedArtChange(it)},
                        colors = CheckboxDefaults.colors(
                            checkedColor = colorResource(R.color.dark_red),
                            uncheckedColor = Color.Gray,
                            checkmarkColor = colorResource(R.color.white)
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(R.string.art),
                        style = TextStyle(
                            fontFamily = mulishFont(),
                            fontSize = 14.sp,
                            color = colorResource(R.color.dark_red)
                        )
                    )
                }
            }
        )
    }
}

@Composable
fun Header(
    viewModel: OurMemoryViewModel,
    navigateToInfoScreen: () -> Unit,
    onClickEvent: () -> Unit,
    expanded: Boolean,
    onDismiss: () -> Unit){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = viewModel.searchState.value,
            onValueChange = { viewModel.onSearchTextChange(it) },
            placeholder = {
                Text(
                    text = stringResource(R.string.search),
                    style = TextStyle(
                        fontFamily = mulishFont(),
                        fontSize = 14.sp,
                        color = Color.White
                    )
                )
            },
            textStyle = TextStyle(
                fontFamily = mulishFont(),
                fontSize = 14.sp,
                color = Color.White
            ),
            modifier = Modifier.weight(4f),
            shape = RoundedCornerShape(32.dp),
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.search_icon),
                    contentDescription = null,
                    tint = colorResource(R.color.white)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
//                focusedContainerColor = Color(0xFFF0F0F0),
//                unfocusedContainerColor = Color(0xFFF0F0F0),
                focusedContainerColor = colorResource(R.color.dark_red),
                unfocusedContainerColor = colorResource(R.color.dark_red),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent
            )
        )
        Button(
            onClick = {
                navigateToInfoScreen()
            },
            modifier = Modifier.padding(start = 8.dp)
                .aspectRatio(1f)
                .weight(1f),
            colors = ButtonDefaults.buttonColors(
                //containerColor = Color(0xFFF0F0F0),
                containerColor = colorResource(R.color.dark_red),
                contentColor = Color.Black
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(painterResource(R.drawable.monument),
                contentDescription = "Monument",
                tint = colorResource(R.color.white),
                modifier = Modifier.padding(8.dp))
        }
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Button(
                onClick = {
                    onClickEvent()
                },
                modifier = Modifier.padding(start = 8.dp)
                    .aspectRatio(1f),
                colors = ButtonDefaults.buttonColors(
                    //containerColor = Color(0xFFF0F0F0),
                    containerColor = colorResource(R.color.dark_red),
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colorResource(R.color.white)
                )
            }
            DropDownMenu(expanded = expanded, viewModel = viewModel, onDismiss = { onDismiss() })
        }
    }
}