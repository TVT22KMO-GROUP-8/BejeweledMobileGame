package com.example.bejeweled.ui

import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bejeweled.R
import com.example.bejeweled.ui.navigation.NavigationDestination
import com.example.bejeweled.ui.theme.BejeweledTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.PrimaryKey
import com.example.bejeweled.data.ScoreboardDetails
import com.example.bejeweled.data.ScoreboardUiState
import com.example.bejeweled.data.ScoreboardViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import kotlin.math.abs

object GameBoardDestination : NavigationDestination {
    override val route = "game_board"
    override val titleRes = R.string.game_board_title
}
var score = 0
var multiplier = 1
data class GemPosition(val row: Int, val col: Int)
data class GemHit(val gemType: GemType, val count: Int)


@Composable
fun BejeweledGameBoard(
    modifier: Modifier = Modifier,
    viewModel: ScoreboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
    sharedPreferences: SharedPreferences
) {
    val database = Firebase.database("https://bejeweledmobiiliprojekti-default-rtdb.europe-west1.firebasedatabase.app/")
    val gridSize = 8
    var gemGrid by remember { mutableStateOf(generateGemGrid(gridSize)) }
    var selectedGemPosition by remember { mutableStateOf<GemPosition?>(null) }
    var isGameOver by remember { mutableStateOf(false) }
    var removedGemsHistory by remember { mutableStateOf<List<GemHit>>(emptyList()) }

    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.le_bijouterie_light)}

    // Start playing the music when the game starts
    DisposableEffect(Unit) {
        mediaPlayer.start()
        mediaPlayer.isLooping = true

        onDispose {
            mediaPlayer.release()
        }
    }


    fun onGameOver() {
        isGameOver = true
        gemGrid = generateGemGrid(gridSize) // Update gemGrid

    }

    if (isGameOver) {
        mediaPlayer.stop()
        GameOverDialog(
            onDismiss = { isGameOver = false },
            scoreboardUiState = viewModel.scoreboardUiState,
            sharedPreferences = sharedPreferences,
            database = database,


        )

    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Score: $score", modifier = Modifier.padding(16.dp))

        //Gridi
        Column(
            modifier = Modifier.weight(1f)
        ) {
            for (i in 0 until gridSize) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (j in 0 until gridSize) {
                        GridCell(gemType = gemGrid[i][j]) {
                            // Handle gem click
                            if (selectedGemPosition == null) {
                                // First gem click
                                selectedGemPosition = GemPosition(i, j)
                            } else {
                                // Second gem click, swap the gems and process the board
                                val (x1, y1) = selectedGemPosition!!
                                val (x2, y2) = GemPosition(i, j)

                                val isAdjacent = (x1 == x2 && abs(y1 - y2) == 1) || (y1 == y2 && abs(x1 - x2) == 1)

                                if (isAdjacent) {
                                    val newGemGrid = gemGrid.map { it.toMutableList() }.toMutableList()
                                    swapGems(newGemGrid, x1, y1, x2, y2)

                                    // Reset the multiplier and process the game board
                                    multiplier = 1

                                    // Clear the removed gems history as new pairs are created manually
                                    removedGemsHistory = emptyList()

                                    if (processGameBoard(newGemGrid, removedGemsHistory.toMutableList()) { updatedHistory ->
                                            removedGemsHistory = updatedHistory
                                        }) {
                                        gemGrid = newGemGrid // Update gemGrid only if there were changes
                                        // Check if the game is over
                                        if (isGameOver(gemGrid)) {
                                            onGameOver()
                                        }
                                    }
                                }
                                selectedGemPosition = null
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            removedGemsHistory.forEach { gemHit ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = gemHit.gemType.drawableResId),
                        contentDescription = "Removed Gem",
                        modifier = Modifier.size(48.dp)
                    )
                    Text(text = "x${gemHit.count}")
                }
            }
        }


        //Restart button
        Button(
            onClick = {
                onGameOver()// Activate GameOver and Regenerate the gem grid
                mediaPlayer.start() // Restart the music
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Restart Game")
        }
      

    }
}


fun generateGemGrid(gridSize: Int): List<List<GemType>> {
    val gemGrid = MutableList(gridSize) {
        MutableList(gridSize) {
            GemType.values().filter { it != GemType.EMPTY }.random() // Exclude EMPTY in initial assignment
        }
    }

    // Check for and prevent three identical gems in a row
    for (i in 0 until gridSize) {
        for (j in 2 until gridSize) {
            while (gemGrid[i][j] == gemGrid[i][j - 1] && gemGrid[i][j] == gemGrid[i][j - 2]) {
                gemGrid[i][j] = GemType.values().filter { it != GemType.EMPTY }.random()
            }
        }
    }

    // Check for and prevent three identical gems in a column
    for (j in 0 until gridSize) {
        for (i in 2 until gridSize) {
            while (gemGrid[i][j] == gemGrid[i - 1][j] && gemGrid[i][j] == gemGrid[i - 2][j]) {
                gemGrid[i][j] = GemType.values().filter { it != GemType.EMPTY }.random()
            }
        }
    }

    return gemGrid
}

fun swapGems(grid: MutableList<MutableList<GemType>>, x1: Int, y1: Int, x2: Int, y2: Int) {
    val temp = grid[x1][y1]
    grid[x1][y1] = grid[x2][y2]
    grid[x2][y2] = temp
}

@Composable
fun GridCell(
    gemType: GemType,
    onGemClick: () -> Unit
) {
    val gemDrawableRes = gemType.drawableResId

    Image(
        painter = painterResource(id = gemDrawableRes),
        contentDescription = null,
        modifier = Modifier
            .size(48.dp)
            .clickable { onGemClick() } // Handle gem click
    )
}

fun findMatches(grid: List<List<GemType>>): List<GemPosition> {
    val matches = mutableListOf<GemPosition>()

    // Check for horizontal matches
    for (row in grid.indices) {
        for (col in 0 until grid[row].size - 2) {
            if (grid[row][col] != GemType.EMPTY &&
                grid[row][col] == grid[row][col + 1] &&
                grid[row][col] == grid[row][col + 2]) {

                matches.add(GemPosition(row, col))
                matches.add(GemPosition(row, col + 1))
                matches.add(GemPosition(row, col + 2))
            }
        }
    }

    // Check for vertical matches
    for (col in grid[0].indices) {
        for (row in 0 until grid.size - 2) {
            if (grid[row][col] != GemType.EMPTY &&
                grid[row][col] == grid[row + 1][col] &&
                grid[row][col] == grid[row + 2][col]) {

                matches.add(GemPosition(row, col))
                matches.add(GemPosition(row + 1, col))
                matches.add(GemPosition(row + 2, col))
            }
        }
    }

    return matches.distinct()
}

fun dropGems(
    grid: MutableList<MutableList<GemType>>,
    columnsToDrop: List<Int>,
    removedGemsHistory: MutableList<GemHit>,  // Use GemHit instead of GemType
    updateRemovedGemsHistory: (List<GemHit>) -> Unit  // Expect a list of GemHit
) {
    var changesMade = false

    for (col in columnsToDrop) {
        // Drop existing gems down and check if changes were made
        for (row in grid.size - 1 downTo 0) {
            if (grid[row][col] == GemType.EMPTY) {
                var newRow = row - 1
                while (newRow >= 0 && grid[newRow][col] == GemType.EMPTY) {
                    newRow--
                }
                if (newRow >= 0) {
                    grid[row][col] = grid[newRow][col]
                    grid[newRow][col] = GemType.EMPTY
                    changesMade = true
                }
            }
        }

        // Fill the top rows with new random gems
        for (row in 0 until grid.size) {
            if (grid[row][col] == GemType.EMPTY) {
                grid[row][col] = GemType.values().filter { it != GemType.EMPTY }.random()
                changesMade = true
            }
        }
    }

    // Call processGameBoard if changes were made
    if (changesMade) {
        processGameBoard(grid, removedGemsHistory, updateRemovedGemsHistory)
    }
}

fun removeMatches(
    grid: MutableList<MutableList<GemType>>,
    matches: List<GemPosition>,
    removedGemsHistory: MutableList<GemHit>
): List<Int> {
    val columnsToDrop = mutableListOf<Int>()
    val pointsPer3Gems = 50
    val pointsPer4Gems = 100
    val pointsPer5Gems = 1000

    // Temporary list to store the gems being removed in this match
    val currentRemovedGems = mutableListOf<GemType>()

    // Group matches by rows or columns to count how many gems in each match
    val groupedMatches = groupMatches(matches)

    for ((_, gemsInMatch) in groupedMatches) {
        val matchScore = when (gemsInMatch.size) {
            3 -> pointsPer3Gems
            4 -> pointsPer4Gems
            5 -> pointsPer5Gems
            else -> 0 // Default case, though ideally this should not happen
        }
        score += matchScore * multiplier
    }

    multiplier++

    for ((x, y) in matches) {
        // Add the gem being removed to the currentRemovedGems list
        currentRemovedGems.add(grid[x][y])

        grid[x][y] = GemType.EMPTY
        if (y !in columnsToDrop) {
            columnsToDrop.add(y)
        }
    }

    // Update the removedGemsHistory
    val removalCounts = currentRemovedGems.groupingBy { it }.eachCount()
    removalCounts.forEach { (gem, count) ->
        removedGemsHistory.add(GemHit(gem, count)) // Append to the end
    }

    // Keep only the last five entries
    if (removedGemsHistory.size > 7) {
        removedGemsHistory.subList(0, removedGemsHistory.size - 7).clear()
    }

    return columnsToDrop
}

fun groupMatches(matches: List<GemPosition>): Map<Int, List<GemPosition>> {
    // Group by row and column
    val groupedByRow = matches.groupBy { it.row }
    val groupedByColumn = matches.groupBy { it.col }

    // Combine the groups
    val combinedGroups = mutableMapOf<Int, MutableList<GemPosition>>()

    groupedByRow.forEach { (row, gems) ->
        combinedGroups.getOrPut(row) { mutableListOf() }.addAll(gems)
    }

    groupedByColumn.forEach { (col, gems) ->
        combinedGroups.getOrPut(col) { mutableListOf() }.addAll(gems)
    }

    // Remove duplicates and ensure each group has at least 3 gems
    return combinedGroups.mapValues { (_, gems) ->
        gems.distinct().filter { gem ->
            gems.count { it.row == gem.row } >= 3 || gems.count { it.col == gem.col } >= 3
        }
    }
}

fun processGameBoard(
    grid: MutableList<MutableList<GemType>>,
    removedGemsHistory: MutableList<GemHit>, // Updated to work with GemHit
    updateRemovedGemsHistory: (List<GemHit>) -> Unit // Updated to expect a list of GemHit
): Boolean {
    val matches = findMatches(grid)
    if (matches.isNotEmpty()) {
        val currentRemovedGems = matches.map { grid[it.row][it.col] }
        val columnsToDrop = removeMatches(grid, matches, removedGemsHistory)

        // Update removed gems history using the callback
        updateRemovedGemsHistory(removedGemsHistory)

        dropGems(grid, columnsToDrop, removedGemsHistory, updateRemovedGemsHistory)
        return true
    }
    return false
}

fun isGameOver(gemGrid: List<List<GemType>>): Boolean {
    for (i in gemGrid.indices) {
        for (j in gemGrid[i].indices) {
            // Check for potential swap to the right
            if (j < gemGrid[i].size - 1) {
                if (checkSwapForMatch(gemGrid, i, j, i, j + 1)) {
                    return false
                }
            }
            // Check for potential swap to the bottom
            if (i < gemGrid.size - 1) {
                if (checkSwapForMatch(gemGrid, i, j, i + 1, j)) {
                    return false
                }
            }
        }
    }
    return true // No more moves available
}

fun checkSwapForMatch(grid: List<List<GemType>>, x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
    // Create a deep copy of the grid to perform temporary operations
    val tempGrid = grid.map { it.toMutableList() }.toMutableList()

    // Temporarily swap gems in the copy
    val temp = tempGrid[x1][y1]
    tempGrid[x1][y1] = tempGrid[x2][y2]
    tempGrid[x2][y2] = temp

    // Check for matches in the copy
    val hasMatch = findMatches(tempGrid).isNotEmpty()

    // No need to swap back as we used a copy
    return hasMatch
}

fun addNewScore(scoreboardDetails: ScoreboardDetails, database: FirebaseDatabase) {
    val scoreboardDetailsRef = database.getReference("scoreboardDetails")
    val scoreboardDetailsId = scoreboardDetailsRef.push().key
    scoreboardDetailsId?.let {
        scoreboardDetailsRef.child(it).setValue(scoreboardDetails)
    }
}


@Composable
fun GameOverDialog(
    onDismiss: () -> Unit,
    scoreboardUiState: ScoreboardUiState,
    sharedPreferences: SharedPreferences,
    database: FirebaseDatabase,

) {
    AlertDialog(
        onDismissRequest = { /* TODO: Handle dismiss request */ },
        title = { Text(text = "Game Over") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreboardInputForm(
                    scoreboardDetails = scoreboardUiState.scoreboardDetails,
                    sharedPreferences = sharedPreferences
                )}},

                confirmButton = {
                    Button(
                        onClick = {
                            onDismiss()
                            addNewScore(scoreboardUiState.scoreboardDetails, database)
                            score = 0 // Reset the score


                        }
                    ) {
                        Text("OK")
                    }
                }
                )
            }



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardInputForm(
    scoreboardDetails: ScoreboardDetails,
    modifier: Modifier = Modifier,
    sharedPreferences: SharedPreferences
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {

        scoreboardDetails.name = sharedPreferences.getString("name", "Your Name") ?: "Your Name"
        scoreboardDetails.score = score
        Text(text = "Score : ${scoreboardDetails.score}")
        Text(text = "Name : ${scoreboardDetails.name}")

    }
}


enum class GemType(val drawableResId: Int) {
    AMBER(R.drawable._circle_alt1),
    AMETHYST(R.drawable._kolmio_alt1),
    DIAMOND(R.drawable._pentagram),
    EMERALD(R.drawable._ruutu_alt1),
    RUBY(R.drawable._square_alt1),
    SAPPHIRE(R.drawable._tiimalasi_alt1),
    TOPAZ(R.drawable._x_alt1),
    EMPTY(R.drawable.empty)
}


@Preview(showBackground = true)
@Composable
fun BejeweledGameBoardPreview() {
    BejeweledTheme {
//        BejeweledGameBoard(sharedPreferences = SharedPreferences)
    }
}