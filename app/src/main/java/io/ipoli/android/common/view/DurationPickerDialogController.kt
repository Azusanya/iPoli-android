package io.ipoli.android.common.view

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.sdsmdg.harjot.crollerTest.Croller
import io.ipoli.android.R
import io.ipoli.android.common.text.DurationFormatter.formatShort
import kotlinx.android.synthetic.main.dialog_duration_picker.view.*
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */

class DurationPickerDialogController : BaseDialogController {
    interface DurationPickedListener {
        fun onDurationPicked(minutes: Int)
    }

    private var listener: DurationPickedListener? = null
    private var selectedDuration: Int? = null

    constructor(listener: DurationPickedListener, selectedDuration: Int? = null) : this() {
        this.listener = listener
        this.selectedDuration = selectedDuration
    }

    constructor(args: Bundle? = null) : super(args)

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_duration_picker, null)

        val croller = contentView.croller
        croller.max = 2 * 12
        croller.setOnProgressChangedListener { progress ->
            Timber.d("AAA $progress")
            val minutes = progress * 5
            croller.label = formatShort(minutes)

        }

        return AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle("Pick duration")
            .setIcon(R.drawable.pet_5_head)
            .setPositiveButton(R.string.dialog_ok, { _, _ ->
                listener!!.onDurationPicked(croller.progress)
            })
            .setNegativeButton(R.string.cancel, null)
            .create()
    }
}