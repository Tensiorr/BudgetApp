package com.tensiorr.budgetapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.data.dao.CategoryDao
import com.tensiorr.budgetapp.data.dao.TagDao
import com.tensiorr.budgetapp.data.dao.TransactionDao
import com.tensiorr.budgetapp.data.entity.Category
import com.tensiorr.budgetapp.data.entity.Tag
import com.tensiorr.budgetapp.data.entity.TransactionType
import kotlinx.coroutines.launch

/**
 * Settings screen for managing categories and tags.
 *
 * Features:
 * - List all categories grouped by type (Income/Expense)
 * - Edit category names
 * - Delete categories (with warning if transactions exist)
 * - Manage tags within categories (expandable lists)
 */
@Composable
fun ManageCategoriesScreen(
    categoryDao: CategoryDao,
    tagDao: TagDao,
    transactionDao: TransactionDao,
    categories: List<Category>,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val expenseCategories = remember(categories) {
        categories.filter { it.transactionType == TransactionType.EXPENSE }
    }

    val incomeCategories = remember(categories) {
        categories.filter { it.transactionType == TransactionType.INCOME }
    }

    var showAddCategoryDialog by remember { mutableStateOf<TransactionType?>(null) }
    var showDeleteCategoryDialog by remember { mutableStateOf<Pair<Category, Int>?>(null) }
    var showEditCategoryDialog by remember { mutableStateOf<Category?>(null) }

    var showAddTagDialog by remember { mutableStateOf<Category?>(null) }
    var showEditTagDialog by remember { mutableStateOf<Tag?>(null) }
    var showDeleteTagDialog by remember { mutableStateOf<Pair<Tag, Int>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                }
                Text(
                    text = "Zarządzaj kategoriami",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "KATEGORIE WYDATKÓW",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(expenseCategories) { category ->
                    CategorySettingsItem(
                        category = category,
                        tagDao = tagDao,
                        onEditCategory = { showEditCategoryDialog = category },
                        onDeleteCategory = {
                            scope.launch {
                                val count = categoryDao.getTransactionCountForCategory(category.id)
                                showDeleteCategoryDialog = Pair(category, count)
                            }
                        },
                        onAddTag = { showAddTagDialog = category },
                        onEditTag = { showEditTagDialog = it },
                        onDeleteTag = { tag ->
                            scope.launch {
                                val count = tagDao.getTransactionCountForTag(tag.id)
                                showDeleteTagDialog = Pair(tag, count)
                            }
                        }
                    )
                }

                item {
                    Text(
                        text = "KATEGORIE PRZYCHODÓW",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(incomeCategories) { category ->
                    CategorySettingsItem(
                        category = category,
                        tagDao = tagDao,
                        onEditCategory = { showEditCategoryDialog = category },
                        onDeleteCategory = {
                            scope.launch {
                                val count = categoryDao.getTransactionCountForCategory(category.id)
                                showDeleteCategoryDialog = Pair(category, count)
                            }
                        },
                        onAddTag = { showAddTagDialog = category },
                        onEditTag = { showEditTagDialog = it },
                        onDeleteTag = { tag ->
                            scope.launch {
                                val count = tagDao.getTransactionCountForTag(tag.id)
                                showDeleteTagDialog = Pair(tag, count)
                            }
                        }
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { showAddCategoryDialog = TransactionType.EXPENSE },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Dodaj kategorię")
        }
    }

    showEditCategoryDialog?.let { category ->
        if (category.id == 0L) {
            AddCategoryDialog(
                transactionType = category.transactionType,
                categoryDao = categoryDao,
                onDismiss = { showEditCategoryDialog = null },
                onConfirm = { newName ->
                    scope.launch {
                        categoryDao.insert(
                            Category(
                                name = newName.trim(),
                                transactionType = category.transactionType
                            )
                        )
                        showEditCategoryDialog = null
                    }
                }
            )
        } else {
            EditCategoryDialog(
                category = category,
                onDismiss = { showEditCategoryDialog = null },
                onConfirm = { newName ->
                    scope.launch {
                        categoryDao.update(category.copy(name = newName.trim()))
                        showEditCategoryDialog = null
                    }
                }
            )
        }
    }

    showDeleteCategoryDialog?.let { (category, transactionCount) ->
        DeleteCategoryDialog(
            category = category,
            transactionCount = transactionCount,
            onDismiss = { showDeleteCategoryDialog = null },
            onConfirm = {
                scope.launch {
                    categoryDao.delete(category)
                    showDeleteCategoryDialog = null
                }
            }
        )
    }

    showAddTagDialog?.let { category ->
        AddTagDialog(
            category = category,
            tagDao = tagDao,
            onDismiss = { showAddTagDialog = null },
            onConfirm = { tagName ->
                scope.launch {
                    tagDao.insertTag(
                        Tag(
                            name = tagName.trim(),
                            categoryId = category.id,
                            transactionType = category.transactionType
                        )
                    )
                    showAddTagDialog = null
                }
            }
        )
    }

    showEditTagDialog?.let { tag ->
        EditTagDialog(
            tag = tag,
            onDismiss = { showEditTagDialog = null },
            onConfirm = { newName ->
                scope.launch {
                    tagDao.updateTag(tag.copy(name = newName.trim()))
                    showEditTagDialog = null
                }
            }
        )
    }

    showDeleteTagDialog?.let { (tag, transactionCount) ->
        DeleteTagDialog(
            tag = tag,
            transactionCount = transactionCount,
            onDismiss = { showDeleteTagDialog = null },
            onConfirm = {
                scope.launch {
                    tagDao.deleteTag(tag)
                    showDeleteTagDialog = null
                }
            }
        )
    }
    showAddCategoryDialog?.let { preselectedType ->
        AddCategoryTypeDialog(
            onDismiss = { showAddCategoryDialog = null },
            onTypeSelected = { selectedType ->
                showAddCategoryDialog = null
                showEditCategoryDialog = Category(
                    id = 0,
                    name = "",
                    transactionType = selectedType
                )
            }
        )
    }
}

/**
 * Category item with expandable tag list.
 */
@Composable
fun CategorySettingsItem(
    category: Category,
    tagDao: TagDao,
    onEditCategory: () -> Unit,
    onDeleteCategory: () -> Unit,
    onAddTag: () -> Unit,
    onEditTag: (Tag) -> Unit,
    onDeleteTag: (Tag) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val tags = tagDao.getTagsForCategory(category.id).collectAsState(initial = emptyList())

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.ChevronRight,
                        contentDescription = if (expanded) "Zwiń" else "Rozwiń"
                    )
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "(${tags.value.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onEditCategory) {
                        Icon(Icons.Default.Edit, contentDescription = "Edytuj kategorię")
                    }
                    IconButton(onClick = onDeleteCategory) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Usuń kategorię",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (expanded) {
                HorizontalDivider()

                Column(modifier = Modifier.padding(start = 40.dp, end = 16.dp, bottom = 8.dp)) {
                    tags.value.forEach { tag ->
                        TagSettingsItem(
                            tag = tag,
                            onEdit = { onEditTag(tag) },
                            onDelete = { onDeleteTag(tag) }
                        )
                    }

                    TextButton(
                        onClick = onAddTag,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Dodaj tag")
                    }
                }
            }
        }
    }
}

/**
 * Individual tag item within category.
 */
@Composable
fun TagSettingsItem(
    tag: Tag,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "↳ ${tag.name}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edytuj tag",
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Usuń tag",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EditCategoryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(category.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj kategorię") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Nazwa kategorii") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank()
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun DeleteCategoryDialog(
    category: Category,
    transactionCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Usuń kategorię") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Czy na pewno chcesz usunąć kategorię \"${category.name}\"?")

                if (transactionCount > 0) {
                    Text(
                        text = "⚠️ Ta kategoria ma $transactionCount powiązanych transakcji. Transakcje zostaną zachowane, ale stracą powiązanie z kategorią.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = "Wszystkie tagi w tej kategorii również zostaną usunięte.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Usuń", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun AddTagDialog(
    category: Category,
    tagDao: TagDao,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var tagName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowy tag w \"${category.name}\"") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = {
                        tagName = it
                        errorMessage = null
                    },
                    label = { Text("Nazwa tagu") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        val existing = tagDao.getTagByNameAndCategory(tagName.trim(), category.id)
                        if (existing != null) {
                            errorMessage = "Tag o tej nazwie już istnieje w tej kategorii"
                        } else {
                            onConfirm(tagName)
                        }
                    }
                },
                enabled = tagName.isNotBlank()
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun EditTagDialog(
    tag: Tag,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(tag.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj tag") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Nazwa tagu") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank()
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun DeleteTagDialog(
    tag: Tag,
    transactionCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Usuń tag") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Czy na pewno chcesz usunąć tag \"${tag.name}\"?")

                if (transactionCount > 0) {
                    Text(
                        text = "⚠️ Ten tag jest przypisany do $transactionCount transakcji. Transakcje zostaną zachowane, ale stracą ten tag.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Usuń", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

/**
 * Dialog for selecting category type (Expense or Income) when adding new category.
 */
@Composable
fun AddCategoryTypeDialog(
    onDismiss: () -> Unit,
    onTypeSelected: (TransactionType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wybierz typ kategorii") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTypeSelected(TransactionType.EXPENSE) }
                ) {
                    Text(
                        text = "Wydatek",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTypeSelected(TransactionType.INCOME) }
                ) {
                    Text(
                        text = "Przychód",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

/**
 * Dialog for adding new category with duplicate checking.
 */
@Composable
fun AddCategoryDialog(
    transactionType: TransactionType,
    categoryDao: CategoryDao,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (transactionType == TransactionType.EXPENSE) {
                    "Nowa kategoria wydatków"
                } else {
                    "Nowa kategoria przychodów"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        errorMessage = null
                    },
                    label = { Text("Nazwa kategorii") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        val existing = categoryDao.getCategoryByNameAndType(
                            categoryName.trim(),
                            transactionType
                        )
                        if (existing != null) {
                            errorMessage = "Kategoria o tej nazwie już istnieje"
                        } else {
                            onConfirm(categoryName)
                        }
                    }
                },
                enabled = categoryName.isNotBlank()
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}