package com.aurora.store.compose.ui.request

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.aurora.store.R
import com.aurora.store.viewmodel.request.AppRequestItem
import com.aurora.store.viewmodel.request.AppRequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRequestScreen(
    initialQuery: String = "",
    onNavigateBack: () -> Unit,
    viewModel: AppRequestViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val items by viewModel.items.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf(initialQuery) }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) {
            viewModel.search(initialQuery)
        }
    }

    val selectedCount = items.count { it.isSelected && !it.isAuthorized && !it.isSubmitted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("בקשת אפליקציות") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "חזרה"
                        )
                    }
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selectedCount > 0,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { viewModel.submitSelected() },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "שלח בקשה ($selectedCount ${if (selectedCount > 1) "אפליקציות" else "אפליקציה"})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // שורת חיפוש
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("חפש שם אפליקציה לבקשה...") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_search),
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            viewModel.search("")
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_cancel),
                                contentDescription = "נקה"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        viewModel.search(query)
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // הסבר עדין
            Text(
                text = "סמן ב-✓ את האפליקציות שברצונך לבקש, והן יישלחו לבדיקת מנהל המערכת.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (items.isEmpty() && query.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "לא נמצאו תוצאות. נסה לחפש מילות מפתח אחרות.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.app.packageName }) { item ->
                        AppRequestRow(
                            item = item,
                            onToggle = { viewModel.toggleSelection(item.app.packageName) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AppRequestRow(
    item: AppRequestItem,
    onToggle: () -> Unit
) {
    val isActionable = !item.isAuthorized && !item.isSubmitted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Checkbox לבחירה
        Checkbox(
            checked = item.isSelected,
            onCheckedChange = { if (isActionable) onToggle() },
            enabled = isActionable
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 2. אייקון (מפוקסל כמו נטפרי אם האפליקציה לא מאושרת!)
        AppIconWithPixelation(
            title = item.app.displayName,
            iconUrl = item.app.iconArtwork.url,
            isPixelated = !item.isAuthorized
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 3. פרטי האפליקציה
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.app.displayName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.app.developerName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.app.packageName,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 4. תגית סטטוס
        when {
            item.isAuthorized -> {
                StatusBadge(text = "כבר בחנות", color = Color(0xFF16A34A), bg = Color(0xFFDCFCE7))
            }
            item.isSubmitted -> {
                StatusBadge(text = "נשלח ✓", color = Color(0xFF2563EB), bg = Color(0xFFDBEAFE))
            }
        }
    }
}

/**
 * מציג אייקון מפוקסל אמיתי בסגנון נטפרי
 */
@Composable
private fun AppIconWithPixelation(
    title: String,
    iconUrl: String,
    isPixelated: Boolean
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        // גיבוי תמיד: אות ראשונה יפה במידה והתמונה חסומה ברשת/בנטפרי
        Text(
            text = title.take(1).uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        if (iconUrl.isNotBlank()) {
            // אם מפוקסל: נדגום מגוגל תמונה זעירה (s12) ונמתח אותה ללא החלקה לקבלת פסיפס פיקסלים מושלם
            val finalUrl = if (isPixelated && iconUrl.contains("=")) {
                iconUrl.substringBeforeLast("=") + "=s12"
            } else {
                iconUrl
            }

            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(finalUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                filterQuality = if (isPixelated) FilterQuality.None else FilterQuality.Medium
            )
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color, bg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
