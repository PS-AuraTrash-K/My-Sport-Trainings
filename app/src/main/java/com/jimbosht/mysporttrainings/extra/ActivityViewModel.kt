package com.jimbosht.mysporttrainings.extra

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jimbosht.mysporttrainings.activities.AddActivity
import com.jimbosht.mysporttrainings.database.SQLite
import java.util.*


data class ActivityDataClass(val name: String, val date: Long, val reps: List<String>) {
    val repUUID = UUID.randomUUID().toString()
}


class ActivityViewModel : ViewModel() {
    private val _name = MutableLiveData("")
    private val _date = MutableLiveData<Long>(0)
    private val _count = MutableLiveData(1)
    private val _list = MutableLiveData<MutableList<String?>>(mutableListOf(""))
    private val _isSaving = MutableLiveData(false)
    private val _invalidNames = MutableLiveData<List<String>?>(null)

    val name: LiveData<String> = _name
    val date: LiveData<Long> = _date
    val count: LiveData<Int> = _count
    val isSaving: LiveData<Boolean> = _isSaving
    val invalidNames: LiveData<List<String>?> = _invalidNames

    fun setName(name: String) {
        _name.value = name
    }

    fun initInvalidNames(context: Context?) {
        if (_invalidNames.value != null) return

        val db = SQLite(context)
        _invalidNames.value = db.getAllActivitiesNames()
    }

    fun setSaving(state: Boolean) {
        _isSaving.value = state
    }

    fun setDate(date: Long) {
        _date.value = date
    }

    fun incrementCountBy(count: Int) {
        _count.value = _count.value!! + count
        reInitList()
    }

    fun findDates(name: String, date: Long, context: Context?): Boolean {
        val db = SQLite(context)
        for (d in db.getAllActivityDates(name).map {
            it.date
        }) {
            if (date == d) {
                return true
            }
        }
        return false
    }

    private fun reInitList() {
        val listCount = _list.value!!.size
        if (listCount > count.value!!) {
            _list.value!!.removeAt(count.value!!)
        } else {
            _list.value!!.add("")
        }
    }

    fun setListElem(index: Int, elem: String) =
        _list.value?.set(index, elem)


    fun addToDB(context: Context?) {
        val db = SQLite(context)

        val activity = ActivityDataClass(name.value!!, _date.value!!, _list.value!!.filterNotNull())
        db.addActivity(activity)

        initInvalidNames(context)
    }


    override fun toString(): String {
        var message = """
            Name: ${quotesIfEmpty(name.value)}
            Date: ${DateHelper.convertMillisToDate(_date.value!!)}
            Date (Millis): ${quotesIfEmpty(_date.value?.toString())}
            Count: ${count.value}
        """.trimIndent()

        for (i in _list.value!!.indices) {
            message += "\nList[$i]: ${quotesIfEmpty(_list.value!![i])}"
        }
        return message
    }

    private fun quotesIfEmpty(text: String?): String? {
        return text?.ifEmpty { "\"\"" }
    }

    fun isNameEmpty(): Boolean = name.value?.isEmpty() == true
    fun isDateEmpty(): Boolean = _date.value?.equals(0L) ?: true
    fun isIndexEmpty(index: Int): Boolean = _list.value?.get(index)?.isEmpty() == true
    fun isIndexLast(index: Int): Boolean {
        return (index == (_list.value?.count()?.minus(1) ?: false)) or (index == 2)
    }

    private fun isListInvalid(): Boolean {
        for (elem in _list.value!!.filterNotNull()) {
            if (elem.isEmpty() or (elem.length > AddActivity.MAX_CHAR_LIMIT_IN_REPS))
                return true
        }
        return false
    }

    fun isInvalid(): Boolean {
        return isNameEmpty() or isDateEmpty() or isListInvalid()
    }
}