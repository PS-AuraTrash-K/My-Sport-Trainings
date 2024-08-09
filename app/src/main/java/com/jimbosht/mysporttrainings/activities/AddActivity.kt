package com.jimbosht.mysporttrainings.activities

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.jimbosht.mysporttrainings.R
import com.jimbosht.mysporttrainings.extra.ActivityViewModel
import com.jimbosht.mysporttrainings.extra.AlertDialogInit
import com.jimbosht.mysporttrainings.extra.DateHelper
import com.jimbosht.mysporttrainings.ui.theme.MySportTrainingsTheme
import kotlinx.coroutines.launch

class AddActivity : ComponentActivity() {
    private var globalViewModel: ActivityViewModel? = null

    private var activityName by mutableStateOf("")
    private var isAddingExtra by mutableStateOf(false)

    companion object {
        const val MAX_CHAR_LIMIT_IN_REPS = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        globalViewModel = ViewModelProvider(this)[ActivityViewModel::class.java]

        intent.extras.let {
            activityName = it?.getString(MainActivity.ACTIVITY_NAME).toString()
            isAddingExtra = it?.getBoolean(MainActivity.IS_ADDING_EXTRA) ?: false
        }


        setContent {
            MySportTrainingsTheme {
                AddActivityContent()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AddActivityContent() {
        var showDialog by remember { mutableStateOf(false) }
        var showSaveDialog by remember { mutableStateOf(false) }

        var finishing by remember { mutableStateOf(false) }

        val viewModel = globalViewModel!!

        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        if (isAddingExtra) {
                            Text(text = "Add Extra Date")
                        } else {
                            Text("Add Activity")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            showDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                    actions = {
                        ElevatedButton(onClick = {
                            viewModel.setSaving(true)

                            if (viewModel.isInvalid()) {
                                return@ElevatedButton
                            }

                            finishing = true
                            viewModel.addToDB(this@AddActivity)
                            showSaveDialog = true
                        }) {
                            Text("Add")
                        }
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp),
            ) {
                EditTextFields(
                    snackbarShow = {
                        scope.launch {
                            snackbarHostState
                                .showSnackbar(
                                    message = """
                                        Invalid date. Changed to empty field.
                                        """.trimIndent(),
                                    actionLabel = "OK",
                                    duration = SnackbarDuration.Long
                                )
                        }
                    },
                    finishing
                )
            }
        }

        AlertDialogInit(
            onConfirmation = {
                showSaveDialog = false

                val nav = Intent(this@AddActivity, MainActivity::class.java)
                nav.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(nav)
            },
            dialogTitle = "Congratulations!",
            dialogText = viewModel.toString(),
            icon = Icons.Default.CheckCircle,
            iconDescription = "Activity was successfully added!",
            showDialog = showSaveDialog,
            hasDecline = false
        )

        AlertDialogInit(
            onDismissRequest = { showDialog = false },
            onConfirmation = {
                showDialog = false

                val nav = Intent(this@AddActivity, MainActivity::class.java)
                nav.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(nav)
            },
            dialogTitle = "Warning!",
            dialogText = "All changes will not be saved!",
            icon = Icons.Default.Info,
            iconDescription = "Caution",
            showDialog = showDialog
        )
    }

    @Composable
    private fun EditTextFields(
        snackbarShow: () -> Unit,
        finishing: Boolean
    ) {
        val viewModel = globalViewModel!!

        val name by viewModel.name.observeAsState("")
        val date by viewModel.date.observeAsState(0L)
        val count by viewModel.count.observeAsState(1)
        val isSaving by viewModel.isSaving.observeAsState(false)
        var isInvoked by remember { mutableStateOf(false) }

        NameEditText(
            name = fun(): String {
                if (isAddingExtra) {
                    viewModel.setName(activityName)
                    isInvoked = true
                }
                return name
            }.invoke(),
            viewModel,
            isSaving,
            isInvoked = {
                isInvoked = it
            }
        )
        DateEditText(
            isSaving,
            viewModel,
            dateReset = {
                if (isInvoked && !finishing) {
                    if (viewModel.findDates(
                            name,
                            date,
                            this@AddActivity
                        )
                    ) {
                        snackbarShow()
                        viewModel.setDate(0L)
                        return@DateEditText ""
                    }
                }
                return@DateEditText it
            },
            additionalValidator = {
                if (isInvoked)
                    return@DateEditText !viewModel.findDates(name, it, this@AddActivity)
                return@DateEditText true
            }
        )

        Column(modifier = Modifier.padding(top = 24.dp)) {
            Text("Repetitions", color = MaterialTheme.colorScheme.secondary)
            Reps(count, isSaving)
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                var addEnabled by remember { mutableStateOf(true) }
                var deleteEnabled by remember { mutableStateOf(false) }

                FilledTonalIconButton(
                    onClick = {
                        viewModel.incrementCountBy(1)
                        addEnabled = count < 6
                        deleteEnabled = count > 1
                    },
                    modifier = Modifier.size(48.dp),
                    enabled = addEnabled
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Reps"
                    )
                }
                FilledTonalIconButton(
                    onClick = {
                        viewModel.incrementCountBy(-1)
                        addEnabled = count < 6
                        deleteEnabled = count > 1
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(48.dp),
                    enabled = deleteEnabled
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_remove_24),
                        contentDescription = "Remove Reps"
                    )
                }
            }
        }
    }

    @Composable
    private fun NameEditText(
        name: String,
        viewModel: ActivityViewModel,
        isSaving: Boolean,
        isInvoked: (Boolean) -> Unit
    ) {
        val invalidNames by viewModel.invalidNames.observeAsState(null)
        var additionalTextShow by remember {
            mutableStateOf(false)
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = {
                if (invalidNames == null)
                    viewModel.initInvalidNames(this@AddActivity)

                viewModel.setName(it)

                additionalTextShow = invalidNames?.any { invalid -> invalid == it }!!
                isInvoked.invoke(additionalTextShow)
            },
            label = { Text("Name") },
            isError = (isSaving and (viewModel.isNameEmpty())),
            supportingText = supportingTextGen(
                isSaving,
                viewModel.isNameEmpty(),
                additionalComposable = {
                    if (additionalTextShow && !isAddingExtra)
                        Text(text = "Activity already exists, only unused dates are available.")
                }
            ),

            enabled = !isAddingExtra
        )
    }

    @Composable
    private fun DateEditText(
        isSaving: Boolean,
        viewModel: ActivityViewModel,
        additionalValidator: (Long) -> Boolean,
        dateReset: (String) -> String
    ) {
        var showDatePicker by remember { mutableStateOf(false) }
        var bufferDate by remember { mutableStateOf("") }

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = fun(): String {
                    bufferDate = dateReset(bufferDate)
                    return bufferDate
                }.invoke(),
                readOnly = true,
                onValueChange = {
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date") },
                isError = isSaving and viewModel.isDateEmpty(),
                supportingText = supportingTextGen(isSaving, viewModel.isDateEmpty()),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = !showDatePicker }) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select date")
                    }
                }
            )

            if (showDatePicker) {
                DateHelper.DatePickerModal(
                    onDismiss = {
                        showDatePicker = false
                    },
                    onDateSelected = { nullMillis ->
                        val selectedMillis = nullMillis!!
                        val selectedDate = DateHelper.convertMillisToDate(selectedMillis)

                        bufferDate = selectedDate
                        viewModel.setDate(selectedMillis)
                    },
                    additionalValidator = additionalValidator
                )
            }
        }
    }

    private object RepsCount {
        fun getString(count: Int): String = when (count) {
            1 -> "First Rep"
            2 -> "Second Rep"
            3 -> "Third Rep"
            4 -> "Fourth Rep"
            5 -> "Fifth Rep"
            6 -> "Sixth Rep"
            else -> {
                ""
            }
        }
    }


    @Composable
    private fun Reps(count: Int, isSaving: Boolean) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (i in 1..if (count < 3) count else 3) {
                CreateButton(
                    Modifier.weight(1f),
                    i,
                    isSaving
                )
            }
        }
        if (count - 3 < 0) return
        Row(modifier = Modifier.fillMaxWidth()) {
            for (i in 4..count) {
                CreateButton(
                    Modifier.weight(1f),
                    i,
                    isSaving
                )
            }
        }
    }

    @Composable
    private fun CreateButton(
        modifier: Modifier,
        index: Int,
        isSaving: Boolean
    ) {
        var bufferedText by remember { mutableStateOf("") }
        var overload by remember { mutableStateOf(false) }
        val isEmpty = globalViewModel?.isIndexEmpty(index - 1) == true
        val isLast = globalViewModel?.isIndexLast(index - 1) == true
        val calcPadding = if (isLast) 0.dp else 8.dp

        OutlinedTextField(
            value = bufferedText,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
                if (it.any { char -> !char.isDigit() }) {
                    return@OutlinedTextField
                }

                if (it.length <= MAX_CHAR_LIMIT_IN_REPS + 1) {
                    overload = it.length > MAX_CHAR_LIMIT_IN_REPS
                    bufferedText = it
                    globalViewModel?.setListElem(index - 1, bufferedText)
                }
            },
            maxLines = 1,
            label = {
                Text(
                    RepsCount.getString(index),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            modifier = modifier.padding(end = calcPadding),
            isError = (isSaving and isEmpty) or overload,
            supportingText = {
                if (isSaving and isEmpty) {
                    Text("Empty")
                }

                Text(
                    text = "${bufferedText.length} / $MAX_CHAR_LIMIT_IN_REPS",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            },
        )
    }

    private fun supportingTextGen(
        isSaving: Boolean,
        isEmpty: Boolean,
        additionalComposable: @Composable () -> Unit = {}
    ): @Composable () -> Unit = {
        if (isSaving and isEmpty) {
            Text("Empty field")
        }
        additionalComposable()
    }

    @Preview(name = "Enabled Mode", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    private fun PreviewEnabled() {
        MySportTrainingsTheme {
            Surface {
                var bufferedText by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = bufferedText,
                    onValueChange = {
                        bufferedText = it
                    },
                    label = {
                        Text(
                            "Hi",
                        )
                    }
                )
            }
        }
    }
}