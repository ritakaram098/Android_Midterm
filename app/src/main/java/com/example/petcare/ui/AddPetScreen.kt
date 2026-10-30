package com.example.petcare.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    viewModel: PetViewModel,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Dog") }
    var breed by remember { mutableStateOf("Golden Retriever") }
    var sex by remember { mutableStateOf("Male") }
    var birthDate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Pet Types list for the selection grid (Core 6: Dog, Cat, Bird, Rabbit, Fish, Reptile)
    val petTypes = listOf("Dog", "Cat", "Bird", "Rabbit", "Fish", "Reptile")

    // Breed suggestions based on selected pet type
    val breedSuggestions = remember(type) {
        when (type) {
            "Dog" -> listOf(
                "Golden Retriever", "German Shepherd", "Labrador Retriever",
                "Poodle", "Bulldog", "Beagle", "Boxer", "Siberian Husky",
                "Chihuahua", "Dachshund", "Mixed / Unknown"
            )
            "Cat" -> listOf(
                "Persian", "Maine Coon", "Siamese", "Ragdoll",
                "British Shorthair", "Sphynx", "Bengal", "Tabby",
                "Domestic Shorthair", "Mixed / Unknown"
            )
            "Bird" -> listOf(
                "Parakeet / Budgie", "Cockatiel", "Canary",
                "Lovebird", "African Grey", "Mixed / Unknown"
            )
            "Rabbit" -> listOf(
                "Netherland Dwarf", "Holland Lop", "Lionhead",
                "Mini Rex", "Angora", "Mixed / Unknown"
            )
            "Fish" -> listOf(
                "Goldfish", "Betta", "Guppy", "Angelfish",
                "Tetra", "Mixed / Unknown"
            )
            "Reptile" -> listOf(
                "Bearded Dragon", "Leopard Gecko", "Ball Python",
                "Corn Snake", "Mixed / Unknown"
            )
            else -> listOf("Mixed / Unknown")
        }
    }

    var breedExpanded by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }
    val sexOptions = listOf("Male", "Female")

    // Calendar DatePicker state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            birthDate = formatter.format(Date(millis))
                        }
                    }
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.secondary)
                }
            },
            shape = RoundedCornerShape(16.dp)
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Add New Pet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Create a profile for your companion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Basic Information Section (Pet Name)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Pet Name *") },
                placeholder = { Text("e.g. Max, Luna") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Pet Type Selection Grid (Compact 3-column grid)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "What type of pet do you have?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Select one option below",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(2.dp))

                petTypes.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { option ->
                            Box(modifier = Modifier.weight(1f)) {
                                PetTypeCard(
                                    title = option,
                                    icon = Icons.Default.Pets,
                                    isSelected = (type == option),
                                    onClick = { type = option }
                                )
                            }
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Breed Field
            ExposedDropdownMenuBox(
                expanded = breedExpanded,
                onExpandedChange = { breedExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed *") },
                    trailingIcon = {
                        IconButton(onClick = { breedExpanded = !breedExpanded }) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedExpanded)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = breedExpanded,
                    onDismissRequest = { breedExpanded = false },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.heightIn(max = 220.dp)
                ) {
                    breedSuggestions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                breed = option
                                breedExpanded = false
                            }
                        )
                    }
                }
            }

            // Obligatory Fields: Date of Birth & Gender / Sex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            ) {
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Date of Birth *") },
                    placeholder = { Text("Select date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.secondary,
                        disabledLabelColor = MaterialTheme.colorScheme.secondary,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            ExposedDropdownMenuBox(
                expanded = sexExpanded,
                onExpandedChange = { sexExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = sex,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender / Sex *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = sexExpanded,
                    onDismissRequest = { sexExpanded = false },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    sexOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                sex = option
                                sexExpanded = false
                            }
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prominent Save Pet Profile Button
            Button(
                onClick = {
                    if (name.isBlank() || type.isBlank() || breed.isBlank() || sex.isBlank() || birthDate.isBlank()) {
                        errorMessage = "Please fill in all required fields (Name, Breed, Date of Birth, Gender)."
                    } else {
                        viewModel.addPet(
                            name = name,
                            type = type,
                            breed = breed,
                            sex = sex,
                            birthDate = birthDate,
                            weight = "",
                            notes = "",
                            photoUri = "",
                            onSuccess = {
                                onNavigateBack()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Save Pet Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PetTypeCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val drawableResId = remember(title) { getPetDrawableId(context, title) }

    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .height(86.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (drawableResId != 0) {
                    Image(
                        painter = painterResource(id = drawableResId),
                        contentDescription = title,
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            // Checkmark badge in top-right if selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

fun getPetDrawableId(context: Context, type: String): Int {
    val resName = when (type.lowercase().trim()) {
        "dog" -> "card_dog"
        "cat" -> "card_cat"
        "bird" -> "card_bird"
        "rabbit" -> "card_rabbit"
        "fish" -> "card_fish"
        "reptile" -> "card_reptile"
        else -> "card_other"
    }
    return context.resources.getIdentifier(resName, "drawable", context.packageName)
}
