package com.example.bejeweleddemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bejeweleddemo.ui.theme.BejeweledDemoTheme
import kotlin.math.abs

var score = 0
var multiplier = 1
data class GemPosition(val row: Int, val col: Int)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BejeweledDemoTheme {
                // Bejeweled game
                BejeweledGameBoard()
            }
        }
    }
}

@Composable
fun BejeweledGameBoard() {
    val gridSize = 8
    var gemGrid by remember { mutableStateOf(generateGemGrid(gridSize)) }
    var selectedGemPosition by remember { mutableStateOf<GemPosition?>(null) }
    var isGameOver by remember { mutableStateOf(false) }

    fun onGameOver() {
        isGameOver = true
    }

    if (isGameOver) {
        GameOverDialog(score = score) {
            isGameOver = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Score: $score", modifier = Modifier.padding(16.dp))

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

                                //Reset the multiplier
                                multiplier = 1

                                // Process the game board for matches and updates
                                if (processGameBoard(newGemGrid)) {
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

        Button(
            onClick = {
                // Regenerate the gem grid
                gemGrid = generateGemGrid(gridSize) // Update gemGrid
                score = 0 // Reset the score
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Regenerate Grid")
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

/*fun findMatches(grid: List<List<GemType>>): List<GemPosition> {
    val matches = mutableListOf<GemPosition>()

    for (i in grid.indices) {
        for (j in 0 until grid[i].size - 2) {
            if (grid[i][j] != GemType.EMPTY &&
                grid[i][j] == grid[i][j + 1] && grid[i][j] == grid[i][j + 2]) {
                matches.add(GemPosition(i, j))
                matches.add(GemPosition(i, j + 1))
                matches.add(GemPosition(i, j + 2))
            }
        }
    }

    for (j in grid[0].indices) {
        for (i in 0 until grid.size - 2) {
            if (grid[i][j] != GemType.EMPTY &&
                grid[i][j] == grid[i + 1][j] && grid[i][j] == grid[i + 2][j]) {
                matches.add(GemPosition(i, j))
                matches.add(GemPosition(i + 1, j))
                matches.add(GemPosition(i + 2, j))
            }
        }
    }

    return matches.distinct()
}*/

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

fun dropGems(grid: MutableList<MutableList<GemType>>, columnsToDrop: List<Int>) {
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
        processGameBoard(grid)
    }
}

fun removeMatches(grid: MutableList<MutableList<GemType>>, matches: List<GemPosition>): List<Int> {
    val columnsToDrop = mutableListOf<Int>()
    val pointsPer3Gems = 50
    val pointsPer4Gems = 100
    val pointsPer5Gems = 1000

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
        grid[x][y] = GemType.EMPTY
        if (y !in columnsToDrop) {
            columnsToDrop.add(y)
        }
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

fun processGameBoard(grid: MutableList<MutableList<GemType>>): Boolean {
    val matches = findMatches(grid)
    if (matches.isNotEmpty()) {
        val columnsToDrop = removeMatches(grid, matches)
        dropGems(grid, columnsToDrop)
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

@Composable
fun GameOverDialog(score: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* TODO: Handle dismiss request */ },
        title = { Text(text = "Game Over") },
        text = { Text("No more possible moves. Your score: $score") },
        confirmButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("OK")
            }
        }
    )
}

enum class GemType(val drawableResId: Int) {
    AMBER(R.drawable._pentagram),
    AMETHYST(R.drawable._x_alt1),
    DIAMOND(R.drawable._circle_alt1),
    EMERALD(R.drawable._tiimalasi_alt1),
    RUBY(R.drawable._square_alt1),
    SAPPHIRE(R.drawable._kolmio_alt1),
    TOPAZ(R.drawable._ruutu_alt1),
    EMPTY(R.drawable.empty)
}

@Preview(showBackground = true)
@Composable
fun BejeweledGameBoardPreview() {
    BejeweledDemoTheme {
        BejeweledGameBoard()
    }
}