package com.crimsonedge.studioadmin.presentation.navigation

sealed class Screen(val route: String) {

    data object Login : Screen("login")

    data object Dashboard : Screen("dashboard")

    data object Content : Screen("content")

    data object ArtworkList : Screen("artworks")

    data object ArtworkForm : Screen("artworks/{id}") {
        fun createRoute(id: Int? = null): String {
            return if (id != null) "artworks/$id" else "artworks/new"
        }
    }

    data object WritingList : Screen("writings")

    data object WritingForm : Screen("writings/{id}") {
        fun createRoute(id: Int? = null): String {
            return if (id != null) "writings/$id" else "writings/new"
        }
    }

    data object ImageList : Screen("images")

    data object SiteConfig : Screen("site-config")

    data object NavList : Screen("navigation")

    data object SocialLinks : Screen("social-links")

    data object ContactList : Screen("contacts")

    data object ContactDetail : Screen("contacts/{id}") {
        fun createRoute(id: Int): String = "contacts/$id"
    }

    data object More : Screen("more")
}
