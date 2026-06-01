package com.usta.query.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }, modifier = modifier) {
        Text(options.getOrElse(selectedIndex) { label }, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        Icon(if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, contentDescription = null)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEachIndexed { index, option ->
            DropdownMenuItem(
                text = { Text(option, fontSize = 13.sp) },
                onClick = {
                    onSelected(index)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun MultiSelectDropdown(
    label: String,
    options: List<Pair<String, String>>,
    selectedValues: Set<String>,
    onToggle: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val count = selectedValues.size
    val displayText = if (count == 0) label else "$label ($count)"
    OutlinedButton(onClick = { expanded = true }) {
        Text(displayText, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        Icon(if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, contentDescription = null)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { (optionLabel, value) ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedValues.contains(value),
                            onCheckedChange = null,
                            modifier = Modifier.width(32.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(optionLabel, fontSize = 13.sp)
                    }
                },
                onClick = { onToggle(value) }
            )
        }
    }
}
