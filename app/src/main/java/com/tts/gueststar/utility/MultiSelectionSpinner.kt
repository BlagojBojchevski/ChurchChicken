package com.tts.gueststar.utility

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.widget.ArrayAdapter
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.util.AttributeSet
import android.view.View
import android.widget.SpinnerAdapter
import android.widget.TextView
import app.com.relevantsdk.sdk.models.GetSurveyResponse
import app.com.relevantsdk.sdk.models.SubmitSurveyRequest
import com.tts.gueststar.R
import com.tts.gueststar.fragments.contact.SurveyFragment
import com.tts.gueststar.ui.support.SurveyPresenter
import java.util.*


class MultiSelectionSpinner : androidx.appcompat.widget.AppCompatSpinner, OnMultiChoiceClickListener {
    private var items: Array<String>? = null
    private var values: Array<String>? = null
    private var mSelection: BooleanArray? = null
    private var answersList: MutableList<SubmitSurveyRequest.Answer> = emptyList<SubmitSurveyRequest.Answer>().toMutableList()
    private lateinit var questions: List<GetSurveyResponse.Survey.Question>
    private lateinit var presenter: SurveyPresenter
    private lateinit var commentsIds: List<Int>
    private var questionType = ""
    private var rowOfQuestion: Int = 0
    private var hintText: TextView? = null
    private var questionId: Int = 0

    private var simpleAdapter: ArrayAdapter<String>

//    val selectedStrings: List<String>
//        get() {
//            val selection = LinkedList<String>()
//            for (i in items!!.indices) {
//                if (mSelection!![i]) {
//                    selection.add(items!![i])
//                }
//            }
//            return selection
//        }

//    val selectedIndicies: List<Int>
//        get() {
//            val selection = LinkedList<Int>()
//            for (i in values!!.indices) {
//                if (mSelection!![i]) {
//                    selection.add(i)
//                }
//            }
//            return selection
//        }

    // public String getSelectedItemsAsString() {
    // StringBuilder sb = new StringBuilder();
    // boolean foundOne = false;
    //
    // for (int i = 0; i < items.length; ++i) {
    // if (mSelection[i]) {
    // if (foundOne) {
    // sb.append(", ");
    // }
    // foundOne = true;
    // sb.append(items[i]);
    // }
    // }
    // return sb.toString();
    // }

//    val selectedItemsAsString: String
//        get() {
//            val sb = StringBuilder()
//            var foundOne = false
//
//            for (i in items!!.indices) {
//                if (mSelection!![i]) {
//                    if (foundOne) {
//                        sb.append("-")
//                    }
//                    foundOne = true
//                    sb.append(values!![i])
//                }
//            }
//
//            return sb.toString()
//        }

    constructor(context: Context) : super(context) {

        simpleAdapter = ArrayAdapter(
            context,
            R.layout.inflater_spinner_multiple_choice
        )
        super.setAdapter(simpleAdapter)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        simpleAdapter = object : ArrayAdapter<String>(
            context,
            R.layout.inflater_spinner_multiple_choice
        ) {
        }

        super.setAdapter(simpleAdapter)
    }

    override fun onClick(dialog: DialogInterface, which: Int, isChecked: Boolean) {
        if (mSelection != null && which < mSelection!!.size) {
            mSelection!![which] = isChecked

            simpleAdapter.clear()
            simpleAdapter.add(buildSelectedItemString())
        } else {
            throw IllegalArgumentException(
                "Argument 'which' is out of bounds."
            )
        }

        if (buildSelectedItemString().trim { it <= ' ' } != "") {

            val selectedIds = emptyArray<Int>().toMutableList()
            for (i in values!!.indices) {
                if (mSelection!![i]) {
                    val chosenId = values!![i].toInt()
                    selectedIds.add(chosenId)
                }
            }
            SurveyFragment().setSelectedChoices(questionId,selectedIds, answersList, presenter, commentsIds)
        } else {
            //SurveyFragment.answersId[rowOfQuestion] = ""
        }

//        SurveyFragment.activateButtonSubmit(
//            questions, questionType,
//            buildSelectedItemString()
//        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun performClick(): Boolean {
        val builder = AlertDialog.Builder(context)
        builder.setMultiChoiceItems(items, mSelection, this)
        // builder.show();
        // return true;
        builder.setPositiveButton(
            "OK"
        ) { _, _ -> hintText!!.visibility = View.GONE }

        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (buildSelectedItemString().trim { it <= ' ' } != "")
                hintText!!.visibility = View.GONE
            else
                hintText!!.visibility = View.VISIBLE


            dialog.dismiss()
        }

        return true
    }

    override fun setAdapter(adapter: SpinnerAdapter) {
        throw RuntimeException(
            "setAdapter is not supported by MultiSelectSpinner."
        )
    }

    fun setItems(
        items: Array<String>, values: Array<String>,
        questions: List<GetSurveyResponse.Survey.Question>,
        questionType: String,
        rowOfQuestion: Int,
        hintText: TextView,
        questionId: Int,
        answersList: MutableList<SubmitSurveyRequest.Answer>,
        presenter: SurveyPresenter,
        commentsIds: List<Int>
    ) {
        this.items = items
        this.values = values
        this.questions = questions
        this.questionType = questionType
        this.rowOfQuestion = rowOfQuestion
        this.hintText = hintText
        this.questionId = questionId
        this.answersList = answersList
        this.presenter = presenter
        this.commentsIds = commentsIds
        mSelection = BooleanArray(this.items!!.size)
        simpleAdapter.clear()
        // simpleAdapter.add(items[0]);
        Arrays.fill(mSelection!!, false)
    }

//    fun setItems(items: List<String>) {
//        this.items = items.toTypedArray()
//        mSelection = BooleanArray(this.items!!.size)
//        simpleAdapter.clear()
//        simpleAdapter.add(this.items!![0])
//        Arrays.fill(mSelection!!, false)
//    }
//
//    fun setSelection(selection: Array<String>) {
//        for (cell in selection) {
//            for (j in items!!.indices) {
//                if (items!![j] == cell) {
//                    mSelection!![j] = true
//                }
//            }
//        }
//    }
//
//    fun setSelection(selection: List<String>) {
//        for (i in mSelection!!.indices) {
//            mSelection!![i] = false
//        }
//        for (sel in selection) {
//            for (j in items!!.indices) {
//                if (items!![j] == sel) {
//                    mSelection!![j] = true
//                }
//            }
//        }
//        simpleAdapter.clear()
//        simpleAdapter.add(buildSelectedItemString())
//    }

    override fun setSelection(index: Int) {
        for (i in mSelection!!.indices) {
            mSelection!![i] = false
        }
        if (index >= 0 && index < mSelection!!.size) {
            mSelection!![index] = true
            hintText!!.visibility = View.GONE
        } else {
            throw IllegalArgumentException(
                ("Index " + index
                        + " is out of bounds.")
            )
        }
        simpleAdapter.clear()
        simpleAdapter.add(buildSelectedItemString())
    }

//    fun setSelection(selectedIndicies: IntArray) {
//        for (i in mSelection!!.indices) {
//            mSelection!![i] = false
//        }
//        for (index in selectedIndicies) {
//            if (index >= 0 && index < mSelection!!.size) {
//                mSelection!![index] = true
//            } else {
//                throw IllegalArgumentException(
//                    ("Index " + index
//                            + " is out of bounds.")
//                )
//            }
//        }
//        simpleAdapter.clear()
//        simpleAdapter.add(buildSelectedItemString())
//    }

    private fun buildSelectedItemString(): String {
        val sb = StringBuilder()
        var foundOne = false

        for (i in items!!.indices) {
            if (mSelection!![i]) {
                if (foundOne) {
                    sb.append(", ")
                }
                foundOne = true

                sb.append(items!![i])
            }
        }
        return sb.toString()
    }
}