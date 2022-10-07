package com.tts.gueststar.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.com.relevantsdk.sdk.models.GetSurveyResponse
import app.com.relevantsdk.sdk.models.SubmitSurveyRequest
import com.tts.gueststar.R
import com.tts.gueststar.fragments.contact.SurveyFragment
import com.tts.gueststar.ui.support.SurveyPresenter
import kotlinx.android.synthetic.main.inflater_multiple_choice.view.*

class SurveyMultipleChoiceAdapter(val context: Context,
                                  private val listChoices: List<GetSurveyResponse.Survey.Question.QuestionChoice>,
                                  private val questionId: Int,
                                  private val answersList: MutableList<SubmitSurveyRequest.Answer>,
                                  private val presenter: SurveyPresenter,
                                  private val commentsIds: List<Int>) : RecyclerView.Adapter<SurveyMultipleChoiceAdapter.ChoicesViewHolder>() {

    val selectedIds =  emptyList<Int>().toMutableList()


    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ChoicesViewHolder {
        return ChoicesViewHolder(LayoutInflater.from(context).inflate(R.layout.inflater_multiple_choice, viewGroup, false))
    }

    override fun getItemCount(): Int {
       return listChoices.size
    }

    override fun onBindViewHolder(holder: ChoicesViewHolder, position: Int) {
        holder.init(listChoices[position])



        holder.cbChoice.tag = position

        holder.cbChoice.setOnCheckedChangeListener { _, isChecked ->
            listChoices.forEachIndexed{i, _ ->
                if (i == holder.cbChoice.tag)
                if(isChecked){
                    selectedIds.add(listChoices[position].id)
                } else {
                    selectedIds.remove(listChoices[position].id)
                }
            }
            SurveyFragment().setSelectedChoices(questionId, selectedIds, answersList, presenter, commentsIds )
        }


    }

    class ChoicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val cbChoice = itemView.cbMultipleChoice
        val etChoiceValue = itemView.tvMultipleChoiceValue


        fun init(choice: GetSurveyResponse.Survey.Question.QuestionChoice){
            etChoiceValue.text = choice.label
        }

    }
}