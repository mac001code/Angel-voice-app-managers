package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddCreatorDialog
import com.example.ui.components.AnimatedBlurredBackground
import com.example.ui.components.AuditLiveDialog
import com.example.ui.components.LeagueLevelUpDialog
import com.example.ui.components.WhatToSayBottomSheet
import com.example.ui.screens.CreatorDetailSheet
import com.example.ui.screens.ManagerScreen
import com.example.ui.screens.RecruiterScreen
import com.example.ui.screens.TacticalChatScreen
import com.example.ui.theme.CardBorder
import com.example.ui.theme.Ceniza
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.Mist
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PaletteVoid
import com.example.ui.theme.PantallaApagada
import com.example.ui.theme.Papel
import com.example.ui.theme.Stone
import com.example.viewmodel.AngelVoiceViewModel
import com.example.viewmodel.AppRole

class MainActivity : ComponentActivity() {

    private val viewModel: AngelVoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AngelVoiceMainContent(viewModel = viewModel)
            }
        }
    }
}

enum class NavigationTab {
    DASHBOARD,
    TACTICAL_AI
}

@Composable
fun AngelVoiceMainContent(viewModel: AngelVoiceViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val selectedCreatorForAi by viewModel.selectedCreatorForAi.collectAsState()
    val aiSuggestions by viewModel.aiSuggestions.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val auditCreator by viewModel.auditCreator.collectAsState()
    val detailCreator by viewModel.detailCreator.collectAsState()
    val creatorNotes by viewModel.creatorNotes.collectAsState()
    val promotionEvent by viewModel.promotionEvent.collectAsState()
    val riskCount by viewModel.riskCount.collectAsState()

    var activeTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var showAddCreatorDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(PaletteVoid),
        containerColor = Color.Transparent,
        topBar = {
            AngelVoiceHeader(
                currentRole = currentRole,
                activeTab = activeTab,
                onRoleChange = { role ->
                    viewModel.setRole(role)
                    activeTab = NavigationTab.DASHBOARD
                }
            )
        },
        bottomBar = {
            AngelVoiceBottomBar(
                activeTab = activeTab,
                riskCount = riskCount,
                onSelectDashboard = { activeTab = NavigationTab.DASHBOARD },
                onSelectAiChat = { activeTab = NavigationTab.TACTICAL_AI }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedBlurredBackground(
                modifier = Modifier.fillMaxSize()
            )

            AnimatedContent(
                targetState = Pair(activeTab, currentRole),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screenTransition"
            ) { (tab, role) ->
                when {
                    tab == NavigationTab.TACTICAL_AI -> {
                        TacticalChatScreen(viewModel = viewModel)
                    }
                    role == AppRole.MANAGER -> {
                        ManagerScreen(
                            viewModel = viewModel,
                            onOpenAddCreator = { showAddCreatorDialog = true }
                        )
                    }
                    else -> {
                        RecruiterScreen(
                            viewModel = viewModel,
                            onOpenAddProspect = { showAddCreatorDialog = true }
                        )
                    }
                }
            }
        }
    }

    selectedCreatorForAi?.let { creator ->
        WhatToSayBottomSheet(
            creator = creator,
            suggestions = aiSuggestions,
            isLoading = isAiLoading,
            onDismiss = { viewModel.closeWhatToSaySheet() }
        )
    }

    auditCreator?.let { creator ->
        AuditLiveDialog(
            creator = creator,
            onDismiss = { viewModel.closeAuditLiveDialog() },
            onConfirm = { note, isLiveAudit ->
                viewModel.confirmAuditLive(creator.creatorId, note, isLiveAudit)
            }
        )
    }

    detailCreator?.let { creator ->
        CreatorDetailSheet(
            creator = creator,
            notes = creatorNotes,
            onDismiss = { viewModel.closeCreatorDetail() },
            onAddNote = { text, isVoice -> viewModel.addBackstageNote(creator.creatorId, text, isVoice) },
            onPromoteLeague = { viewModel.promoteLeague(creator) },
            onIncrementScreenshot = { viewModel.incrementScreenshot(creator.creatorId) }
        )
    }

    promotionEvent?.let { event ->
        LeagueLevelUpDialog(
            event = event,
            onDismiss = { viewModel.dismissPromotionEvent() }
        )
    }

    if (showAddCreatorDialog) {
        AddCreatorDialog(
            initialIsRecruiterProspect = currentRole == AppRole.RECRUITER,
            onDismiss = { showAddCreatorDialog = false },
            onConfirm = { name, handle, country, niche, league, cadence, phone, isProspect ->
                viewModel.addNewCreator(name, handle, country, niche, league, cadence, phone, isProspect)
            }
        )
    }
}

@Composable
fun AngelVoiceHeader(
    currentRole: AppRole,
    activeTab: NavigationTab,
    onRoleChange: (AppRole) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Angel Voice",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Ink,
                            letterSpacing = (-0.3).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when (activeTab) {
                            NavigationTab.TACTICAL_AI -> "Mente · Inteligencia del ecosistema"
                            NavigationTab.DASHBOARD -> if (currentRole == AppRole.MANAGER) {
                                "Ecosistema · Gestión de creadores"
                            } else {
                                "Ecosistema · Descubrimiento"
                            }
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Mist,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (activeTab == NavigationTab.DASHBOARD) {
                Spacer(modifier = Modifier.width(10.dp))
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ElevatedSlate.copy(alpha = 0.9f))
                        .border(0.8.dp, CardBorder, CircleShape)
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModeSwitchPillOption(
                        label = "Scouting",
                        isSelected = currentRole == AppRole.RECRUITER,
                        onClick = { onRoleChange(AppRole.RECRUITER) },
                        testTag = "pill_modo_reclutador"
                    )

                    ModeSwitchPillOption(
                        label = "Manager",
                        isSelected = currentRole == AppRole.MANAGER,
                        onClick = { onRoleChange(AppRole.MANAGER) },
                        testTag = "pill_modo_manager"
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeSwitchPillOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) Papel else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) PantallaApagada else Stone,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                fontSize = 11.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
fun AngelVoiceBottomBar(
    activeTab: NavigationTab,
    riskCount: Int,
    onSelectDashboard: () -> Unit,
    onSelectAiChat: () -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = ElevatedSlate.copy(alpha = 0.95f),
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = activeTab == NavigationTab.DASHBOARD,
            onClick = onSelectDashboard,
            icon = {
                if (riskCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = Papel,
                                contentColor = PantallaApagada
                            ) {
                                Text("$riskCount", fontWeight = FontWeight.Medium)
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Ecosistema")
                    }
                } else {
                    Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Ecosistema")
                }
            },
            label = {
                Text(
                    text = "Ecosistema",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PantallaApagada,
                selectedTextColor = Papel,
                indicatorColor = Papel,
                unselectedIconColor = Ceniza,
                unselectedTextColor = Ceniza
            )
        )

        NavigationBarItem(
            selected = activeTab == NavigationTab.TACTICAL_AI,
            onClick = onSelectAiChat,
            icon = {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Mente")
            },
            label = {
                Text(
                    text = "Mente",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PantallaApagada,
                selectedTextColor = Papel,
                indicatorColor = Papel,
                unselectedIconColor = Ceniza,
                unselectedTextColor = Ceniza
            )
        )
    }
}