package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BookmarkItem
import com.example.domain.model.HighlightItem
import com.example.ui.components.TactileBookmarkButton
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.ExpressivePillShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    bookmarks: List<BookmarkItem>,
    highlights: List<HighlightItem>,
    onItemClick: (String, String) -> Unit, // id, type
    onRemoveBookmark: (Long, String, String) -> Unit,
    onDeleteHighlight: (Long) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var showNewCollectionDialog by remember { mutableStateOf(false) }
    var collectionNameInput by remember { mutableStateOf("") }
    var activeCollection by remember { mutableStateOf("All Collections") }

    val filterOptions = listOf("All", "Books", "Articles", "Highlights")

    val filteredBookmarks = when (selectedCategory) {
        "Books" -> bookmarks.filter { it.itemType == "BOOK" }
        "Articles" -> bookmarks.filter { it.itemType == "ARTICLE" }
        else -> bookmarks
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("bookmarks_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bookmarks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("bookmarks_search_btn")
                    ) {
                        Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search Bookmarks")
                    }
                    IconButton(
                        onClick = { showNewCollectionDialog = true },
                        modifier = Modifier.testTag("bookmarks_add_collection_btn")
                    ) {
                        Icon(imageVector = Icons.Outlined.CreateNewFolder, contentDescription = "New Collection")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { option ->
                    val isSelected = selectedCategory == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = option },
                        label = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = ExpressivePillShape,
                        modifier = Modifier.testTag("bookmark_filter_$option")
                    )
                }
            }

            if (selectedCategory == "Highlights") {
                // Show Highlights
                if (highlights.isEmpty()) {
                    EmptyBookmarksView(message = "No highlights saved yet.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(highlights) { highlight ->
                            HighlightCard(
                                highlight = highlight,
                                onDelete = { onDeleteHighlight(highlight.id) },
                                onClick = { onItemClick(highlight.contentId, highlight.contentType) }
                            )
                        }
                    }
                }
            } else {
                // Show Bookmarks
                if (filteredBookmarks.isEmpty()) {
                    EmptyBookmarksView(message = "No bookmarks in this section yet.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredBookmarks) { item ->
                            BookmarkItemCard(
                                item = item,
                                onClick = { onItemClick(item.itemId, item.itemType) },
                                onRemove = { onRemoveBookmark(item.id, item.itemId, item.itemType) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewCollectionDialog) {
        AlertDialog(
            onDismissRequest = { showNewCollectionDialog = false },
            title = { Text("Create Collection") },
            text = {
                OutlinedTextField(
                    value = collectionNameInput,
                    onValueChange = { collectionNameInput = it },
                    placeholder = { Text("e.g. Philosophy, Deep Reading") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_collection_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (collectionNameInput.isNotBlank()) {
                            activeCollection = collectionNameInput
                        }
                        showNewCollectionDialog = false
                        collectionNameInput = ""
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCollectionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BookmarkItemCard(
    item: BookmarkItem,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = ExpressiveCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("bookmark_card_${item.itemId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (item.itemType == "BOOK") MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.itemType == "BOOK") Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Outlined.Article,
                    contentDescription = null,
                    tint = if (item.itemType == "BOOK") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.category} · Collection: ${item.collectionName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.testTag("remove_bookmark_btn_${item.itemId}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = "Remove Bookmark",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun HighlightCard(
    highlight: HighlightItem,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = ExpressiveCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("highlight_card_${highlight.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${highlight.title} · ${highlight.sectionOrChapter}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_highlight_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Highlight",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${highlight.selectedText}\"",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }

            if (highlight.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: ${highlight.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyBookmarksView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
