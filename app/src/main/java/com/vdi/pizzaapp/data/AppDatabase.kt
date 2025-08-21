package com.vdi.pizzaapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [Pizza::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pizzaDao(): PizzaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null


        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                //non encrypted
               /* Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pizza_db"
                ).build().also { INSTANCE = it }*/

                //encrypted
                SQLiteDatabase.loadLibs(context)
                val  passphrase:ByteArray= SQLiteDatabase.getBytes("my_secure_key".toCharArray())
                val  factory = SupportFactory(passphrase)
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "encrypted-database"
                )
                    .openHelperFactory(factory)
                    .build()
                    .also { INSTANCE=it }
            }
        }
    }
}
