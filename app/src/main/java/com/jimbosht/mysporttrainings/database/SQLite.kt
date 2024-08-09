package com.jimbosht.mysporttrainings.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jimbosht.mysporttrainings.extra.ActivityDataClass

class SQLite(context: Context?) : SQLiteOpenHelper(context, "test_kotlin.db", null, 1) {

    companion object {
        const val TABLE_ACTIVITIES_ID = "activities_id"
        const val TABLE_REPS_ID = "reps_id"
        const val TABLE_ACTIVITIES = "activities"

        const val COLUMN_ACT_ID = "activity_id"
        const val COLUMN_NAME = "name"

        const val COLUMN_UUID = "uuid"
        const val COLUMN_REP_ID = "rep_id"
        const val COLUMN_REP_COUNT = "count"

        const val COLUMN_DATE_MILLIS = "date_millis"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createActivitiesId = """
            CREATE TABLE IF NOT EXISTS '$TABLE_ACTIVITIES_ID' (
                '$COLUMN_ACT_ID' INTEGER NOT NULL UNIQUE,
                '$COLUMN_NAME' TEXT NOT NULL,
                PRIMARY KEY('$COLUMN_ACT_ID')
            );
            """.trimIndent()

        val createRepsId = """
            CREATE TABLE IF NOT EXISTS '$TABLE_REPS_ID' (
                '$COLUMN_UUID' INTEGER NOT NULL,
                '$COLUMN_REP_ID' INTEGER NOT NULL,
                '$COLUMN_REP_COUNT' INTEGER,
                PRIMARY KEY('$COLUMN_UUID', '$COLUMN_REP_ID')
            );
            """.trimIndent()

        val createActivities = """
            CREATE TABLE IF NOT EXISTS '$TABLE_ACTIVITIES' (
                '$COLUMN_ACT_ID' INTEGER NOT NULL,
                '$COLUMN_DATE_MILLIS' INTEGER NOT NULL,
                '$COLUMN_UUID' TEXT NOT NULL,
                PRIMARY KEY('$COLUMN_ACT_ID', '$COLUMN_DATE_MILLIS'),
                FOREIGN KEY('$COLUMN_UUID') REFERENCES '$TABLE_REPS_ID'('$COLUMN_UUID')
            )
        """.trimIndent()

        db?.execSQL(createActivitiesId)
        db?.execSQL(createRepsId)
        db?.execSQL(createActivities)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    private fun addActivity(
        name: String,
    ) {
        val db = this.writableDatabase

        val addActivityTable = """
            INSERT INTO '$TABLE_ACTIVITIES_ID' ('$COLUMN_NAME')
            VALUES ('$name')
        """.trimIndent()

        db.execSQL(addActivityTable)
    }

    private fun findActivityId(
        name: String,
    ): Int? {
        val db = this.readableDatabase

        val selectId = """
            SELECT $COLUMN_ACT_ID
            FROM $TABLE_ACTIVITIES_ID
            WHERE $COLUMN_NAME = '$name'
        """.trimIndent()

        val cursor = db.rawQuery(selectId, null)

        var id: Int? = null

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return id
    }

    private fun addRepsToTable(
        activity: ActivityDataClass
    ) {
        val db = this.writableDatabase

        for (index in activity.reps.indices) {
            val addReps = """
                INSERT INTO '$TABLE_REPS_ID' ('$COLUMN_UUID', '$COLUMN_REP_ID', '$COLUMN_REP_COUNT') 
                VALUES ('${activity.repUUID}', '$index', '${activity.reps[index]}');
            """.trimIndent()

            db.execSQL(addReps)
        }
    }

    fun addActivity(
        activity: ActivityDataClass,
    ) {
        var lastId = findActivityId(activity.name)
        while (lastId == null) {
            addActivity(activity.name)
            lastId = findActivityId(activity.name)
        }

        addRepsToTable(activity)

        val db = this.writableDatabase

        val addActivity = """
            INSERT INTO '$TABLE_ACTIVITIES' ('$COLUMN_ACT_ID', '$COLUMN_DATE_MILLIS', '$COLUMN_UUID')
            VALUES ($lastId, '${activity.date}', '${activity.repUUID}')
        """.trimIndent()

        db?.execSQL(addActivity)
    }

    fun getAllActivitiesNames(): List<String> {
        val db = this.readableDatabase
        val list = mutableListOf<String>()

        val selectNames = """
            SELECT $COLUMN_NAME
            FROM $TABLE_ACTIVITIES_ID
        """.trimIndent()

        val cursor = db.rawQuery(selectNames, null)

        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    private fun getActivities(
        query: String,
    ): List<ActivityDataClass> {
        val db = this.readableDatabase
        val list = mutableListOf<ActivityDataClass>()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val activity = ActivityDataClass(
                    name = cursor.getString(0),
                    date = cursor.getLong(1),
                    reps = getActivitiesReps(cursor.getString(2))
                )

                list.add(activity)
            } while (cursor.moveToNext())
        }

        cursor.close()

        return list
    }

    private fun getActivitiesReps(
        uuid: String
    ): List<String> {
        val db = this.readableDatabase
        val reps = mutableListOf<String>()

        val selectReps = """
            SELECT $COLUMN_REP_ID, $COLUMN_REP_COUNT
            FROM $TABLE_REPS_ID
            WHERE $COLUMN_UUID = '$uuid'
        """.trimIndent()

        val cursor = db.rawQuery(selectReps, null)

        if (cursor.moveToFirst()) {
            do {
                reps.add(
                    index = cursor.getInt(0),
                    element = cursor.getString(1)
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return reps
    }

    fun getAllNewestActivities(): List<ActivityDataClass> {
        return getActivities(
            """
            SELECT $TABLE_ACTIVITIES_ID.$COLUMN_NAME,
                MAX($TABLE_ACTIVITIES.$COLUMN_DATE_MILLIS) AS date_mil,
                $TABLE_ACTIVITIES.$COLUMN_UUID
            FROM $TABLE_ACTIVITIES_ID INNER JOIN $TABLE_ACTIVITIES
            ON $TABLE_ACTIVITIES_ID.$COLUMN_ACT_ID = $TABLE_ACTIVITIES.$COLUMN_ACT_ID
            GROUP BY $TABLE_ACTIVITIES_ID.$COLUMN_NAME
            ORDER BY date_mil DESC 
        """.trimIndent()
        )
    }

    fun getAllActivityDates(name: String?): List<ActivityDataClass> {
        if (name == null) return listOf()

        return getActivities(
            """
            SELECT $TABLE_ACTIVITIES_ID.$COLUMN_NAME,
                $TABLE_ACTIVITIES.$COLUMN_DATE_MILLIS AS date_mil,
                $TABLE_ACTIVITIES.$COLUMN_UUID
            FROM $TABLE_ACTIVITIES_ID INNER JOIN $TABLE_ACTIVITIES
            ON $TABLE_ACTIVITIES_ID.$COLUMN_ACT_ID = $TABLE_ACTIVITIES.$COLUMN_ACT_ID
            WHERE $TABLE_ACTIVITIES_ID.$COLUMN_NAME = '$name'
            ORDER BY date_mil DESC 
        """.trimIndent()
        )
    }

    fun removeDate(name: String?, date: Long?): Boolean {
        if (name == null || date == null) return false

        val db = this.writableDatabase

        val getUUID = """
            SELECT $COLUMN_UUID
            FROM $TABLE_ACTIVITIES_ID INNER JOIN $TABLE_ACTIVITIES
            WHERE $COLUMN_DATE_MILLIS = $date AND $COLUMN_NAME = '$name'
        """.trimIndent()

        var cursor = db.rawQuery(getUUID, null)
        var uuid = ""

        if (cursor.moveToFirst()) {
            do {
                uuid = cursor.getString(0)
            } while (cursor.moveToNext())
        }

        cursor.close()

        val remove = """
            DELETE FROM $TABLE_ACTIVITIES
            WHERE ROWID IN (
                SELECT act.ROWID
                FROM $TABLE_ACTIVITIES_ID id inner join $TABLE_ACTIVITIES act
                ON id.activity_id = act.activity_id
                WHERE id.name = '$name' AND act.date_millis = $date
                )
        """.trimIndent()

        val removeReps = """
            DELETE FROM $TABLE_REPS_ID
            WHERE $COLUMN_UUID = '$uuid'
        """.trimIndent()

        db.execSQL(remove)
        db.execSQL(removeReps)

        val get = """
            SELECT *
            FROM $TABLE_ACTIVITIES_ID INNER JOIN $TABLE_ACTIVITIES
            WHERE $COLUMN_NAME = '$name'
        """.trimIndent()

        cursor = db.rawQuery(get, null)

        if (cursor.moveToFirst()) {
            cursor.close()
            return false
        }

        cursor.close()

        val removeFromActivityId = """
            DELETE FROM $TABLE_ACTIVITIES_ID
            WHERE $COLUMN_NAME = '$name'
        """.trimIndent()

        db.execSQL(removeFromActivityId)

        return true
    }
}