package com.example.runtrack

import com.example.runtrack.settings.FriendRequestConfig
import com.example.runtrack.settings.FriendRequestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FriendRequestsTest {

    @Test
    fun testDefaultFriendRequestConfiguration() {
        val config = FriendRequestConfig()
        assertEquals(FriendRequestScope.EVERYONE, config.scope)
        assertTrue(config.allowSearchByContact)
        assertTrue(config.suggestToClubMembers)
        assertFalse(config.autoAcceptClubMembers)
    }

    @Test
    fun testScopeTransitions() {
        val config = FriendRequestConfig()

        config.scope = FriendRequestScope.FRIENDS_OF_FRIENDS
        assertEquals(FriendRequestScope.FRIENDS_OF_FRIENDS, config.scope)

        config.scope = FriendRequestScope.NO_ONE
        assertEquals(FriendRequestScope.NO_ONE, config.scope)
    }

    @Test
    fun testSocialDiscoveryToggles() {
        val config = FriendRequestConfig(
            scope = FriendRequestScope.EVERYONE,
            allowSearchByContact = false,
            suggestToClubMembers = false,
            autoAcceptClubMembers = true
        )

        assertFalse(config.allowSearchByContact)
        assertFalse(config.suggestToClubMembers)
        assertTrue(config.autoAcceptClubMembers)
    }

    @Test
    fun testScopeParsingFallback() {
        val valid = try {
            FriendRequestScope.valueOf("FRIENDS_OF_FRIENDS")
        } catch (_: Exception) {
            FriendRequestScope.EVERYONE
        }
        assertEquals(FriendRequestScope.FRIENDS_OF_FRIENDS, valid)

        val invalid = try {
            FriendRequestScope.valueOf("UNKNOWN_SCOPE_TYPE")
        } catch (_: Exception) {
            FriendRequestScope.EVERYONE
        }
        assertEquals(FriendRequestScope.EVERYONE, invalid)
    }
}
