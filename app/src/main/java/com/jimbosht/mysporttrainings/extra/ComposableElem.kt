package com.jimbosht.mysporttrainings.extra

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*


enum class CardType {
    Main,
    Date
}


@Composable
fun AlertDialogInit(
    onDismissRequest: () -> Unit = {},
    onConfirmation: () -> Unit = {},
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
    iconDescription: String,
    showDialog: Boolean,
    hasDecline: Boolean = true
) {
    if (showDialog) {
        AlertDialog(
            icon = {
                Icon(imageVector = icon, contentDescription = iconDescription)
            },
            title = {
                Text(text = dialogTitle)
            },
            text = {
                Text(text = dialogText)
            },
            onDismissRequest = {
                if (onDismissRequest == {}) onConfirmation()
                else onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                if (hasDecline) {
                    TextButton(
                        onClick = {
                            onDismissRequest()
                        }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        )
    }
}

@Composable
fun FilledTonalButtonWithIcon(
    onClick: () -> Unit = {},
    imageVector: ImageVector,
    contentDescription: String,
    text: String
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.offset(x = -8.dp)
        )

        Text(
            text = text,
        )
    }
}

class SampleMainProvider : PreviewParameterProvider<ActivityDataClass> {
    override val values: Sequence<ActivityDataClass> = sequenceOf(
        ActivityDataClass(
            name = "Squads",
            date = 1722902400000L,
            reps = listOf(
                "4", "5", "6", "256"
            )
        ),
        ActivityDataClass(
            name = "Pushups",
            date = 1722729600000L,
            reps = listOf(
                "1"
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityItem(
    headline: String = "",
    descriptionComposable: @Composable () -> Unit = { },
    fullDescriptionComposable: @Composable () -> Unit = { },
    additionalCentered: Boolean = true,
    isClicked: Boolean,
    onCardClick: () -> Unit,
) {

    ElevatedCard(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.headlineLarge,
            )

            descriptionComposable()
        }

        if (isClicked) {
            Row(
                horizontalArrangement =
                if (additionalCentered)
                    Arrangement.Center
                else
                    Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 4.dp),
            ) {
                fullDescriptionComposable()
            }
        }
    }
}

fun repsListToString(reps: List<String>): String {
    var result = ""

    for (index in reps.indices) {
        if (index != 0) result += "/"
        result += reps[index]
    }

    return result
}

@Composable
fun CustomSnackbar(
    showSnackbar: Boolean
) {
    if (showSnackbar) {
        Snackbar(

        ) {

        }
    }
}


object DateHelper {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DatePickerModal(
        onDateSelected: (Long?) -> Unit,
        onDismiss: () -> Unit,
        additionalValidator: (Long) -> Boolean = { true }
    ) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                dateValidator = {
                    val selected = System.currentTimeMillis()
                    return@DatePicker it <= selected && additionalValidator.invoke(it)
                }
            )
        }
    }

    fun convertMillisToDate(millis: Long, pattern: String = "dd.MM.yyyy"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(Date(millis))
    }
}