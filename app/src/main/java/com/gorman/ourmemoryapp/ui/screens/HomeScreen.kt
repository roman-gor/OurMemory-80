package com.gorman.ourmemoryapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.ui.fonts.mulishFont
import com.gorman.ourmemoryapp.ui.states.HomeUiIntent
import com.gorman.ourmemoryapp.ui.states.HomeUiState
import com.gorman.ourmemoryapp.ui.viewModel.HomeViewModel

@Composable
fun MainScreen(
    onItemClick: (String) -> Unit,
    navigateToInfoScreen: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val onUiIntent = homeViewModel::onUiIntent

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        when (val state = uiState) {
            is HomeUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Error occurred!")
                }
            }
            HomeUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorResource(R.color.dark_red)
                )
            }
            is HomeUiState.Success -> {
                OurMemoryScreen(
                    onItemClick = onItemClick,
                    navigateToInfoScreen = navigateToInfoScreen,
                    state = state,
                    onCheckedArtChange = { onUiIntent(HomeUiIntent.OnCheckedArtChange(it)) },
                    onCheckedWarChange = { onUiIntent(HomeUiIntent.OnCheckedWarChange(it)) },
                    onSearchTextChange = { onUiIntent(HomeUiIntent.OnSearchChange(it)) },
                )
            }
        }
    }
}

@Composable
fun OurMemoryScreen(
    onItemClick: (String) -> Unit,
    navigateToInfoScreen: () -> Unit,
    state: HomeUiState.Success,
    onCheckedArtChange: (Boolean) -> Unit,
    onCheckedWarChange: (Boolean) -> Unit,
    onSearchTextChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Header(
            data = HeaderData(
                navigateToInfoScreen = navigateToInfoScreen,
                onClickEvent = { expanded = true },
                expanded = expanded,
                onDismiss = { expanded = false },
                search = state.search,
                onSearchTextChange = onSearchTextChange,
                checkedArt = state.checkedArt,
                checkedWar = state.checkedWar,
                onCheckedArtChange = onCheckedArtChange,
                onCheckedWarChange = onCheckedWarChange
            )
        )
        if (!state.veterans.isEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.veterans) { veteran ->
                    VeteranItem(
                        veteran,
                        onClick = { onItemClick(veteran.id) }
                    )
                }
            }
        } else {
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .background(color = Color(0xFFF0F0F0))
        ) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.9f)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = veteran.name,
                    color = colorResource(R.color.dark_red),
                    fontFamily = mulishFont(),
                    fontSize = 12.sp,
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = veteran.baseInfo,
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
    checkedWar: Boolean,
    checkedArt: Boolean,
    onCheckedWarChange: (Boolean) -> Unit,
    onCheckedArtChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismiss() },
        containerColor = colorResource(R.color.dark_white)
    ) {
        DropdownMenuItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 4.dp),
            onClick = { onCheckedWarChange(!checkedWar) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checkedWar,
                        modifier = Modifier.size(20.dp),
                        onCheckedChange = { onCheckedWarChange(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colorResource(R.color.dark_red),
                            uncheckedColor = Color.Gray,
                            checkmarkColor = colorResource(R.color.white)
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.heroUSSR),
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
            onClick = { onCheckedArtChange(!checkedArt) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checkedArt,
                        modifier = Modifier.size(20.dp),
                        onCheckedChange = { onCheckedArtChange(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colorResource(R.color.dark_red),
                            uncheckedColor = Color.Gray,
                            checkmarkColor = colorResource(R.color.white)
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.art),
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

data class HeaderData(
    val navigateToInfoScreen: () -> Unit,
    val onClickEvent: () -> Unit,
    val expanded: Boolean,
    val search: String,
    val onDismiss: () -> Unit,
    val onSearchTextChange: (String) -> Unit,
    val checkedArt: Boolean,
    val checkedWar: Boolean,
    val onCheckedArtChange: (Boolean) -> Unit,
    val onCheckedWarChange: (Boolean) -> Unit
)

@Composable
fun Header(data: HeaderData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchField(
            search = data.search,
            onSearchTextChange = { data.onSearchTextChange(it) }
        )
        Button(
            onClick = { data.navigateToInfoScreen() },
            modifier = Modifier.padding(start = 8.dp).aspectRatio(1f).weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.dark_red),
                contentColor = Color.Black
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                painterResource(R.drawable.monument),
                contentDescription = "Monument",
                tint = colorResource(R.color.white),
                modifier = Modifier.padding(8.dp)
            )
        }
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Button(
                onClick = { data.onClickEvent() },
                modifier = Modifier.padding(start = 8.dp)
                    .aspectRatio(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.dark_red),
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.keyboard_arrow_down),
                    contentDescription = null,
                    tint = colorResource(R.color.white)
                )
            }
            DropDownMenu(
                expanded = data.expanded,
                onDismiss = { data.onDismiss() },
                checkedWar = data.checkedWar,
                checkedArt = data.checkedArt,
                onCheckedWarChange = data.onCheckedWarChange,
                onCheckedArtChange = data.onCheckedArtChange
            )
        }
    }
}

@Composable
private fun RowScope.SearchField(
    search: String,
    onSearchTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = search,
        onValueChange = { onSearchTextChange(it) },
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
            focusedContainerColor = colorResource(R.color.dark_red),
            unfocusedContainerColor = colorResource(R.color.dark_red),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent
        )
    )
}
