package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.Creator
import com.example.data.model.HealthStatus
import com.example.data.model.TikTokLeague
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun testAppNameIsAngelVoice() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Angel Voice", appName)
  }

  @Test
  fun testHealthStatusTransitions() {
    val now = System.currentTimeMillis()
    val day = 86400000L

    // In streak (<= 3 days)
    val greenCreator = Creator(
      name = "Test Streamer",
      tiktokHandle = "@streamer",
      country = "VE",
      lastContactTimestamp = now - (1 * day),
      lastLiveAuditTimestamp = now - (1 * day)
    )
    assertEquals(HealthStatus.EN_RACHA_VERDE, greenCreator.calculateHealthStatus(now))

    // Maintenance (4 to 6 days)
    val yellowCreator = Creator(
      name = "Test Streamer",
      tiktokHandle = "@streamer",
      country = "MX",
      lastContactTimestamp = now - (5 * day),
      lastLiveAuditTimestamp = now - (2 * day)
    )
    assertEquals(HealthStatus.MANTENIMIENTO_AMARILLO, yellowCreator.calculateHealthStatus(now))

    // In Risk (>= 7 days without contact)
    val redCreator = Creator(
      name = "Test Streamer",
      tiktokHandle = "@streamer",
      country = "AR",
      lastContactTimestamp = now - (8 * day),
      lastLiveAuditTimestamp = now - (3 * day)
    )
    assertEquals(HealthStatus.EN_RIESGO_ROJO, redCreator.calculateHealthStatus(now))
  }

  @Test
  fun testRoomDatabasePersistence() = runBlocking {
    val creator = Creator(
      creatorId = "test-c-1",
      name = "Maria Streamer",
      tiktokHandle = "@maria_live",
      country = "VE",
      tiktokLeague = TikTokLeague.LIGA_B,
      diamondsEarned = 75000L,
      peakViewers = 1200
    )

    db.creatorDao().insertCreator(creator)
    val retrieved = db.creatorDao().getCreatorById("test-c-1")

    assertNotNull(retrieved)
    assertEquals("Maria Streamer", retrieved?.name)
    assertEquals(75000L, retrieved?.diamondsEarned)
    assertEquals(1200, retrieved?.peakViewers)
    assertEquals(1, db.creatorDao().getCreatorCount())

    // Test Update
    db.creatorDao().updateLeague("test-c-1", TikTokLeague.LIGA_A)
    val updated = db.creatorDao().getCreatorById("test-c-1")
    assertEquals(TikTokLeague.LIGA_A, updated?.tiktokLeague)

    // Test Delete
    db.creatorDao().deleteCreatorById("test-c-1")
    assertEquals(0, db.creatorDao().getCreatorCount())
  }
}
