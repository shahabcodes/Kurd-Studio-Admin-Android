package com.crimsonedge.studioadmin.presentation.siteconfig

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crimsonedge.studioadmin.presentation.common.components.BrandLogo
import com.crimsonedge.studioadmin.presentation.siteconfig.hero.HeroEditorScreen
import com.crimsonedge.studioadmin.presentation.siteconfig.profile.ProfileEditorScreen
import com.crimsonedge.studioadmin.presentation.siteconfig.sections.SectionsListScreen
import com.crimsonedge.studioadmin.presentation.siteconfig.settings.SettingsListScreen
import com.crimsonedge.studioadmin.ui.theme.BrandGradientHorizontal
import com.crimsonedge.studioadmin.ui.theme.Pink500
import kotlinx.coroutines.launch

private enum class SiteConfigTab(
    val title: String,
    val icon: ImageVector
) {
    Profile("Profile", Icons.Rounded.Person),
    Hero("Hero", Icons.Rounded.RocketLaunch),
    Settings("Settings", Icons.Rounded.Settings),
    Sections("Sections", Icons.Rounded.ViewModule)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SiteConfigScreen() {
    val tabs = SiteConfigTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Site Configuration",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 12.dp)) {
                        BrandLogo()
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .background(
                                    brush = BrandGradientHorizontal,
                                    shape = MaterialTheme.shapes.extraSmall
                                )
                                .padding(vertical = 1.5.dp)
                        )
                    }
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = pagerState.currentPage == index
                    val animatedColor by animateColorAsState(
                        targetValue = if (selected) Pink500 else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(250),
                        label = "tab_color_$index"
                    )

                    Tab(
                        selected = selected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = animatedColor
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = animatedColor
                            )
                        }
                    )
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                when (tabs[page]) {
                    SiteConfigTab.Profile -> ProfileEditorScreen()
                    SiteConfigTab.Hero -> HeroEditorScreen()
                    SiteConfigTab.Settings -> SettingsListScreen()
                    SiteConfigTab.Sections -> SectionsListScreen()
                }
            }
        }
    }
}
