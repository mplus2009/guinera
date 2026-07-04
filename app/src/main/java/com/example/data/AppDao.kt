package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    @Query("SELECT * FROM local_product_config WHERE productId = :productId")
    fun getLocalProductConfig(productId: String): Flow<LocalProductConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalProductConfig(config: LocalProductConfig)

    @Query("SELECT * FROM seen_notifications")
    fun getSeenNotifications(): Flow<List<SeenNotification>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSeenNotification(seen: SeenNotification)

    @Query("SELECT * FROM local_product_config")
    fun getAllLocalProductConfigs(): Flow<List<LocalProductConfig>>

    @Query("SELECT * FROM p2p_contacts")
    fun getP2PContacts(): Flow<List<P2PContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertP2PContact(contact: P2PContact)

    @Query("SELECT * FROM p2p_messages WHERE (senderUid = :user1 AND receiverUid = :user2) OR (senderUid = :user2 AND receiverUid = :user1) ORDER BY timestamp ASC")
    fun getP2PMessages(user1: String, user2: String): Flow<List<P2PMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertP2PMessage(message: P2PMessage)
}
