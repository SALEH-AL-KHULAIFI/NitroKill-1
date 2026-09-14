package com.isx3i.nitrokill.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.isx3i.nitrokill.R
import com.isx3i.nitrokill.data.UsageRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onOpenOptions: () -> Unit,
    onReenable: () -> Unit,
    onHideAndExit: () -> Unit,
    onToggleMonitor: (Boolean) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var monitorOn by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.main_title)) },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_options)) },
                            onClick = { menuExpanded = false; onOpenOptions() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_reenable)) },
                            onClick = { menuExpanded = false; onReenable() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_hide_exit)) },
                            onClick = { menuExpanded = false; onHideAndExit() }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (monitorOn) stringResource(R.string.stop_monitor) else stringResource(R.string.start_monitor),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = monitorOn,
                    onCheckedChange = {
                        monitorOn = it
                        onToggleMonitor(it)
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            DisclaimerCard()

            Spacer(Modifier.height(16.dp))
            UsageTable()

            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.about_developer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun DisclaimerCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(8.dp))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.disclaimer_title),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.disclaimer_body),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun UsageTable() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val usage = remember { UsageRepository(context).getTodayUsage() }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(stringResource(R.string.table_title), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TableHeaderCell(stringResource(R.string.table_date))
                TableHeaderCell(stringResource(R.string.table_mobile))
                TableHeaderCell(stringResource(R.string.table_transfer))
                TableHeaderCell(stringResource(R.string.table_total))
            }
            Row(Modifier.fillMaxWidth().background(Color(0xFFF0F0F0)).padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                TableCell(usage.date)
                TableCell(UsageRepository.formatBytes(usage.mobileBytes))
                TableCell(UsageRepository.formatBytes(usage.transferBytes))
                TableCell(UsageRepository.formatBytes(usage.totalBytes))
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(4.dp))
}

@Composable
private fun TableCell(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(4.dp))
}
