package com.kurakulas.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.shape.CircleShape
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.kurakulas.app.ui.viewmodel.MainPanelViewModel
import pzn.AddAppointmentPanel
import com.kurakulas.app.ui.viewmodel.DsaCodeViewModel
import com.kurakulas.app.ui.viewmodel.BankerViewModel
import com.kurakulas.app.ui.model.DsaCodeData
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.content.ContentResolver
import android.content.Context
import java.io.FileOutputStream
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import org.json.JSONObject
import java.net.URL
import com.kurakulas.app.data.api.ApiConfig
import android.util.Log
import kotlinx.coroutines.delay
import androidx.compose.ui.res.painterResource
import com.kurakulas.app.R
import kotlinx.coroutines.CoroutineScope
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration

// Helper function to get file name
fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
    val cursor = contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        it.getString(nameIndex)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun MainPanelScreen(
    onMenuClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onNavigateToAddAppointment: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: MainPanelViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddAppointment by remember { mutableStateOf(false) }
    var showMyAccount by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showSalAppointmentPanel by remember { mutableStateOf(false) }
    var showSenpAppointmentPanel by remember { mutableStateOf(false) }
    var showSepAppointmentPanel by remember { mutableStateOf(false) }
    var showNriAppointmentPanel by remember { mutableStateOf(false) }
    var showEducationalAppointmentPanel by remember { mutableStateOf(false) }
    var showTeamSalAppointmentPanel by remember { mutableStateOf(false) }
    var showTeamSenpAppointmentPanel by remember { mutableStateOf(false) }
    var showTeamSepAppointmentPanel by remember { mutableStateOf(false) }
    var showTeamNriAppointmentPanel by remember { mutableStateOf(false) }
    var showTeamEducationalAppointmentPanel by remember { mutableStateOf(false) }
    var showAddAgentPanel by remember { mutableStateOf(false) }
    var showMyAgentPanel by remember { mutableStateOf(false) }
    var showDsaCodePanel by remember { mutableStateOf(false) }
    var showBankersPanel by remember { mutableStateOf(false) }

    // Get points from ViewModel
    val salPoints by viewModel.salPoints.collectAsState()
    val senpPoints by viewModel.senpPoints.collectAsState()
    val sepPoints by viewModel.sepPoints.collectAsState()
    val nriPoints by viewModel.nriPoints.collectAsState()
    val educationalPoints by viewModel.educationalPoints.collectAsState()
    
    // Add state variables for team appointment points
    var teamSalPoints by remember { mutableStateOf(0) }
    var teamSenpPoints by remember { mutableStateOf(0) }
    var teamSepPoints by remember { mutableStateOf(0) }
    var teamNriPoints by remember { mutableStateOf(0) }
    var teamEducationalPoints by remember { mutableStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { 
                            scope.launch {
                                if (drawerState.isOpen) {
                                    drawerState.close()
                                } else {
                                    drawerState.open()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                        
                        Text(
                            text = "Kurakulas",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Row {
                            IconButton(onClick = { showHelp = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Help")
                            }
                            IconButton(onClick = { showMyAccount = true }) {
                                Icon(Icons.Default.Person, contentDescription = "My Account")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Appointments") },
                    label = { Text("Appointments") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Files") },
                    label = { Text("Files") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.MoreVert, contentDescription = "More") },
                    label = { Text("More") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Image Slider
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.White)
                    ) {
                        val pagerState = rememberPagerState()
                        val coroutineScope = rememberCoroutineScope()
                        
                        // Auto-sliding effect
                        LaunchedEffect(Unit) {
                            while (true) {
                                delay(2000) // 2 seconds delay
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        page = (pagerState.currentPage + 1) % 4
                                    )
                                }
                            }
                        }
                        
                        HorizontalPager(
                            count = 4,
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White)
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = when (page) {
                                            0 -> R.drawable.kurakulasqualitylogoremovebgpreview
                                            1 -> R.drawable.kfinelogooneremovebgpreview
                                            2 -> R.drawable.logokinfomediafinalremovebgpreview
                                            else -> R.drawable.logintoloans_removebg_preview
                                        }
                                    ),
                                    contentDescription = "Carousel Image ${page + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        
                        HorizontalPagerIndicator(
                            pagerState = pagerState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        )
                    }
                }

                // Welcome Box
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Welcome to Kurakulas",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val firstName by viewModel.userFirstName.collectAsState()
                            val lastName by viewModel.userLastName.collectAsState()
                            Text(
                                text = if (firstName != null && lastName != null) {
                                    "$firstName $lastName"
                                } else {
                                    "User"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                // Add Statistics Section here
                item {
                    StatisticsSection(
                        salPoints = salPoints,
                        senpPoints = senpPoints,
                        sepPoints = sepPoints,
                        nriPoints = nriPoints,
                        educationalPoints = educationalPoints,
                        teamSalPoints = teamSalPoints,
                        teamSenpPoints = teamSenpPoints,
                        teamSepPoints = teamSepPoints,
                        teamNriPoints = teamNriPoints,
                        teamEducationalPoints = teamEducationalPoints
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // Appointments Section
                        item {
                            if (selectedTab == 0) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Appointments",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        items(appointments) { appointment ->
                            AppointmentCard(
                                appointment = appointment,
                                onAddAppointmentClick = { showAddAppointment = true },
                                onSalAppointmentClick = { showSalAppointmentPanel = true },
                                onSenpAppointmentClick = { showSenpAppointmentPanel = true },
                                onSepAppointmentClick = { showSepAppointmentPanel = true },
                                onNriAppointmentClick = { showNriAppointmentPanel = true },
                                onEducationalAppointmentClick = { showEducationalAppointmentPanel = true },
                                onTeamSalAppointmentClick = { showTeamSalAppointmentPanel = true },
                                onTeamSenpAppointmentClick = { showTeamSenpAppointmentPanel = true },
                                onTeamSepAppointmentClick = { showTeamSepAppointmentPanel = true },
                                onTeamNriAppointmentClick = { showTeamNriAppointmentPanel = true },
                                onTeamEducationalAppointmentClick = { showTeamEducationalAppointmentPanel = true },
                                salPoints = salPoints,
                                senpPoints = senpPoints,
                                sepPoints = sepPoints,
                                nriPoints = nriPoints,
                                educationalPoints = educationalPoints,
                                teamSalPoints = teamSalPoints,
                                teamSenpPoints = teamSenpPoints,
                                teamSepPoints = teamSepPoints,
                                teamNriPoints = teamNriPoints,
                                teamEducationalPoints = teamEducationalPoints
                            )
                        }
                    }
                    1 -> {
                        // Files Section
                        item {
                            Text(
                                text = "Files",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        item {
                            FileUploadCard()
                        }
                    }
                    2 -> {
                        // More Section
                        item {
                            Text(
                                text = "More Options",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        item {
                            MoreOptionCard(
                                title = "DSA Code",
                                onClick = { showDsaCodePanel = true }
                            )
                        }
                        item {
                            MoreOptionCard(
                                title = "Bankers",
                                onClick = { showBankersPanel = true }
                            )
                        }
                    }
                }
            }

            if (showSalAppointmentPanel) {
                SalAppointmentPanel(
                    onDismiss = { showSalAppointmentPanel = false }
                )
            }

            if (showSenpAppointmentPanel) {
                SenpAppointmentPanel(
                    onDismiss = { showSenpAppointmentPanel = false }
                )
            }

            if (showSepAppointmentPanel) {
                SepAppointmentPanel(
                    onDismiss = { showSepAppointmentPanel = false }
                )
            }

            if (showNriAppointmentPanel) {
                NriAppointmentPanel(onDismiss = { showNriAppointmentPanel = false })
            }

            if (showEducationalAppointmentPanel) {
                EducationalAppointmentPanel(onDismiss = { showEducationalAppointmentPanel = false })
            }

            if (showTeamSalAppointmentPanel) {
                TeamSalAppointmentPanel(onDismiss = { showTeamSalAppointmentPanel = false })
            }

            if (showTeamSenpAppointmentPanel) {
                TeamSenpAppointmentPanel(onDismiss = { showTeamSenpAppointmentPanel = false })
            }

            if (showTeamSepAppointmentPanel) {
                TeamSepAppointmentPanel(onDismiss = { showTeamSepAppointmentPanel = false })
            }

            if (showTeamNriAppointmentPanel) {
                TeamNriAppointmentPanel(onDismiss = { showTeamNriAppointmentPanel = false })
            }

            if (showTeamEducationalAppointmentPanel) {
                TeamEducationalAppointmentPanel(onDismiss = { showTeamEducationalAppointmentPanel = false })
            }

            if (showAddAgentPanel) {
                AddAgentPanel(onDismiss = { showAddAgentPanel = false })
            }

            if (showMyAgentPanel) {
                MyAgentPanel(onDismiss = { showMyAgentPanel = false })
            }

            if (showDsaCodePanel) {
                DsaCodePanel(
                    onDismiss = { showDsaCodePanel = false },
                    viewModel = hiltViewModel()
                )
            }

            if (showBankersPanel) {
                BankersPanel(onDismiss = { showBankersPanel = false })
            }

            // Drawer
            if (drawerState.isOpen) {
                // Semi-transparent overlay to handle outside clicks
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable {
                            scope.launch {
                                drawerState.close()
                            }
                        }
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(300.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        DrawerHeader(viewModel)
                        
                        // Main Menu Section
                        Text(
                            "Main Menu",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        DrawerItem(
                            icon = Icons.Default.Dashboard,
                            text = "Dashboard",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                        )
                        
                        DrawerItem(
                            icon = Icons.Default.Person,
                            text = "Profile",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                        )
                        
                        // Settings Section
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        DrawerItem(
                            icon = Icons.Default.Settings,
                            text = "Settings",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                        )
                        
                        DrawerItem(
                            icon = Icons.Default.Help,
                            text = "Help & Support",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                        )
                        
                        DrawerItem(
                            icon = Icons.Default.Info,
                            text = "About",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Logout Section
                        Divider()
                        DrawerItem(
                            icon = Icons.Default.ExitToApp,
                            text = "Logout",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    viewModel.logout()
                                    onLogout()
                                }
                            }
                        )
                    }
                }
            }

            // Show Add Appointment Panel
            if (showAddAppointment) {
                AddAppointmentPanel(
                    onNavigateBack = { showAddAppointment = false },
                    context = LocalContext.current,
                    onPointsIncrement = { type ->
                        viewModel.incrementPoints(type)
                    }
                )
            }

            // Show My Account Panel
            if (showMyAccount) {
                MyAccountPanel(
                    onNavigateBack = { showMyAccount = false },
                    onLogout = onLogout,
                    onNavigateToProfile = { 
                        showMyAccount = false
                        showProfile = true
                    },
                    viewModel = viewModel
                )
            }

            // Show Profile Screen
            if (showProfile) {
                ProfileScreen(
                    onNavigateBack = { showProfile = false }
                )
            }

            // Show Help Panel
            if (showHelp) {
                HelpPanel(
                    onDismiss = { showHelp = false }
                )
            }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
            .animateContentSize(),
        color = if (isHovered) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DrawerHeader(viewModel: MainPanelViewModel) {
    val firstName by viewModel.userFirstName.collectAsState()
    val lastName by viewModel.userLastName.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 12.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Column {
                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = if (firstName != null && lastName != null) {
                        "$firstName $lastName"
                    } else {
                        "User"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
    }
}

@Composable
fun AppointmentCard(
    appointment: String,
    onAddAppointmentClick: () -> Unit,
    onSalAppointmentClick: () -> Unit,
    onSenpAppointmentClick: () -> Unit,
    onSepAppointmentClick: () -> Unit,
    onNriAppointmentClick: () -> Unit,
    onEducationalAppointmentClick: () -> Unit,
    onTeamSalAppointmentClick: () -> Unit,
    onTeamSenpAppointmentClick: () -> Unit,
    onTeamSepAppointmentClick: () -> Unit,
    onTeamNriAppointmentClick: () -> Unit,
    onTeamEducationalAppointmentClick: () -> Unit,
    salPoints: Int = 0,
    senpPoints: Int = 0,
    sepPoints: Int = 0,
    nriPoints: Int = 0,
    educationalPoints: Int = 0,
    teamSalPoints: Int = 0,
    teamSenpPoints: Int = 0,
    teamSepPoints: Int = 0,
    teamNriPoints: Int = 0,
    teamEducationalPoints: Int = 0
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { 
                when (appointment) {
                    "Add Appointment" -> onAddAppointmentClick()
                    "Salaried-SAL" -> onSalAppointmentClick()
                    "Self Employed Non Professionals-SENP" -> onSenpAppointmentClick()
                    "Self Employed Professionals-SEP" -> onSepAppointmentClick()
                    "NRI" -> onNriAppointmentClick()
                    "Educational" -> onEducationalAppointmentClick()
                    "Team SAL Appointment (Salaried)" -> onTeamSalAppointmentClick()
                    "Team SENP Appointment (Self Employed Non Professionals)" -> onTeamSenpAppointmentClick()
                    "Team SEP Appointment (Self Employed Professionals)" -> onTeamSepAppointmentClick()
                    "Team NRI Appointment (Non-Resident Indian)" -> onTeamNriAppointmentClick()
                    "Team Educational Appointment (Educational)" -> onTeamEducationalAppointmentClick()
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text(appointment, fontSize = 16.sp)
            }
            if (appointment != "Add Appointment") {
                val points = when (appointment) {
                    "Salaried-SAL" -> salPoints
                    "Self Employed Non Professionals-SENP" -> senpPoints
                    "Self Employed Professionals-SEP" -> sepPoints
                    "NRI" -> nriPoints
                    "Educational" -> educationalPoints
                    "Team SAL Appointment (Salaried)" -> teamSalPoints
                    "Team SENP Appointment (Self Employed Non Professionals)" -> teamSenpPoints
                    "Team SEP Appointment (Self Employed Professionals)" -> teamSepPoints
                    "Team NRI Appointment (Non-Resident Indian)" -> teamNriPoints
                    "Team Educational Appointment (Educational)" -> teamEducationalPoints
                    else -> 0
                }
                Text(points.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileUploadCard() {
    var selectedFileType by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val fileTypes = listOf("PNG", "JPEG", "PDF")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Choose File",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedFileType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("File Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .width(120.dp)
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        fileTypes.forEach { fileType ->
                            DropdownMenuItem(
                                text = { Text(fileType) },
                                onClick = {
                                    selectedFileType = fileType
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Text(
                text = "Coming Soon",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AgentCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 16.sp)
        }
    }
}

@Composable
fun MoreOptionCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalAppointmentPanel(onDismiss: () -> Unit) {
    var mobileNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var appointments by remember { mutableStateOf<List<AppointmentData>>(emptyList()) }
    val context = LocalContext.current

    // Function to fetch appointments
    fun fetchAppointments() {
        if (mobileNumber.length != 10) {
            Log.d("SalAppointmentPanel", "Invalid mobile number length: ${mobileNumber.length}")
            Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("SalAppointmentPanel", "Starting appointment fetch for mobile: $mobileNumber")
        isLoading = true
        error = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://pznstudio.shop/get_sal_appointments.php")
                Log.d("SalAppointmentPanel", "Connecting to URL: $url")
                
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val postData = "mobile_number=$mobileNumber"
                Log.d("SalAppointmentPanel", "Sending POST data: $postData")
                
                connection.outputStream.use { os ->
                    os.write(postData.toByteArray())
                    os.flush()
                }

                val responseCode = connection.responseCode
                Log.d("SalAppointmentPanel", "Response code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("SalAppointmentPanel", "Raw response: $response")
                    
                    val jsonResponse = JSONObject(response)
                    
                    if (jsonResponse.getBoolean("success")) {
                        val appointmentsArray = jsonResponse.getJSONArray("appointments")
                        Log.d("SalAppointmentPanel", "Number of appointments found: ${appointmentsArray.length()}")
                        
                        val appointmentsList = mutableListOf<AppointmentData>()
                        
                        for (i in 0 until appointmentsArray.length()) {
                            val appointment = appointmentsArray.getJSONObject(i)
                            Log.d("SalAppointmentPanel", "Processing appointment $i: ${appointment.toString()}")
                            
                            appointmentsList.add(
                                AppointmentData(
                                    mobileNumber = appointment.optString("mobile_number", ""),
                                    leadName = appointment.optString("lead_name", ""),
                                    emailId = appointment.optString("email_id", ""),
                                    createdBy = appointment.optString("createdBy", ""),
                                    alternativeMobile = appointment.optString("alternative_mobile", ""),
                                    state = appointment.optString("state", ""),
                                    location = appointment.optString("location", ""),
                                    subLocation = appointment.optString("sub_location", ""),
                                    pinCode = appointment.optString("pin_code", ""),
                                    source = appointment.optString("source", ""),
                                    qualification = appointment.optString("user_qualification", ""),
                                    address = appointment.optString("residental_address", ""),
                                    customerType = appointment.optString("customer_type_name", "")
                                )
                            )
                        }
                        
                        withContext(Dispatchers.Main) {
                            appointments = appointmentsList
                            Log.d("SalAppointmentPanel", "Updated appointments list with ${appointmentsList.size} items")
                        }
                    } else {
                        val errorMessage = jsonResponse.getString("message")
                        Log.e("SalAppointmentPanel", "API error: $errorMessage")
                        withContext(Dispatchers.Main) {
                            error = errorMessage
                        }
                    }
                } else {
                    val errorMessage = "Server error: $responseCode"
                    Log.e("SalAppointmentPanel", errorMessage)
                    withContext(Dispatchers.Main) {
                        error = errorMessage
                    }
                }
            } catch (e: Exception) {
                Log.e("SalAppointmentPanel", "Exception occurred", e)
                withContext(Dispatchers.Main) {
                    error = e.message ?: "Unknown error occurred"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Log.d("SalAppointmentPanel", "Finished loading appointments")
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Appointment Salaried List",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Search Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Search Appointments",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { if (it.length <= 10) mobileNumber = it },
                        label = { Text("Enter Mobile Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { 
                                mobileNumber = ""
                                appointments = emptyList()
                                error = null
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = { fetchAppointments() }
                        ) {
                            Text("Search")
                        }
                    }
                }
            }

            // Results Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Appointment Results",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else if (appointments.isEmpty()) {
                        Text(
                            text = "No appointments found",
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lead Name",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Mobile",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Email",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Location",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Table Content
                        appointments.forEach { appointment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = appointment.leadName,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = appointment.mobileNumber,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = appointment.emailId,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${appointment.location}, ${appointment.state}",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    
                                    // Additional Details
                                    Text(
                                        text = "Alternative Mobile: ${appointment.alternativeMobile}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Qualification: ${appointment.qualification}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Address: ${appointment.address}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Created By: ${appointment.createdBy}",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Customer Type: ${appointment.customerType}",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenpAppointmentPanel(onDismiss: () -> Unit) {
    var mobileNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var appointments by remember { mutableStateOf<List<AppointmentData>>(emptyList()) }
    val context = LocalContext.current

    // Function to fetch appointments
    fun fetchAppointments() {
        if (mobileNumber.length != 10) {
            Log.d("SenpAppointmentPanel", "Invalid mobile number length: ${mobileNumber.length}")
            Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("SenpAppointmentPanel", "Starting appointment fetch for mobile: $mobileNumber")
        isLoading = true
        error = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${ApiConfig.BASE_URL}get_senp_appointments.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val postData = "mobile_number=$mobileNumber"
                connection.outputStream.use { os ->
                    os.write(postData.toByteArray())
                    os.flush()
                }

                Log.d("SenpAppointmentPanel", "Response code: ${connection.responseCode}")
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("SenpAppointmentPanel", "Response: $response")

                withContext(Dispatchers.Main) {
                    try {
                        val jsonResponse = JSONObject(response)
                        if (jsonResponse.getBoolean("success")) {
                            val appointmentsArray = jsonResponse.getJSONArray("appointments")
                            val appointmentsList = mutableListOf<AppointmentData>()
                            
                            for (i in 0 until appointmentsArray.length()) {
                                val appointment = appointmentsArray.getJSONObject(i)
                                appointmentsList.add(
                                    AppointmentData(
                                        mobileNumber = appointment.optString("mobile_number", ""),
                                        leadName = appointment.optString("lead_name", ""),
                                        emailId = appointment.optString("email_id", ""),
                                        createdBy = appointment.optString("createdBy", ""),
                                        alternativeMobile = appointment.optString("alternative_mobile", ""),
                                        state = appointment.optString("state", ""),
                                        location = appointment.optString("location", ""),
                                        subLocation = appointment.optString("sub_location", ""),
                                        pinCode = appointment.optString("pin_code", ""),
                                        source = appointment.optString("source", ""),
                                        qualification = appointment.optString("qualification", ""),
                                        address = appointment.optString("address", ""),
                                        customerType = appointment.optString("customer_type_name", "")
                                    )
                                )
                            }
                            appointments = appointmentsList
                            error = null
                        } else {
                            error = jsonResponse.getString("message")
                        }
                    } catch (e: Exception) {
                        Log.e("SenpAppointmentPanel", "Error parsing response", e)
                        error = "Error parsing response: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                Log.e("SenpAppointmentPanel", "Error fetching appointments", e)
                withContext(Dispatchers.Main) {
                    error = "Error fetching appointments: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Appointment SENP List",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Search Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { 
                            if (it.length <= 10) {
                                mobileNumber = it
                            }
                        },
                        label = { Text("Mobile Number") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { fetchAppointments() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { fetchAppointments() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Filter")
                        }
                    }
                }
            }

            // Error Message
            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Loading Indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Results Section
            if (appointments.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Results",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        appointments.forEach { appointment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Basic Details
                                    Text(
                                        text = "Name: ${appointment.leadName}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Mobile: ${appointment.mobileNumber}",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Email: ${appointment.emailId}",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Created By: ${appointment.createdBy}",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Customer Type: ${appointment.customerType}",
                                        fontSize = 14.sp
                                    )
                                    
                                    // Additional Details
                                    Text(
                                        text = "Alternative Mobile: ${appointment.alternativeMobile}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Qualification: ${appointment.qualification}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Address: ${appointment.address}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SepAppointmentPanel(onDismiss: () -> Unit) {
    var mobileNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var appointments by remember { mutableStateOf<List<AppointmentData>>(emptyList()) }
    val context = LocalContext.current

    // Function to fetch appointments
    fun fetchAppointments() {
        if (mobileNumber.length != 10) {
            Log.d("SepAppointmentPanel", "Invalid mobile number length: ${mobileNumber.length}")
            Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("SepAppointmentPanel", "Starting appointment fetch for mobile: $mobileNumber")
        isLoading = true
        error = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${ApiConfig.BASE_URL}get_sep_appointments.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val postData = "mobile_number=$mobileNumber"
                connection.outputStream.use { os ->
                    os.write(postData.toByteArray())
                    os.flush()
                }

                Log.d("SepAppointmentPanel", "Response code: ${connection.responseCode}")
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("SepAppointmentPanel", "Response: $response")

                withContext(Dispatchers.Main) {
                    try {
                        val jsonResponse = JSONObject(response)
                        if (jsonResponse.getBoolean("success")) {
                            val appointmentsArray = jsonResponse.getJSONArray("appointments")
                            val appointmentsList = mutableListOf<AppointmentData>()
                            
                            for (i in 0 until appointmentsArray.length()) {
                                val appointment = appointmentsArray.getJSONObject(i)
                                appointmentsList.add(
                                    AppointmentData(
                                        mobileNumber = appointment.optString("mobile_number", ""),
                                        leadName = appointment.optString("lead_name", ""),
                                        emailId = appointment.optString("email_id", ""),
                                        createdBy = appointment.optString("createdBy", ""),
                                        alternativeMobile = appointment.optString("alternative_mobile", ""),
                                        state = appointment.optString("state", ""),
                                        location = appointment.optString("location", ""),
                                        subLocation = appointment.optString("sub_location", ""),
                                        pinCode = appointment.optString("pin_code", ""),
                                        source = appointment.optString("source", ""),
                                        qualification = appointment.optString("user_qualification", ""),
                                        address = appointment.optString("residental_address", ""),
                                        customerType = appointment.optString("customer_type_name", "")
                                    )
                                )
                            }
                            appointments = appointmentsList
                            error = null
                        } else {
                            error = jsonResponse.getString("message")
                        }
                    } catch (e: Exception) {
                        Log.e("SepAppointmentPanel", "Error parsing response", e)
                        error = "Error parsing response: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                Log.e("SepAppointmentPanel", "Error fetching appointments", e)
                withContext(Dispatchers.Main) {
                    error = "Error fetching appointments: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Appointment SEP List",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Search Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { 
                            if (it.length <= 10) {
                                mobileNumber = it
                            }
                        },
                        label = { Text("Mobile Number") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { fetchAppointments() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { 
                                mobileNumber = ""
                                appointments = emptyList()
                                error = null
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = { fetchAppointments() },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Filter")
                            }
                        }
                    }
                }
            }

            // Error Message
            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Loading Indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Results Section
            if (appointments.isNotEmpty()) {
                Text(
                    text = "Results",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                appointments.forEach { appointment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Basic Details
                            Text(
                                text = "Name: ${appointment.leadName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Mobile: ${appointment.mobileNumber}",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Email: ${appointment.emailId}",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Created By: ${appointment.createdBy}",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Customer Type: ${appointment.customerType}",
                                fontSize = 14.sp
                            )
                            
                            // Additional Details
                            Text(
                                text = "Alternative Mobile: ${appointment.alternativeMobile}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Qualification: ${appointment.qualification}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Address: ${appointment.address}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NriAppointmentPanel(onDismiss: () -> Unit) {
    var mobileNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var appointments by remember { mutableStateOf<List<AppointmentData>>(emptyList()) }
    val context = LocalContext.current

    // Function to fetch appointments
    fun fetchAppointments() {
        if (mobileNumber.length != 10) {
            Log.d("NriAppointmentPanel", "Invalid mobile number length: ${mobileNumber.length}")
            Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("NriAppointmentPanel", "Starting appointment fetch for mobile: $mobileNumber")
        isLoading = true
        error = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${ApiConfig.BASE_URL}get_nri_appointments.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val postData = "mobile_number=$mobileNumber"
                connection.outputStream.use { os ->
                    os.write(postData.toByteArray())
                    os.flush()
                }

                Log.d("NriAppointmentPanel", "Response code: ${connection.responseCode}")
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("NriAppointmentPanel", "Response: $response")

                withContext(Dispatchers.Main) {
                    try {
                        val jsonResponse = JSONObject(response)
                        if (jsonResponse.getBoolean("success")) {
                            val appointmentsArray = jsonResponse.getJSONArray("appointments")
                            val appointmentsList = mutableListOf<AppointmentData>()
                            
                            for (i in 0 until appointmentsArray.length()) {
                                val appointment = appointmentsArray.getJSONObject(i)
                                appointmentsList.add(
                                    AppointmentData(
                                        mobileNumber = appointment.optString("mobile_number", ""),
                                        leadName = appointment.optString("lead_name", ""),
                                        emailId = appointment.optString("email_id", ""),
                                        createdBy = appointment.optString("createdBy", ""),
                                        alternativeMobile = appointment.optString("alternative_mobile", ""),
                                        state = appointment.optString("state", ""),
                                        location = appointment.optString("location", ""),
                                        subLocation = appointment.optString("sub_location", ""),
                                        pinCode = appointment.optString("pin_code", ""),
                                        source = appointment.optString("source", ""),
                                        qualification = appointment.optString("user_qualification", ""),
                                        address = appointment.optString("residental_address", ""),
                                        customerType = appointment.optString("customer_type_name", "")
                                    )
                                )
                            }
                            appointments = appointmentsList
                            error = null
                        } else {
                            error = jsonResponse.getString("message")
                        }
                    } catch (e: Exception) {
                        Log.e("NriAppointmentPanel", "Error parsing response", e)
                        error = "Error parsing response: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                Log.e("NriAppointmentPanel", "Error fetching appointments", e)
                withContext(Dispatchers.Main) {
                    error = "Error fetching appointments: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Database NRI List",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Search Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { 
                            if (it.length <= 10) {
                                mobileNumber = it
                            }
                        },
                        label = { Text("Mobile Number") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { fetchAppointments() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { 
                                mobileNumber = ""
                                appointments = emptyList()
                                error = null
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = { fetchAppointments() },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Filter")
                            }
                        }
                    }
                }
            }

            // Error Message
            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Loading Indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Results Section
            if (appointments.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Lead Name",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "Mobile",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "Email",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "Location",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Table Content
                        appointments.forEach { appointment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = appointment.leadName,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = appointment.mobileNumber,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = appointment.emailId,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${appointment.location}, ${appointment.state}",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    
                                    // Additional Details
                                    Text(
                                        text = "Alternative Mobile: ${appointment.alternativeMobile}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Qualification: ${appointment.qualification}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Address: ${appointment.address}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Created By: ${appointment.createdBy}",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Customer Type: ${appointment.customerType}",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationalAppointmentPanel(onDismiss: () -> Unit) {
    var mobileNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var appointments by remember { mutableStateOf<List<AppointmentData>>(emptyList()) }
    val context = LocalContext.current

    // Function to fetch appointments
    fun fetchAppointments() {
        if (mobileNumber.length != 10) {
            Log.d("EducationalAppointmentPanel", "Invalid mobile number length: ${mobileNumber.length}")
            Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("EducationalAppointmentPanel", "Starting appointment fetch for mobile: $mobileNumber")
        isLoading = true
        error = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://pznstudio.shop/get_educational_appointments.php"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = true

                val postData = "mobile_number=$mobileNumber"
                connection.outputStream.use { os ->
                    os.write(postData.toByteArray())
                    os.flush()
                }

                val responseCode = connection.responseCode
                Log.d("EducationalAppointmentPanel", "Response code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("EducationalAppointmentPanel", "Response: $response")

                    try {
                        val jsonResponse = JSONObject(response)
                        if (jsonResponse.getBoolean("success")) {
                            val appointmentsArray = jsonResponse.getJSONArray("appointments")
                            val appointmentsList = mutableListOf<AppointmentData>()

                            for (i in 0 until appointmentsArray.length()) {
                                val appointment = appointmentsArray.getJSONObject(i)
                                appointmentsList.add(
                                    AppointmentData(
                                        mobileNumber = appointment.getString("mobile_number"),
                                        leadName = appointment.getString("lead_name"),
                                        emailId = appointment.getString("email_id"),
                                        createdBy = appointment.getString("createdBy"),
                                        alternativeMobile = appointment.optString("alternative_mobile", ""),
                                        state = appointment.optString("state", ""),
                                        location = appointment.optString("location", ""),
                                        subLocation = appointment.optString("sub_location", ""),
                                        pinCode = appointment.optString("pin_code", ""),
                                        source = appointment.optString("source", ""),
                                        qualification = appointment.optString("user_qualification", ""),
                                        address = appointment.optString("residential_address", ""),
                                        customerType = appointment.optString("customer_type", "")
                                    )
                                )
                            }
                            appointments = appointmentsList
                            error = null
                        } else {
                            error = jsonResponse.getString("message")
                        }
                    } catch (e: Exception) {
                        Log.e("EducationalAppointmentPanel", "Error parsing response", e)
                        error = "Error parsing response: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                Log.e("EducationalAppointmentPanel", "Error fetching appointments", e)
                withContext(Dispatchers.Main) {
                    error = "Error fetching appointments: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Appointment Educational List",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Search Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { if (it.length <= 10) mobileNumber = it },
                        label = { Text("Enter Mobile Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { 
                                mobileNumber = ""
                                appointments = emptyList()
                                error = null
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = { fetchAppointments() }
                        ) {
                            Text("Search")
                        }
                    }
                }
            }

            // Loading and Error States
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Appointments List
            if (appointments.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(appointments) { appointment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Basic Details
                                Text(
                                    text = appointment.leadName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Mobile: ${appointment.mobileNumber}",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Email: ${appointment.emailId}",
                                    fontSize = 14.sp
                                )
                                
                                // Additional Details
                                Text(
                                    text = "Alternative Mobile: ${appointment.alternativeMobile}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Qualification: ${appointment.qualification}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Address: ${appointment.address}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamSalAppointmentPanel(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Coming Soon",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Team SAL Appointment feature is under development",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamSenpAppointmentPanel(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Coming Soon",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Team SENP Appointment feature is under development",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamSepAppointmentPanel(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Coming Soon",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Team SEP Appointment feature is under development",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamNriAppointmentPanel(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Coming Soon",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Team NRI Appointment feature is under development",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamEducationalAppointmentPanel(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Coming Soon",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Team Educational Appointment feature is under development",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AppointmentTable(appointments: List<AppointmentData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Mobile Number",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Lead Name",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Email ID",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Created By",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Actions",
                    modifier = Modifier.weight(0.5f),
                    fontWeight = FontWeight.Bold
                )
            }

            // Table Content
            appointments.forEach { appointment ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        appointment.mobileNumber,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        appointment.leadName,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        appointment.emailId,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        appointment.createdBy,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { /* TODO: View action */ }) {
                            Icon(Icons.Default.Visibility, contentDescription = "View")
                        }
                        IconButton(onClick = { /* TODO: Edit action */ }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
                Divider()
            }
        }
    }
}

data class AppointmentData(
    val mobileNumber: String,
    val leadName: String,
    val emailId: String,
    val createdBy: String,
    val alternativeMobile: String = "",
    val state: String = "",
    val location: String = "",
    val subLocation: String = "",
    val pinCode: String = "",
    val source: String = "",
    val qualification: String = "",
    val address: String = "",
    val customerType: String = ""
)

private val appointments = listOf(
    "Add Appointment",
    "Salaried-SAL",
    "Self Employed Non Professionals-SENP",
    "Self Employed Professionals-SEP",
    "NRI",
    "Educational",
    "Team SAL Appointment (Salaried)",
    "Team SENP Appointment (Self Employed Non Professionals)",
    "Team SEP Appointment (Self Employed Professionals)",
    "Team NRI Appointment (Non-Resident Indian)",
    "Team Educational Appointment (Educational)"
)

private val files = listOf(
    "Recent Documents",
    "Shared Files",
    "My Documents",
    "Team Documents",
    "Archived Files"
)

private val moreOptions = listOf(
    "Settings",
    "Help & Support",
    "About",
    "Logout"
)

@Preview(showBackground = true)
@Composable
fun MainPanelScreenPreview() {
    MaterialTheme {
        MainPanelScreen(
            onMenuClick = {},
            onHelpClick = {},
            onAccountClick = {},
            onNavigateToAddAppointment = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAgentPanel(onDismiss: () -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var alternativePhone by remember { mutableStateOf("") }
    
    var selectedBranchState by remember { mutableStateOf("") }
    var selectedPartnerType by remember { mutableStateOf("") }
    var selectedBranchLocation by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf("") }
    
    var branchStateExpanded by remember { mutableStateOf(false) }
    var partnerTypeExpanded by remember { mutableStateOf(false) }
    var branchLocationExpanded by remember { mutableStateOf(false) }
    
    val branchStates = listOf("State 1", "State 2", "State 3")
    val partnerTypes = listOf("Type 1", "Type 2", "Type 3")
    val branchLocations = listOf("Location 1", "Location 2", "Location 3")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add Agent Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Phone Number
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { if (it.length <= 10) phoneNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // Company Name
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    // Branch State Dropdown
                    ExposedDropdownMenuBox(
                        expanded = branchStateExpanded,
                        onExpandedChange = { branchStateExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBranchState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Branch State") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchStateExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .padding(bottom = 16.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = branchStateExpanded,
                            onDismissRequest = { branchStateExpanded = false }
                        ) {
                            branchStates.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(state) },
                                    onClick = {
                                        selectedBranchState = state
                                        branchStateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Address
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        minLines = 3
                    )

                    // Alternative Phone
                    OutlinedTextField(
                        value = alternativePhone,
                        onValueChange = { if (it.length <= 10) alternativePhone = it },
                        label = { Text("Alternative Phone Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    // Partner Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = partnerTypeExpanded,
                        onExpandedChange = { partnerTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedPartnerType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type of Partner") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partnerTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .padding(bottom = 16.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = partnerTypeExpanded,
                            onDismissRequest = { partnerTypeExpanded = false }
                        ) {
                            partnerTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedPartnerType = type
                                        partnerTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Branch Location Dropdown
                    ExposedDropdownMenuBox(
                        expanded = branchLocationExpanded,
                        onExpandedChange = { branchLocationExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBranchLocation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Branch Location") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchLocationExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .padding(bottom = 16.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = branchLocationExpanded,
                            onDismissRequest = { branchLocationExpanded = false }
                        ) {
                            branchLocations.forEach { location ->
                                DropdownMenuItem(
                                    text = { Text(location) },
                                    onClick = {
                                        selectedBranchLocation = location
                                        branchLocationExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Visiting Card File Selection
                    OutlinedButton(
                        onClick = { /* TODO: Implement file selection */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text("Choose Visiting Card")
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            // TODO: Implement submit functionality
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAgentPanel(onDismiss: () -> Unit) {
    var selectedAgentType by remember { mutableStateOf("") }
    var selectedBranchState by remember { mutableStateOf("") }
    var selectedBranchLocation by remember { mutableStateOf("") }
    
    var agentTypeExpanded by remember { mutableStateOf(false) }
    var branchStateExpanded by remember { mutableStateOf(false) }
    var branchLocationExpanded by remember { mutableStateOf(false) }
    
    val agentTypes = listOf("Type 1", "Type 2", "Type 3")
    val branchStates = listOf("State 1", "State 2", "State 3")
    val branchLocations = listOf("Location 1", "Location 2", "Location 3")

    // Sample data for the table
    val agents = remember {
        listOf(
            AgentData(
                fullName = "John Doe",
                companyName = "Company 1",
                mobile = "9876543210",
                agentType = "Type 1",
                branchState = "State 1",
                branchLocation = "Location 1",
                createdBy = "Admin"
            ),
            AgentData(
                fullName = "Jane Smith",
                companyName = "Company 2",
                mobile = "9876543211",
                agentType = "Type 2",
                branchState = "State 2",
                branchLocation = "Location 2",
                createdBy = "Admin"
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Agent List",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Filter Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Agent Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = agentTypeExpanded,
                        onExpandedChange = { agentTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedAgentType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Agent Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = agentTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .padding(bottom = 16.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = agentTypeExpanded,
                            onDismissRequest = { agentTypeExpanded = false }
                        ) {
                            agentTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedAgentType = type
                                        agentTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Branch State Dropdown
                    ExposedDropdownMenuBox(
                        expanded = branchStateExpanded,
                        onExpandedChange = { branchStateExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBranchState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Branch State") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchStateExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .padding(bottom = 16.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = branchStateExpanded,
                            onDismissRequest = { branchStateExpanded = false }
                        ) {
                            branchStates.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(state) },
                                    onClick = {
                                        selectedBranchState = state
                                        branchStateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Branch Location Dropdown
                    ExposedDropdownMenuBox(
                        expanded = branchLocationExpanded,
                        onExpandedChange = { branchLocationExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBranchLocation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Branch Location") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchLocationExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .padding(bottom = 16.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = branchLocationExpanded,
                            onDismissRequest = { branchLocationExpanded = false }
                        ) {
                            branchLocations.forEach { location ->
                                DropdownMenuItem(
                                    text = { Text(location) },
                                    onClick = {
                                        selectedBranchLocation = location
                                        branchLocationExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Filter and Reset Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedAgentType = ""
                                selectedBranchState = ""
                                selectedBranchLocation = ""
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = {
                                // TODO: Implement filter functionality
                            }
                        ) {
                            Text("Filter")
                        }
                    }
                }
            }

            // Table Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Full Name",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Company Name",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Mobile",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Agent Type",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Branch State",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Branch Location",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Table Content
                    agents.forEach { agent ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                agent.fullName,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                agent.companyName,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                agent.mobile,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                agent.agentType,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                agent.branchState,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                agent.branchLocation,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

data class AgentData(
    val fullName: String,
    val companyName: String,
    val mobile: String,
    val agentType: String,
    val branchState: String,
    val branchLocation: String,
    val createdBy: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DsaCodePanel(
    onDismiss: () -> Unit,
    viewModel: DsaCodeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    var selectedVendorBank by remember { mutableStateOf("") }
    var selectedLoanType by remember { mutableStateOf("") }
    var selectedBranchState by remember { mutableStateOf("") }
    var selectedBranchLocation by remember { mutableStateOf("") }
    
    var vendorBankExpanded by remember { mutableStateOf(false) }
    var loanTypeExpanded by remember { mutableStateOf(false) }
    var branchStateExpanded by remember { mutableStateOf(false) }
    var branchLocationExpanded by remember { mutableStateOf(false) }
    
    // Track expanded state for each DSA code
    var expandedDsaCodeId by remember { mutableStateOf<String?>(null) }

    // Fetch data when panel is opened
    LaunchedEffect(Unit) {
        viewModel.fetchVendorBanks()
        viewModel.fetchLoanTypes()
        viewModel.fetchBranchStates()
        viewModel.fetchBranchLocations()
        viewModel.fetchDsaCodes()
    }

    // Fetch detailed information when a vendor bank is selected
    LaunchedEffect(selectedVendorBank) {
        if (selectedVendorBank.isNotEmpty()) {
            viewModel.fetchDetailedDsaCodes(selectedVendorBank)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "DSA Code List",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Filter Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Vendor Bank Dropdown
                    ExposedDropdownMenuBox(
                        expanded = vendorBankExpanded,
                        onExpandedChange = { vendorBankExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedVendorBank,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Vendor Bank") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vendorBankExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = vendorBankExpanded,
                            onDismissRequest = { vendorBankExpanded = false }
                        ) {
                            state.vendorBanks.forEach { bank ->
                                DropdownMenuItem(
                                    text = { Text(bank) },
                                    onClick = {
                                        selectedVendorBank = bank
                                        vendorBankExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Loan Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = loanTypeExpanded,
                        onExpandedChange = { loanTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedLoanType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Loan Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = loanTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = loanTypeExpanded,
                            onDismissRequest = { loanTypeExpanded = false }
                        ) {
                            state.loanTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedLoanType = type
                                        loanTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Branch State Dropdown
                    ExposedDropdownMenuBox(
                        expanded = branchStateExpanded,
                        onExpandedChange = { branchStateExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBranchState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Branch State") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchStateExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = branchStateExpanded,
                            onDismissRequest = { branchStateExpanded = false }
                        ) {
                            state.branchStates.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(state) },
                                    onClick = {
                                        selectedBranchState = state
                                        branchStateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Branch Location Dropdown
                    ExposedDropdownMenuBox(
                        expanded = branchLocationExpanded,
                        onExpandedChange = { branchLocationExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBranchLocation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Branch Location") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchLocationExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = branchLocationExpanded,
                            onDismissRequest = { branchLocationExpanded = false }
                        ) {
                            state.branchLocations.forEach { location ->
                                DropdownMenuItem(
                                    text = { Text(location) },
                                    onClick = {
                                        selectedBranchLocation = location
                                        branchLocationExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedVendorBank = ""
                                selectedLoanType = ""
                                selectedBranchState = ""
                                selectedBranchLocation = ""
                                viewModel.fetchDsaCodes()
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = {
                                viewModel.filterDsaCodes(
                                    vendorBank = selectedVendorBank,
                                    loanType = selectedLoanType,
                                    state = selectedBranchState,
                                    location = selectedBranchLocation
                                )
                            }
                        ) {
                            Text("Filter")
                        }
                    }
                }
            }

            // DSA Codes List Section
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.dsaCodes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No DSA codes found",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(state.dsaCodes) { dsaCode ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    expandedDsaCodeId = if (expandedDsaCodeId == dsaCode.dsaCode) null else dsaCode.dsaCode
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                        text = dsaCode.dsaName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = if (expandedDsaCodeId == dsaCode.dsaCode) 
                                            Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (expandedDsaCodeId == dsaCode.dsaCode) 
                                            "Show less" else "Show more"
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Basic Information (always visible)
                                InfoRow("Bank", dsaCode.vendorBank)
                                InfoRow("DSA Code", dsaCode.dsaCode)
                                InfoRow("Loan Type", dsaCode.loanType)
                                
                                // Expanded Information
                                if (expandedDsaCodeId == dsaCode.dsaCode) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Additional Details
                                    InfoRow("State", dsaCode.state)
                                    InfoRow("Location", dsaCode.location)
                                    dsaCode.contactPerson?.let { InfoRow("Contact Person", it) }
                                    dsaCode.phoneNumber?.let { InfoRow("Phone Number", it) }
                                    dsaCode.email?.let { InfoRow("Email", it) }
                                    dsaCode.address?.let { InfoRow("Address", it) }
                                    dsaCode.registrationDate?.let { InfoRow("Registration Date", it) }
                                    dsaCode.status?.let { InfoRow("Status", it) }
                                    
                                    // Performance Metrics
                                    dsaCode.performanceMetrics?.let { metrics ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Performance Metrics",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        InfoRow("Total Applications", metrics.totalApplications.toString())
                                        InfoRow("Approved", metrics.approved.toString())
                                        InfoRow("Rejected", metrics.rejected.toString())
                                        InfoRow("Pending", metrics.pending.toString())
                                        InfoRow("Success Rate", "${metrics.successRate}%")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankersPanel(onDismiss: () -> Unit) {
    var showAddBanker by remember { mutableStateOf(false) }
    var showListBankers by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Bankers",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Add Banker Option
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { showAddBanker = true },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Banker")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Add Banker", fontSize = 16.sp)
                }
            }

            // List Bankers Option
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { showListBankers = true },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.List, contentDescription = "List Bankers")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("List Bankers", fontSize = 16.sp)
                }
            }
        }
    }

    if (showAddBanker) {
        AddBankerPanel(onDismiss = { showAddBanker = false })
    }

    if (showListBankers) {
        ListBankersPanel(onDismiss = { showListBankers = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBankerPanel(onDismiss: () -> Unit) {
    var vendorBank by remember { mutableStateOf("") }
    var bankerName by remember { mutableStateOf("") }
    var bankerDesignation by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var visitingCardUri by remember { mutableStateOf<Uri?>(null) }
    var visitingCardFileName by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf("") }

    var vendorBankExpanded by remember { mutableStateOf(false) }
    var designationExpanded by remember { mutableStateOf(false) }
    var loanTypeExpanded by remember { mutableStateOf(false) }
    var stateExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    val viewModel: BankerViewModel = hiltViewModel()
    val bankerState by viewModel.state.collectAsState()
    val context = LocalContext.current

    // File picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            visitingCardUri = it
            visitingCardFileName = getFileName(context.contentResolver, it)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add Banker",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Vendor Bank Dropdown
            ExposedDropdownMenuBox(
                expanded = vendorBankExpanded,
                onExpandedChange = { vendorBankExpanded = it }
            ) {
                OutlinedTextField(
                    value = vendorBank,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Vendor Bank") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vendorBankExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 16.dp)
                )

                ExposedDropdownMenu(
                    expanded = vendorBankExpanded,
                    onDismissRequest = { vendorBankExpanded = false }
                ) {
                    if (bankerState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (bankerState.vendorBanks.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No banks available") },
                            onClick = { }
                        )
                    } else {
                        bankerState.vendorBanks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank) },
                                onClick = {
                                    vendorBank = bank
                                    vendorBankExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Banker Name
            OutlinedTextField(
                value = bankerName,
                onValueChange = { bankerName = it },
                label = { Text("Banker Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Banker Designation
            ExposedDropdownMenuBox(
                expanded = designationExpanded,
                onExpandedChange = { designationExpanded = it }
            ) {
                OutlinedTextField(
                    value = bankerDesignation,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Designation") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = designationExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 16.dp)
                )

                ExposedDropdownMenu(
                    expanded = designationExpanded,
                    onDismissRequest = { designationExpanded = false }
                ) {
                    if (bankerState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (bankerState.designations.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No designations available") },
                            onClick = { }
                        )
                    } else {
                        bankerState.designations.forEach { designation ->
                            DropdownMenuItem(
                                text = { Text(designation) },
                                onClick = {
                                    bankerDesignation = designation
                                    designationExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Mobile Number
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email ID") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Loan Type
            ExposedDropdownMenuBox(
                expanded = loanTypeExpanded,
                onExpandedChange = { loanTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = loanType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Loan Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = loanTypeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 16.dp)
                )

                ExposedDropdownMenu(
                    expanded = loanTypeExpanded,
                    onDismissRequest = { loanTypeExpanded = false }
                ) {
                    if (bankerState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (bankerState.loanTypes.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No loan types available") },
                            onClick = { }
                        )
                    } else {
                        bankerState.loanTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    loanType = type
                                    loanTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // State
            ExposedDropdownMenuBox(
                expanded = stateExpanded,
                onExpandedChange = { stateExpanded = it }
            ) {
                OutlinedTextField(
                    value = state,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("State") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 16.dp)
                )

                ExposedDropdownMenu(
                    expanded = stateExpanded,
                    onDismissRequest = { stateExpanded = false }
                ) {
                    if (bankerState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (bankerState.branchStates.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No states available") },
                            onClick = { }
                        )
                    } else {
                        bankerState.branchStates.forEach { branchState ->
                            DropdownMenuItem(
                                text = { Text(branchState) },
                                onClick = {
                                    state = branchState
                                    stateExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Location
            ExposedDropdownMenuBox(
                expanded = locationExpanded,
                onExpandedChange = { locationExpanded = it }
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Location") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 16.dp)
                )

                ExposedDropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false }
                ) {
                    if (bankerState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (bankerState.branchLocations.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No locations available") },
                            onClick = { }
                        )
                    } else {
                        bankerState.branchLocations.forEach { branchLocation ->
                            DropdownMenuItem(
                                text = { Text(branchLocation) },
                                onClick = {
                                    location = branchLocation
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Visiting Card File Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = visitingCardFileName ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Visiting Card") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { launcher.launch("application/pdf,image/*") }) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                        }
                    }
                )
            }

            // Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Error message
            bankerState.addBankerError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Submit Button
            Button(
                onClick = {
                    if (vendorBank.isBlank() || bankerName.isBlank() || 
                        mobileNumber.isBlank() || email.isBlank() || 
                        bankerDesignation.isBlank() || loanType.isBlank() || 
                        state.isBlank() || location.isBlank()) {
                        Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    // Handle file upload
                    visitingCardUri?.let { uri ->
                        val fileName = visitingCardFileName ?: "visiting_card"
                        val file = uriToFile(context, uri, fileName)
                        viewModel.addBanker(
                            vendorBank = vendorBank,
                            bankerName = bankerName,
                            phoneNumber = mobileNumber,
                            emailId = email,
                            bankerDesignation = bankerDesignation,
                            loanType = loanType,
                            state = state,
                            location = location,
                            visitingCard = file.absolutePath,
                            address = address
                        )
                    } ?: run {
                        viewModel.addBanker(
                            vendorBank = vendorBank,
                            bankerName = bankerName,
                            phoneNumber = mobileNumber,
                            emailId = email,
                            bankerDesignation = bankerDesignation,
                            loanType = loanType,
                            state = state,
                            location = location,
                            visitingCard = "",
                            address = address
                        )
                    }
                    
                    if (bankerState.addBankerError == null) {
                        Toast.makeText(context, "Banker added successfully", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = !bankerState.isAddingBanker
            ) {
                if (bankerState.isAddingBanker) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Banker")
                }
            }
        }
    }
}

// Helper function to convert Uri to File
private fun uriToFile(context: Context, uri: Uri, fileName: String): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, fileName)
    inputStream?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
    return file
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListBankersPanel(onDismiss: () -> Unit) {
    var selectedVendorBank by remember { mutableStateOf("") }
    var selectedLoanType by remember { mutableStateOf("") }
    var selectedBranchState by remember { mutableStateOf("") }
    var selectedBranchLocation by remember { mutableStateOf("") }
    var expandedBankerId by remember { mutableStateOf<Int?>(null) }
    
    var vendorBankExpanded by remember { mutableStateOf(false) }
    var loanTypeExpanded by remember { mutableStateOf(false) }
    var branchStateExpanded by remember { mutableStateOf(false) }
    var branchLocationExpanded by remember { mutableStateOf(false) }
    
    val viewModel: BankerViewModel = hiltViewModel()
    val bankerState by viewModel.state.collectAsState()

    // Fetch data when panel is opened
    LaunchedEffect(Unit) {
        viewModel.loadVendorBanks()
        viewModel.loadLoanTypes()
        viewModel.loadBranchStates()
        viewModel.loadBranchLocations()
        viewModel.filterBankers("", "", "", "") // Load all bankers initially
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Bankers List",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Filter Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Vendor Bank Dropdown
                    ExposedDropdownMenuBox(
                        expanded = vendorBankExpanded,
                        onExpandedChange = { vendorBankExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedVendorBank,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Vendor Bank") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vendorBankExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = vendorBankExpanded,
                            onDismissRequest = { vendorBankExpanded = false }
                        ) {
                            if (bankerState.isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else if (bankerState.vendorBanks.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No banks available") },
                                    onClick = { }
                                )
                            } else {
                                bankerState.vendorBanks.forEach { bank ->
                                    DropdownMenuItem(
                                        text = { Text(bank) },
                                        onClick = {
                                            selectedVendorBank = bank
                                            vendorBankExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Loan Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = loanTypeExpanded,
                        onExpandedChange = { loanTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedLoanType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Loan Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = loanTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = loanTypeExpanded,
                            onDismissRequest = { loanTypeExpanded = false }
                        ) {
                            if (bankerState.isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else if (bankerState.loanTypes.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No loan types available") },
                                    onClick = { }
                                )
                            } else {
                                bankerState.loanTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            selectedLoanType = type
                                            loanTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Branch State Dropdown
                    ExposedDropdownMenuBox(
                        expanded = branchStateExpanded,
                        onExpandedChange = { branchStateExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBranchState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Branch State") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchStateExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = branchStateExpanded,
                            onDismissRequest = { branchStateExpanded = false }
                        ) {
                            if (bankerState.isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else if (bankerState.branchStates.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No states available") },
                                    onClick = { }
                                )
                            } else {
                                bankerState.branchStates.forEach { state ->
                                    DropdownMenuItem(
                                        text = { Text(state) },
                                        onClick = {
                                            selectedBranchState = state
                                            branchStateExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Branch Location Dropdown
                    ExposedDropdownMenuBox(
                        expanded = branchLocationExpanded,
                        onExpandedChange = { branchLocationExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBranchLocation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Branch Location") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchLocationExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = branchLocationExpanded,
                            onDismissRequest = { branchLocationExpanded = false }
                        ) {
                            if (bankerState.isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else if (bankerState.branchLocations.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No locations available") },
                                    onClick = { }
                                )
                            } else {
                                bankerState.branchLocations.forEach { location ->
                                    DropdownMenuItem(
                                        text = { Text(location) },
                                        onClick = {
                                            selectedBranchLocation = location
                                            branchLocationExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedVendorBank = ""
                                selectedLoanType = ""
                                selectedBranchState = ""
                                selectedBranchLocation = ""
                                viewModel.filterBankers("", "", "", "")
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = {
                                viewModel.filterBankers(
                                    vendorBank = selectedVendorBank,
                                    loanType = selectedLoanType,
                                    state = selectedBranchState,
                                    location = selectedBranchLocation
                                )
                            }
                        ) {
                            Text("Filter")
                        }
                    }
                }
            }

            // Results Section
            if (bankerState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (bankerState.error != null) {
                val errorMessage = bankerState.error ?: "Unknown error"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (bankerState.filteredBankers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedVendorBank.isEmpty() && selectedLoanType.isEmpty() && 
                                 selectedBranchState.isEmpty() && selectedBranchLocation.isEmpty()) {
                            "Please select filters and click Filter button"
                        } else {
                            "No bankers found for the selected filters"
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn {
                    items(bankerState.filteredBankers) { banker ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = banker.bankerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Phone: ${banker.mobileNumber}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Email: ${banker.email}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Bank: ${banker.vendorBank}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Loan Type: ${banker.loanType}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



data class BankerData(
    val id: Int,
    val vendorBank: String,
    val bankerName: String,
    val bankerDesignation: String,
    val mobileNumber: String,
    val email: String,
    val loanType: String,
    val state: String,
    val location: String,
    val visitingCard: String,
    val address: String
)

@Composable
fun StatisticsBox(
    title: String,
    count: String,
    amount: String? = null
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (amount != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total Amount: $amount",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun StatisticsSection(
    salPoints: Int = 0,
    senpPoints: Int = 0,
    sepPoints: Int = 0,
    nriPoints: Int = 0,
    educationalPoints: Int = 0,
    teamSalPoints: Int = 0,
    teamSenpPoints: Int = 0,
    teamSepPoints: Int = 0,
    teamNriPoints: Int = 0,
    teamEducationalPoints: Int = 0
) {
    var totalPortfolio by remember { mutableStateOf("0") }
    var totalPartners by remember { mutableStateOf("0") }
    var totalConnectors by remember { mutableStateOf("0") }
    var totalAgents by remember { mutableStateOf("0") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Fetch all counts
    LaunchedEffect(Unit) {
        try {
            // Fetch total portfolio
            val portfolioResponse = withContext(Dispatchers.IO) {
                val url = URL("https://pznstudio.shop/get_total_portfolio.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                try {
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        Log.d("StatisticsSection", "Portfolio Response: $response")
                        JSONObject(response)
                    } else {
                        throw Exception("HTTP Error: $responseCode")
                    }
                } finally {
                    connection.disconnect()
                }
            }

            // Fetch total partners
            val partnersResponse = withContext(Dispatchers.IO) {
                val url = URL("https://pznstudio.shop/get_total_partners.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                try {
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        Log.d("StatisticsSection", "Partners Response: $response")
                        JSONObject(response)
                    } else {
                        throw Exception("HTTP Error: $responseCode")
                    }
                } finally {
                    connection.disconnect()
                }
            }

            // Fetch total connectors
            val connectorsResponse = withContext(Dispatchers.IO) {
                val url = URL("https://pznstudio.shop/get_total_connectors.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                try {
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        Log.d("StatisticsSection", "Connectors Response: $response")
                        JSONObject(response)
                    } else {
                        throw Exception("HTTP Error: $responseCode")
                    }
                } finally {
                    connection.disconnect()
                }
            }

            // Fetch total agents
            val agentsResponse = withContext(Dispatchers.IO) {
                val url = URL("https://pznstudio.shop/get_total_agents.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                try {
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        Log.d("StatisticsSection", "Agents Response: $response")
                        JSONObject(response)
                    } else {
                        throw Exception("HTTP Error: $responseCode")
                    }
                } finally {
                    connection.disconnect()
                }
            }

            // Update states with fetched values
            if (portfolioResponse.getBoolean("success")) {
                totalPortfolio = portfolioResponse.getInt("data").toString()
            }
            if (partnersResponse.getBoolean("success")) {
                totalPartners = partnersResponse.getInt("data").toString()
            }
            if (connectorsResponse.getBoolean("success")) {
                totalConnectors = connectorsResponse.getInt("data").toString()
            }
            if (agentsResponse.getBoolean("success")) {
                totalAgents = agentsResponse.getInt("data").toString()
            }

        } catch (e: Exception) {
            error = e.message
            Log.e("StatisticsSection", "Exception: ${e.message}")
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            item {
                StatisticsBox(
                    title = "Total My Portfolio",
                    count = if (isLoading) "Loading..." else if (error != null) "Error" else totalPortfolio,
                    amount = "0.00"
                )
            }
            item {
                StatisticsBox(
                    title = "Total Team Portfolio",
                    count = "0",
                    amount = "0.00"
                )
            }
            item {
                StatisticsBox(
                    title = "Team SAL Data",
                    count = teamSalPoints.toString(),
                    amount = null
                )
            }
            item {
                StatisticsBox(
                    title = "Team SENP Data",
                    count = teamSenpPoints.toString(),
                    amount = null
                )
            }
            item {
                StatisticsBox(
                    title = "Team SEP Data",
                    count = teamSepPoints.toString(),
                    amount = null
                )
            }
            item {
                StatisticsBox(
                    title = "Team NRI Data",
                    count = teamNriPoints.toString(),
                    amount = null
                )
            }
            item {
                StatisticsBox(
                    title = "Team Educational Data",
                    count = teamEducationalPoints.toString(),
                    amount = null
                )
            }
            item {
                StatisticsBox(
                    title = "Team SAL Appt",
                    count = salPoints.toString(),
                    amount = null
                )
            }
            item {
                StatisticsBox(
                    title = "Team SENP Appt",
                    count = senpPoints.toString(),
                    amount = null
                )
            }
            item {
                StatisticsBox(
                    title = "Team SEP Appt",
                    count = sepPoints.toString(),
                    amount = null
                )
            }
            item {
                StatisticsBox(
                    title = "Team NRI Appt",
                    count = nriPoints.toString(),
                    amount = null
                )
            }
            item {
                StatisticsBox(
                    title = "Team Educational Appt",
                    count = educationalPoints.toString(),
                    amount = null
                )
            }
            item {
                StatisticsBox(
                    title = "Total Partners",
                    count = if (isLoading) "Loading..." else if (error != null) "Error" else totalPartners,
                    amount = null
                )
            }
            item {
                StatisticsBox(
                    title = "Total Connectors",
                    count = if (isLoading) "Loading..." else if (error != null) "Error" else totalConnectors,
                    amount = null
                )
            }
            item {
                StatisticsBox(
                    title = "Total Agents",
                    count = if (isLoading) "Loading..." else if (error != null) "Error" else totalAgents,
                    amount = null
                )
            }
        }

        if (error != null) {
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAccountPanel(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: MainPanelViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val firstName by viewModel.userFirstName.collectAsState()
    val lastName by viewModel.userLastName.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "My Account",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // User Information Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = if (firstName != null && lastName != null) {
                            "$firstName $lastName"
                        } else {
                            "User"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Settings Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Account Settings",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Profile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onNavigateToProfile() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Profile")
                        Icon(Icons.Default.ArrowForward, contentDescription = "Profile")
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Change Password
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { showChangePassword = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Change Password")
                        Icon(Icons.Default.ArrowForward, contentDescription = "Change Password")
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Logout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { showLogoutDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Logout")
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }

    if (showChangePassword) {
        ChangePasswordPanel(
            onDismiss = { showChangePassword = false }
        )
    }
} 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpPanel(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Help & Support",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // FAQ Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Frequently Asked Questions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // FAQ Items
                    FaqItem(
                        question = "How do I add a new appointment?",
                        answer = "Click on 'Add Appointment' in the Appointments section. Fill in the required details and submit the form."
                    )

                    FaqItem(
                        question = "How do I view my appointments?",
                        answer = "Go to the Appointments section and select the type of appointment you want to view (SAL, SENP, SEP, etc.)."
                    )

                    FaqItem(
                        question = "How do I manage my team?",
                        answer = "Use the Team section to view and manage your team members' appointments and performance."
                    )

                    FaqItem(
                        question = "How do I upload files?",
                        answer = "Go to the Files section and use the file upload feature to add new documents."
                    )
                }
            }

            // Contact Support Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Contact Support",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Support Contact Items
                    SupportContactItem(
                        icon = Icons.Default.Email,
                        title = "Email Support",
                        description = "support@kurakulas.com"
                    )

                    SupportContactItem(
                        icon = Icons.Default.Phone,
                        title = "Phone Support",
                        description = "+91 1800-XXX-XXXX"
                    )

                    SupportContactItem(
                        icon = Icons.Default.Chat,
                        title = "Live Chat",
                        description = "Available 24/7"
                    )
                }
            }

            // Quick Links Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Quick Links",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Quick Link Items
                    QuickLinkItem(
                        title = "User Guide",
                        onClick = { /* TODO: Open user guide */ }
                    )

                    QuickLinkItem(
                        title = "Video Tutorials",
                        onClick = { /* TODO: Open video tutorials */ }
                    )

                    QuickLinkItem(
                        title = "Terms of Service",
                        onClick = { /* TODO: Open terms of service */ }
                    )

                    QuickLinkItem(
                        title = "Privacy Policy",
                        onClick = { /* TODO: Open privacy policy */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun FaqItem(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
        
        if (expanded) {
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun SupportContactItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickLinkItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Open $title"
        )
    }
} 
