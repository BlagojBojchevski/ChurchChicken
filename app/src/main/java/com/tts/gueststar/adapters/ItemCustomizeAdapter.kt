package com.tts.gueststar.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.SpannedString
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.ButtonCheckChanges
import com.tts.gueststar.interfaces.ButtonCheckChangesSubModifiers
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.Modifier
import com.tts.olosdk.models.Option
import com.tts.olosdk.models.Optiongroup
import kotlinx.android.synthetic.main.order_item_modifier_cb.view.*
import kotlinx.android.synthetic.main.order_item_modifier_group.view.*
import kotlinx.android.synthetic.main.order_item_modifier_rb.view.*


class ItemCustomizeAdapter(
    val list: List<ItemCustomizeView>,
    val context: Context,
    private val listener: ButtonCheckChanges,
    private val listenerSub: ButtonCheckChangesSubModifiers,
    private val currentPositiion: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface ItemCustomizeView
    class TitleContainer(val item: Optiongroup, val parentId: Int) : ItemCustomizeView
    class ItemCustomizeRadioButton(
        val mod: Optiongroup,
        val item: Option,
        val parentId: Int
    ) : ItemCustomizeView

    class ItemCustomizeCheckButton(
        val mod: Optiongroup,
        val item: Option,
        val parentId: Int
    ) : ItemCustomizeView

    class TitleContainerSubModifier(val itm: Option, val item: Modifier, val parentId: Long) :
        ItemCustomizeView

    class ItemCustomizeRadioButtonSubModifier(
        val mod: Modifier,
        val item: Option,
        val parentId: Long,
        val itm: Option
    ) : ItemCustomizeView

    class ItemCustomizeCheckButtonSubModifier(
        val mod: Modifier,
        val item: Option,
        val parentId: Long,
        val itm: Option
    ) : ItemCustomizeView

    companion object {
        const val TYPE_TITLE = 1
        const val TYPE_RADIO_BUTTON = 2
        const val TYPE_CHECK_BOX = 3

        const val TYPE_TITLE_SUB = 4
        const val TYPE_RADIO_BUTTON_SUB = 5
        const val TYPE_CHECK_BOX_SUB = 6

        var selectedItem = -1
    }

    override fun getItemId(position: Int): Long =
        when (list[position]) {
            is TitleContainer -> TYPE_TITLE.toLong()
            is ItemCustomizeRadioButton -> TYPE_RADIO_BUTTON.toLong()
            is ItemCustomizeCheckButton -> TYPE_CHECK_BOX.toLong()
            is TitleContainerSubModifier -> TYPE_TITLE_SUB.toLong()
            is ItemCustomizeRadioButtonSubModifier -> TYPE_RADIO_BUTTON_SUB.toLong()
            is ItemCustomizeCheckButtonSubModifier -> TYPE_CHECK_BOX_SUB.toLong()
            else -> throw IllegalArgumentException()
        }


    override fun getItemViewType(position: Int): Int =
        when (list[position]) {
            is TitleContainer -> TYPE_TITLE
            is ItemCustomizeRadioButton -> TYPE_RADIO_BUTTON
            is ItemCustomizeCheckButton -> TYPE_CHECK_BOX
            is TitleContainerSubModifier -> TYPE_TITLE_SUB
            is ItemCustomizeRadioButtonSubModifier -> TYPE_RADIO_BUTTON_SUB
            is ItemCustomizeCheckButtonSubModifier -> TYPE_CHECK_BOX_SUB
            else -> throw IllegalArgumentException()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_TITLE -> TitleHolder(
            LayoutInflater.from(context).inflate(
                R.layout.order_item_modifier_group,
                parent,
                false
            )
        )
        TYPE_RADIO_BUTTON -> RadioButtonHolder(
            LayoutInflater.from(context).inflate(
                R.layout.order_item_modifier_rb,
                parent,
                false
            )
        )
        TYPE_CHECK_BOX -> CheckBoxHolder(
            LayoutInflater.from(context).inflate(
                R.layout.order_item_modifier_cb,
                parent,
                false
            )
        )

        TYPE_TITLE_SUB -> TitleHolderSub(
            LayoutInflater.from(context).inflate(
                R.layout.order_item_modifier_group,
                parent,
                false
            )
        )
        TYPE_RADIO_BUTTON_SUB -> RadioButtonHolder(
            LayoutInflater.from(context).inflate(
                R.layout.order_item_modifier_rb,
                parent,
                false
            )
        )
        TYPE_CHECK_BOX_SUB -> CheckBoxHolder(
            LayoutInflater.from(context).inflate(
                R.layout.order_item_modifier_cb,
                parent,
                false
            )
        )
        else -> throw IllegalArgumentException()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_TITLE -> onBindTitle(holder, list[position] as TitleContainer)
            TYPE_RADIO_BUTTON -> onBindRadioButtonView(
                holder,
                list[position] as ItemCustomizeRadioButton
            )
            TYPE_CHECK_BOX -> onBindCheckView(
                holder,
                list[position] as ItemCustomizeCheckButton
            )

            TYPE_TITLE_SUB -> onBindTitleSub(
                holder,
                list[position] as TitleContainerSubModifier
            )
            TYPE_RADIO_BUTTON_SUB -> onBindRadioButtonViewSub(
                holder,
                list[position] as ItemCustomizeRadioButtonSubModifier,
                position
            )
            TYPE_CHECK_BOX_SUB -> onBindCheckViewSub(
                holder,
                list[position] as ItemCustomizeCheckButtonSubModifier
            )
            else -> throw IllegalArgumentException()
        }
        if (currentPositiion == -1)
            selectedItem = currentPositiion
    }

    private fun onBindTitle(
        holder: RecyclerView.ViewHolder,
        titleContainer: TitleContainer
    ) {
        holder as TitleHolder
        holder.init(titleContainer.item)
    }

    private fun onBindTitleSub(
        holder: RecyclerView.ViewHolder,
        titleContainer: TitleContainerSubModifier
    ) {
        holder as TitleHolderSub
        holder.initSub(titleContainer.itm, titleContainer.item)
    }

    private fun onBindRadioButtonView(
        holder: RecyclerView.ViewHolder,
        radioButton: ItemCustomizeRadioButton
    ) {
        val radioButtonHolder = holder as RadioButtonHolder
        radioButtonHolder.init(radioButton.mod, radioButton.item, listener)
    }

    private fun onBindRadioButtonViewSub(
        holder: RecyclerView.ViewHolder,
        radioButton: ItemCustomizeRadioButtonSubModifier, position: Int
    ) {
        val radioButtonHolder = holder as RadioButtonHolder
        radioButtonHolder.initSub(
            radioButton.item,
            radioButton.mod,
            listenerSub
        )
    }

    private fun onBindCheckView(
        holder: RecyclerView.ViewHolder,
        checkBox: ItemCustomizeCheckButton
    ) {
        val checkBoxHolder = holder as CheckBoxHolder
        checkBoxHolder.init(checkBox.mod, checkBox.item, listener)
    }

    private fun onBindCheckViewSub(
        holder: RecyclerView.ViewHolder,
        checkBox: ItemCustomizeCheckButtonSubModifier
    ) {

        val checkBoxHolder = holder as CheckBoxHolder
        checkBoxHolder.initSub(checkBox.item, checkBox.mod, listenerSub)
    }

    class TitleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title = itemView.modifier_group_title!!

        fun init(group: Optiongroup) {
            title.text = group.description
        }
    }

    class TitleHolderSub(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title = itemView.modifier_group_title!!

        @SuppressLint("SetTextI18n")
        fun initSub(
            option: Option,
            group: Modifier
        ) {
         //   title.text = option.name + ": " + group.description

            val s = SpannableStringBuilder()
                .append( option.name + ": ")
                .bold { append(group.description) }
            title.text = s
//            val string: SpannedString = buildSpannedString {
//                bold {
//                    append("foo")
//                }
//                append("bar")
//            }
        }
    }

    class RadioButtonHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var radioBtn = itemView.modifier_rb!!
        private var modifierName = itemView.modifier_name!!
        private var caloric = itemView.modifier_caloric!!
        private var modifierPrice = itemView.modifier_price!!

        @SuppressLint("SetTextI18n")
        fun init(
            group: Optiongroup,
            modifier: Option,
            listener: ButtonCheckChanges
        ) {
            modifierName.text = modifier.name
            if (!group.hidechoicecost)
                modifierPrice.text = "+$" + String.format("%.2f", modifier.cost)
            else {
                modifierPrice.visibility = View.GONE
            }

            if (OrderHelper.showCalories) {
                if (modifier.caloriesseparator != null) {
                    caloric.visibility = View.VISIBLE
                    caloric.text =
                        modifier.basecalories + modifier.caloriesseparator + modifier.maxcalories + " Cal"
                } else {
                    if (modifier.basecalories != null) {
                        caloric.visibility = View.VISIBLE
                        caloric.text = modifier.basecalories + " Cal"
                    }
                }
            } else {
                caloric.visibility = View.GONE
            }


            if (modifier.id in OrderHelper.selectedModifiersIds) {
                if (OrderHelper.checkedRadioGruopWithModifiers[group.id] == modifier.id) {
                    radioBtn.isChecked = true

                    //edit
                    if (OrderHelper.fromEdit) {
                        if (modifier.id in OrderHelper.TMPselectedModifiersIds) {
                            if (modifier.basecalories != null) {
                                OrderHelper.currentCalories += modifier.basecalories!!.toLong()
                            }
                            OrderHelper.currentPrice += modifier.cost
                            OrderHelper.TMPselectedModifiersIds.remove(modifier.id)
                            listener.onRadioBtnCheckChanges(
                                group,
                                modifier,
                                false
                            )
                        }
                        radioBtn.isChecked = true
                        listener.updatePrice()
                    }
                } else {
                    OrderHelper.selectedModifiersIds.remove(modifier.id)
                    if (modifier.basecalories != null) {
                        OrderHelper.currentCalories -= modifier.basecalories!!.toLong()
                    }
                    OrderHelper.currentPrice -= modifier.cost
                    radioBtn.isChecked = false
                    listener.updatePrice()
                }
            } else {
                if (!OrderHelper.checkedRadioGruopWithModifiers.containsKey(group.id)) {
                    if (modifier.isdefault) {
                        if (modifier.basecalories != null) {
                            OrderHelper.currentCalories += modifier.basecalories!!.toLong()
                        }
                        OrderHelper.currentPrice += modifier.cost
                        radioBtn.isChecked = true
                        OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                        OrderHelper.selectedModifiersIds.add(modifier.id)
                        listener.onRadioBtnCheckChanges(
                            group,
                            modifier,
                            true
                        )
                    }
                } else {
//                    if (!OrderHelper.fromEdit)
                    radioBtn.isChecked = false
//                    else {
//                        if (modifier.id in OrderHelper.selectedModifiersIds) {
//                            radioBtn.isChecked = true
//                        }
//                    }

                }
            }
            itemView.setOnClickListener {
                OrderHelper.fromEdit = false
                if (modifier.id !in OrderHelper.selectedModifiersIds) {
                    if (modifier.basecalories != null) {
                        OrderHelper.currentCalories += modifier.basecalories!!.toLong()
                    }
                    OrderHelper.currentPrice += modifier.cost
                    OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                    radioBtn.isChecked = true
                    OrderHelper.selectedModifiersIds.add(modifier.id)
                    //     if (!radioBtn.isChecked)
                    listener.onRadioBtnCheckChanges(
                        group,
                        modifier,
                        false
                    )
                }
            }
            radioBtn.setOnClickListener {
                OrderHelper.fromEdit = false
                if (modifier.id !in OrderHelper.selectedModifiersIds) {
                    if (modifier.basecalories != null) {
                        OrderHelper.currentCalories += modifier.basecalories!!.toLong()
                    }
                    OrderHelper.currentPrice += modifier.cost
                    OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                    radioBtn.isChecked = true
                    OrderHelper.selectedModifiersIds.add(modifier.id)
                    //     if (!radioBtn.isChecked)
                    listener.onRadioBtnCheckChanges(
                        group,
                        modifier,
                        false
                    )
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun initSub(
            modifier: Option,
            group: Modifier,
            listener: ButtonCheckChangesSubModifiers
        ) {
            modifierName.text = modifier.name
            modifierPrice.text = "+$" + String.format("%.2f", modifier.cost)

            if (OrderHelper.showCalories) {
                if (modifier.caloriesseparator != null) {
                    caloric.visibility = View.VISIBLE
                    caloric.text =
                        modifier.basecalories + modifier.caloriesseparator + modifier.maxcalories + " Cal"
                } else {
                    if (modifier.basecalories != null) {
                        caloric.visibility = View.VISIBLE
                        caloric.text = modifier.basecalories + " Cal"
                    }
                }
            } else {
                caloric.visibility = View.GONE
            }

            if (modifier.id in OrderHelper.selectedModifiersIds) {
                if (OrderHelper.checkedRadioGruopWithModifiers.get(group.id) == modifier.id) {
                    radioBtn.isChecked = true

                    if (OrderHelper.fromEdit) {
                        if (modifier.id in OrderHelper.TMPselectedModifiersIds) {
                            if (modifier.basecalories != null) {
                                OrderHelper.currentCalories += modifier.basecalories!!.toLong()
                            }
                            OrderHelper.currentPrice += modifier.cost
                            OrderHelper.TMPselectedModifiersIds.remove(modifier.id)
                            listener.onRadioBtnSubModifiersCheckChanges(
                                group,
                                modifier,
                                false
                            )
                        }
                        radioBtn.isChecked = true
                        listener.updatePrice()
                    }

                } else {
                    // OrderHelper.checkedRadioGruopWithModifiers.remove(group.id,modifier.id)
                    if (modifier.basecalories != null) {
                        OrderHelper.currentCalories -= modifier.basecalories!!.toLong()
                    }
                    OrderHelper.currentPrice -= modifier.cost
                    OrderHelper.selectedModifiersIds.remove(modifier.id)
                    radioBtn.isChecked = false
                    listener.updatePrice()
                }
            } else {
                if (!OrderHelper.checkedRadioGruopWithModifiers.containsKey(group.id)) {
                    if (modifier.isdefault) {
                        if (modifier.basecalories != null) {
                            OrderHelper.currentCalories += modifier.basecalories!!.toLong()
                        }
                        OrderHelper.currentPrice += modifier.cost
                        radioBtn.isChecked = true
                        OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                        OrderHelper.selectedModifiersIds.add(modifier.id)
                        listener.onRadioBtnSubModifiersCheckChanges(
                            group,
                            modifier,
                            true
                        )
                    }
                } else {
//                    if (!OrderHelper.fromEdit)
                    radioBtn.isChecked = false
//                    else
//                        if (modifier.id in OrderHelper.selectedModifiersIds) {
//                            radioBtn.isChecked = true
//                        }
                }
            }
            itemView.setOnClickListener {
                OrderHelper.fromEdit = false
                if (modifier.id !in OrderHelper.selectedModifiersIds) {
                    if (modifier.basecalories != null) {
                        OrderHelper.currentCalories += modifier.basecalories!!.toLong()
                    }
                    OrderHelper.currentPrice += modifier.cost
                    OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                    radioBtn.isChecked = true
                    OrderHelper.selectedModifiersIds.add(modifier.id)
                    //     if (!radioBtn.isChecked)
                    listener.onRadioBtnSubModifiersCheckChanges(
                        group,
                        modifier,
                        false
                    )
                }
            }

            radioBtn.setOnClickListener {
                OrderHelper.fromEdit = false
                if (modifier.id !in OrderHelper.selectedModifiersIds) {
                    if (modifier.basecalories != null) {
                        OrderHelper.currentCalories += modifier.basecalories!!.toLong()
                    }
                    OrderHelper.currentPrice += modifier.cost
                    OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                    radioBtn.isChecked = true
                    OrderHelper.selectedModifiersIds.add(modifier.id)
                    //     if (!radioBtn.isChecked)
                    listener.onRadioBtnSubModifiersCheckChanges(
                        group,
                        modifier,
                        false
                    )
                }
            }

        }

    }

    class CheckBoxHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var checkBox = itemView.modifier_cb!!
        private var modifierNameCb = itemView.modifier_name_cb!!
        private var caloricCb = itemView.modifier_caloric_cb!!
        private var modifierPriceCb = itemView.modifier_price_cb!!
        private var minus = itemView.minus
        private var plus = itemView.plus
        private var quantity = itemView.quantity
        private var quantity_layout = itemView.quantity_layout
        private var itemViewLayout = itemView.itemViewLayout

        @SuppressLint("SetTextI18n")
        fun init(
            group: Optiongroup,
            modifier: Option,
            listener: ButtonCheckChanges
        ) {
            modifierNameCb.text = modifier.name
            modifierPriceCb.text = "+$" + String.format("%.2f", modifier.cost)

            if (group.minaggregatequantity == null || group.minaggregatequantity == "null") {
                quantity_layout.visibility = View.GONE
            }

            if (OrderHelper.showCalories) {
                if (modifier.caloriesseparator != null) {
                    caloricCb.visibility = View.VISIBLE
                    caloricCb.text =
                        modifier.basecalories + modifier.caloriesseparator + modifier.maxcalories + " Cal"
                } else {
                    if (modifier.basecalories != null) {
                        caloricCb.visibility = View.VISIBLE
                        caloricCb.text = modifier.basecalories + " Cal"
                    }
                }
            } else {
                caloricCb.visibility = View.GONE
            }
            quantity.text = "0"
            checkBox.isChecked = false
            if (modifier.id in OrderHelper.selectedModifiersIds) {
                checkBox.isChecked = true
                if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                    var groupQuantity = 0
                    for (itm in OrderHelper.groupQuantityModifiers.keys) {
                        if (itm == group.id) {
                            groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                        }
                    }
                    var modQuantity = 0
                    for (itm in OrderHelper.quantityModifiers.keys) {
                        if (itm == modifier.id) {
                            modQuantity = OrderHelper.quantityModifiers.get(itm)!!
                        }
                    }

                    if (modQuantity == 0) {
                        quantity.text = group.minchoicequantity
                        OrderHelper.quantityModifiers.put(
                            modifier.id,
                            group.minchoicequantity!!.toInt()
                        )
                        if (modifier.id !in OrderHelper.selectedModifiersIds) {
                            OrderHelper.groupQuantityModifiers.put(
                                group.id,
                                group.minchoicequantity!!.toInt()
                            )
                        }
                    } else {
                        quantity.text = modQuantity.toString()
                        OrderHelper.quantityModifiers.put(
                            modifier.id,
                            modQuantity
                        )
                        groupQuantity += modQuantity
                        if (modifier.id !in OrderHelper.selectedModifiersIds) {
                            OrderHelper.groupQuantityModifiers.put(
                                group.id,
                                groupQuantity
                            )
                        }
                    }
                }
                //edit
                if (OrderHelper.fromEdit) {
                    if (modifier.id in OrderHelper.TMPselectedModifiersIds) {
                        //      OrderHelper.currentPrice += modifier.cost
                        OrderHelper.TMPselectedModifiersIds.remove(modifier.id)
                        listener.onCheckBoxCheck(
                            group,
                            modifier,
                            true
                        )
                        if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                            var groupQuantity = 0
                            for (itm in OrderHelper.groupQuantityModifiers.keys) {
                                if (itm == group.id) {
                                    groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                                }
                            }

                            var modQuantity = 0
                            for (itm in OrderHelper.quantityModifiers.keys) {
                                if (itm == modifier.id) {
                                    modQuantity = OrderHelper.quantityModifiers.get(itm)!!
                                }
                            }
                            if (modQuantity == 0) {
                                quantity.text = group.minchoicequantity
                                OrderHelper.quantityModifiers.put(
                                    modifier.id,
                                    group.minchoicequantity!!.toInt()
                                )

                                OrderHelper.groupQuantityModifiers.put(
                                    group.id,
                                    group.minchoicequantity!!.toInt()
                                )
                            } else {
                                quantity.text = modQuantity.toString()
                                OrderHelper.quantityModifiers.put(
                                    modifier.id,
                                    modQuantity
                                )
                                groupQuantity += modQuantity
                                OrderHelper.groupQuantityModifiers.put(
                                    group.id,
                                    groupQuantity
                                )
                            }
                        }
                        OrderHelper.checkedCheckBoxWithModifiers.put(modifier.id, group.id)
                    }
//                    OrderHelper.checkedCheckBoxWithModifiers.put(modifier.id, group.id)
                    checkBox.isChecked = true
                }
            } else {
                if (!OrderHelper.checkedRadioGruopWithModifiers.containsKey(group.id)) {
                    if (OrderHelper.fromEdit) {
                        if (modifier.isdefault && modifier.id !in OrderHelper.selectedModifiersIds) {
                            modifier.isdefault = false
                            OrderHelper.selectedModifiersDedault.add(modifier.id)
                        }
                    }
                    if (modifier.isdefault) {
                        modifier.isdefault = false
                        OrderHelper.selectedModifiersDedault.add(modifier.id)
                        checkBox.isChecked = true
                        var groupQuantity = 0
                        for (itm in OrderHelper.groupQuantityModifiers.keys) {
                            if (itm == group.id) {
                                groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                            }
                        }
                        if (groupQuantity == 0) {
                            if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                                quantity.text = group.minchoicequantity
                                OrderHelper.quantityModifiers.put(
                                    modifier.id,
                                    group.minchoicequantity!!.toInt()
                                )
                                OrderHelper.groupQuantityModifiers.put(
                                    group.id,
                                    group.minchoicequantity!!.toInt()
                                )
                            }
                        } else {
                            if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                                groupQuantity += group.minchoicequantity!!.toInt()
                                quantity.text = group.minchoicequantity
                                OrderHelper.quantityModifiers.put(
                                    modifier.id,
                                    group.minchoicequantity!!.toInt()
                                )
                                OrderHelper.groupQuantityModifiers.put(
                                    group.id,
                                    groupQuantity
                                )
                            }
                        }
                        OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                        OrderHelper.selectedModifiersIds.add(modifier.id)
                        listener.onCheckBoxCheck(
                            group,
                            modifier,
                            true
                        )
                    }

                } else {
                    checkBox.isChecked = false
                    quantity.text = "0"
                    OrderHelper.quantityModifiers.put(
                        modifier.id,
                        0
                    )
                    if (modifier.id !in OrderHelper.selectedModifiersIds) {
                        if (OrderHelper.fromEdit) {
                            if (modifier.isdefault && modifier.id !in OrderHelper.selectedModifiersIds) {
                                modifier.isdefault = false
                                OrderHelper.selectedModifiersDedault.add(modifier.id)
                            }
                        }
                        if (modifier.isdefault) {
                            modifier.isdefault = false
                            OrderHelper.selectedModifiersDedault.add(modifier.id)
                            checkBox.isChecked = true
                            var groupQuantity = 0
                            for (itm in OrderHelper.groupQuantityModifiers.keys) {
                                if (itm == group.id) {
                                    groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                                }
                            }

                            if (groupQuantity == 0) {
                                if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                                    quantity.text = group.minchoicequantity
                                    OrderHelper.quantityModifiers.put(
                                        modifier.id,
                                        group.minchoicequantity!!.toInt()
                                    )
                                    OrderHelper.groupQuantityModifiers.put(
                                        group.id,
                                        group.minchoicequantity!!.toInt()
                                    )
                                }
                            } else {
                                if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                                    groupQuantity += group.minchoicequantity!!.toInt()
                                    quantity.text = group.minchoicequantity
                                    OrderHelper.quantityModifiers.put(
                                        modifier.id,
                                        group.minchoicequantity!!.toInt()
                                    )
                                    OrderHelper.groupQuantityModifiers.put(
                                        group.id,
                                        groupQuantity
                                    )
                                }
                            }

                            OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                            OrderHelper.selectedModifiersIds.add(modifier.id)
                            listener.onCheckBoxCheck(
                                group,
                                modifier,
                                true
                            )
                        }
                    } else {
                        checkBox.isChecked = true
                        if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                            quantity.text = group.minchoicequantity
                            OrderHelper.quantityModifiers.put(
                                modifier.id,
                                group.minchoicequantity!!.toInt()
                            )
                        }
                    }
                }
            }
            itemViewLayout.setOnClickListener {
                OrderHelper.fromEdit = false
                var count = 0
                if (modifier.id !in OrderHelper.selectedModifiersIds) {
                    OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                    OrderHelper.checkedCheckBoxWithModifiers.put(modifier.id, group.id)
                    if (group.maxselects != null) {
                        for (keys in OrderHelper.checkedCheckBoxWithModifiers.values) {
                            if (keys == group.id) {
                                count += 1
                            }
                        }
                        if (count > group.maxselects!!.toInt()) {
                            OrderHelper.checkedCheckBoxWithModifiers.remove(modifier.id)
                        } else {
                            checkBox.isChecked = true
                            OrderHelper.selectedModifiersIds.add(modifier.id)
                            listener.onCheckBoxCheck(
                                group,
                                modifier,
                                true
                            )
                            if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                                quantity.text = group.minchoicequantity
                                OrderHelper.quantityModifiers.put(
                                    modifier.id,
                                    group.minchoicequantity!!.toInt()
                                )
                            }
                        }
                    } else {
                        checkBox.isChecked = true
                        OrderHelper.selectedModifiersIds.add(modifier.id)
                        listener.onCheckBoxCheck(
                            group,
                            modifier,
                            true
                        )
                        if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                            quantity.text = group.minchoicequantity
                            OrderHelper.quantityModifiers.put(
                                modifier.id,
                                group.minchoicequantity!!.toInt()
                            )
                        }
                    }
                } else {
                    OrderHelper.checkedCheckBoxWithModifiers.remove(modifier.id)

                    var curQroupQuantity = 0
                    curQroupQuantity = quantity.text.toString().toInt()
                    var groupQuantity = 0
                    for (itm in OrderHelper.groupQuantityModifiers.keys) {
                        if (itm == group.id) {
                            groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                        }
                    }
                    groupQuantity -= curQroupQuantity
                    OrderHelper.groupQuantityModifiers.put(
                        group.id,
                        groupQuantity
                    )


                    checkBox.isChecked = false
                    quantity.text = "0"
                    OrderHelper.quantityModifiers.put(
                        modifier.id,
                        0
                    )

                    listener.onCheckBoxCheck(
                        group,
                        modifier,
                        false
                    )
                    OrderHelper.selectedModifiersIds.remove(modifier.id)
                }
            }

            checkBox.setOnClickListener {
                OrderHelper.fromEdit = false
                var count1 = 0L
                if (modifier.id !in OrderHelper.selectedModifiersIds) {
                    OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                    OrderHelper.checkedCheckBoxWithModifiers.put(modifier.id, group.id)
                    if (group.maxselects != null) {
                        for (keys in OrderHelper.checkedCheckBoxWithModifiers.values) {
                            if (keys == group.id) {
                                count1 += 1
                            }
                        }
                        if (count1 > group.maxselects!!.toLong()) {
                            checkBox.isChecked = false
                            quantity.text = "0"
                            OrderHelper.quantityModifiers.put(
                                modifier.id,
                                0
                            )
                            count1 = 0
                            OrderHelper.checkedCheckBoxWithModifiers.remove(modifier.id)
                        } else {
                            count1 = 0
                            var groupQuantity = 0
                            for (itm in OrderHelper.groupQuantityModifiers.keys) {
                                if (itm == group.id) {
                                    groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                                }
                            }
                            if (groupQuantity != group.maxaggregatequantity!!.toInt()) {
                                checkBox.isChecked = true
                                OrderHelper.selectedModifiersIds.add(modifier.id)
                                listener.onCheckBoxCheck(
                                    group,
                                    modifier,
                                    true
                                )
                                if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                                    quantity.text = group.minchoicequantity
                                    OrderHelper.quantityModifiers.put(
                                        modifier.id,
                                        group.minchoicequantity!!.toInt()
                                    )
                                    var groupQuantity = 0
                                    for (itm in OrderHelper.groupQuantityModifiers.keys) {
                                        if (itm == group.id) {
                                            groupQuantity =
                                                OrderHelper.groupQuantityModifiers.get(itm)!!
                                        }
                                    }
                                    if (groupQuantity == 0) {
                                        OrderHelper.groupQuantityModifiers.put(
                                            group.id,
                                            group.minchoicequantity!!.toInt()
                                        )
                                    } else {
                                        groupQuantity += group.minchoicequantity!!.toInt()
                                        OrderHelper.groupQuantityModifiers.put(
                                            group.id,
                                            groupQuantity
                                        )
                                    }

                                }
                            } else {
                                checkBox.isChecked = false
                            }
                        }
                    } else {
                        checkBox.isChecked = true
                        OrderHelper.selectedModifiersIds.add(modifier.id)
                        listener.onCheckBoxCheck(
                            group,
                            modifier,
                            true
                        )
                        if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                            quantity.text = group.minchoicequantity
                            OrderHelper.quantityModifiers.put(
                                modifier.id,
                                group.minchoicequantity!!.toInt()
                            )

                            var groupQuantity = 0
                            for (itm in OrderHelper.groupQuantityModifiers.keys) {
                                if (itm == group.id) {
                                    groupQuantity =
                                        OrderHelper.groupQuantityModifiers.get(itm)!!
                                }
                            }
                            if (groupQuantity == 0) {
                                OrderHelper.groupQuantityModifiers.put(
                                    group.id,
                                    group.minchoicequantity!!.toInt()
                                )
                            } else {
                                groupQuantity += group.minchoicequantity!!.toInt()
                                OrderHelper.groupQuantityModifiers.put(group.id, groupQuantity)
                            }
                        }
                    }
                } else {
                    OrderHelper.checkedCheckBoxWithModifiers.remove(modifier.id)
                    var curQroupQuantity = 0
                    curQroupQuantity = quantity.text.toString().toInt()
                    var groupQuantity = 0
                    for (itm in OrderHelper.groupQuantityModifiers.keys) {
                        if (itm == group.id) {
                            groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                        }
                    }
                    groupQuantity = groupQuantity - curQroupQuantity


                    OrderHelper.groupQuantityModifiers.put(
                        group.id,
                        groupQuantity
                    )

                    checkBox.isChecked = false
                    quantity.text = "0"
                    OrderHelper.quantityModifiers.put(modifier.id, 0)
                    listener.onCheckBoxCheck(
                        group,
                        modifier,
                        false
                    )
                    OrderHelper.selectedModifiersIds.remove(modifier.id)
                }
            }

            plus.setOnClickListener {
                if (checkBox.isChecked) {
                    var currentQuanity = quantity.text.toString().toInt()
                    currentQuanity += 1

                    var groupQuantity = 0
                    for (itm in OrderHelper.groupQuantityModifiers.keys) {
                        if (itm == group.id) {
                            groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                        }
                    }

                    if (currentQuanity <= group.maxchoicequantity.toInt() && groupQuantity < group.maxaggregatequantity!!.toInt()) {
                        quantity.text = currentQuanity.toString()
                        OrderHelper.quantityModifiers.put(
                            modifier.id,
                            currentQuanity
                        )
                        groupQuantity += 1
                        OrderHelper.groupQuantityModifiers.put(
                            group.id,
                            groupQuantity
                        )
                    } else {
                        currentQuanity -= 1
                    }
                }
            }

            minus.setOnClickListener {
                if (checkBox.isChecked) {
                    var currentQuanity = quantity.text.toString().toInt()
                    currentQuanity -= 1
                    var groupQuantity = 0
                    for (itm in OrderHelper.groupQuantityModifiers.keys) {
                        if (itm == group.id) {
                            groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                        }
                    }
                    if (currentQuanity >= group.minchoicequantity!!.toInt()) {
                        OrderHelper.quantityModifiers.put(
                            modifier.id,
                            currentQuanity
                        )
                        groupQuantity -= 1
                        OrderHelper.groupQuantityModifiers.put(
                            group.id,
                            groupQuantity
                        )
                        quantity.text = currentQuanity.toString()
                    } else {
                        currentQuanity += 1
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun initSub(
            modifier: Option,
            group: Modifier,
            listenerSub: ButtonCheckChangesSubModifiers
        ) {
            modifierNameCb.text = modifier.name
            modifierPriceCb.text = "+$" + String.format("%.2f", modifier.cost)

            if (group.minaggregatequantity == null || group.minaggregatequantity == "null") {
                quantity_layout.visibility = View.GONE
            }

            if (OrderHelper.showCalories) {
                if (modifier.caloriesseparator != null) {
                    caloricCb.visibility = View.VISIBLE
                    caloricCb.text =
                        modifier.basecalories + modifier.caloriesseparator + modifier.maxcalories + " Cal"
                } else {
                    if (modifier.basecalories != null) {
                        caloricCb.visibility = View.VISIBLE
                        caloricCb.text = modifier.basecalories + " Cal"
                    }
                }
            } else {
                caloricCb.visibility = View.GONE
            }

            checkBox.isChecked = false
            quantity.text = "0"
            if (modifier.id in OrderHelper.selectedModifiersIds) {
                checkBox.isChecked = true
                if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                    var groupQuantity = 0
                    for (itm in OrderHelper.groupQuantityModifiers.keys) {
                        if (itm == group.id) {
                            groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                        }
                    }
                    var modQuantity = 0
                    for (itm in OrderHelper.quantityModifiers.keys) {
                        if (itm == modifier.id) {
                            modQuantity = OrderHelper.quantityModifiers.get(itm)!!
                        }
                    }

                    if (modQuantity == 0) {
                        quantity.text = group.minchoicequantity
                        OrderHelper.quantityModifiers.put(
                            modifier.id,
                            group.minchoicequantity!!.toInt()
                        )
                        if (modifier.id !in OrderHelper.selectedModifiersIds) {
                            OrderHelper.groupQuantityModifiers.put(
                                group.id,
                                group.minchoicequantity!!.toInt()
                            )
                        }
                    } else {
                        quantity.text = modQuantity.toString()
                        OrderHelper.quantityModifiers.put(
                            modifier.id,
                            modQuantity
                        )
                        groupQuantity += modQuantity
                        if (modifier.id !in OrderHelper.selectedModifiersIds) {
                            OrderHelper.groupQuantityModifiers.put(
                                group.id,
                                groupQuantity
                            )
                        }
                    }
                }
                //edit
                if (OrderHelper.fromEdit) {
                    if (modifier.id in OrderHelper.TMPselectedModifiersIds) {
                        //      OrderHelper.currentPrice += modifier.cost
                        OrderHelper.TMPselectedModifiersIds.remove(modifier.id)
                        listenerSub.onCheckBoxSubModifiersCheck(
                            group,
                            modifier,
                            true
                        )
                        if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                            var groupQuantity = 0
                            for (itm in OrderHelper.groupQuantityModifiers.keys) {
                                if (itm == group.id) {
                                    groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                                }
                            }

                            var modQuantity = 0
                            for (itm in OrderHelper.quantityModifiers.keys) {
                                if (itm == modifier.id) {
                                    modQuantity = OrderHelper.quantityModifiers.get(itm)!!
                                }
                            }
                            if (modQuantity == 0) {
                                quantity.text = group.minchoicequantity
                                OrderHelper.quantityModifiers.put(
                                    modifier.id,
                                    group.minchoicequantity!!.toInt()
                                )

                                OrderHelper.groupQuantityModifiers.put(
                                    group.id,
                                    group.minchoicequantity!!.toInt()
                                )
                            } else {
                                quantity.text = modQuantity.toString()
                                OrderHelper.quantityModifiers.put(
                                    modifier.id,
                                    modQuantity
                                )
                                groupQuantity += modQuantity
                                OrderHelper.groupQuantityModifiers.put(
                                    group.id,
                                    groupQuantity
                                )
                            }
                        }
                        OrderHelper.checkedCheckBoxWithModifiers.put(modifier.id, group.id)
                    }
//                    OrderHelper.checkedCheckBoxWithModifiers.put(modifier.id, group.id)
                    checkBox.isChecked = true
                }
            } else {
                if (!OrderHelper.checkedRadioGruopWithModifiers.containsKey(group.id)) {
                    if (OrderHelper.fromEdit) {
                        if (modifier.isdefault && modifier.id !in OrderHelper.selectedModifiersIds) {
                            modifier.isdefault = false
                            OrderHelper.selectedModifiersDedault.add(modifier.id)
                        }
                    }
                    if (modifier.isdefault) {
                        modifier.isdefault = false
                        OrderHelper.selectedModifiersDedault.add(modifier.id)
                        checkBox.isChecked = true
                        var groupQuantity = 0
                        for (itm in OrderHelper.groupQuantityModifiers.keys) {
                            if (itm == group.id) {
                                groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                            }
                        }
                        if (groupQuantity == 0) {
                            if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                                quantity.text = group.minchoicequantity
                                OrderHelper.quantityModifiers.put(
                                    modifier.id,
                                    group.minchoicequantity!!.toInt()
                                )
                                OrderHelper.groupQuantityModifiers.put(
                                    group.id,
                                    group.minchoicequantity!!.toInt()
                                )
                            }
                        } else {
                            if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                                groupQuantity += group.minchoicequantity!!.toInt()
                                quantity.text = group.minchoicequantity
                                OrderHelper.quantityModifiers.put(
                                    modifier.id,
                                    group.minchoicequantity!!.toInt()
                                )
                                OrderHelper.groupQuantityModifiers.put(
                                    group.id,
                                    groupQuantity
                                )
                            }
                        }
                        OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                        OrderHelper.selectedModifiersIds.add(modifier.id)
                        listenerSub.onCheckBoxSubModifiersCheck(
                            group,
                            modifier,
                            true
                        )
                    }
                } else {
                    checkBox.isChecked = false
                    quantity.text = "0"
                    OrderHelper.quantityModifiers.put(
                        modifier.id,
                        0
                    )
                    if (modifier.id !in OrderHelper.selectedModifiersIds) {
                        if (OrderHelper.fromEdit) {
                            if (modifier.isdefault && modifier.id !in OrderHelper.selectedModifiersIds) {
                                modifier.isdefault = false
                                OrderHelper.selectedModifiersDedault.add(modifier.id)
                            }
                        }
                        if (modifier.isdefault) {
                            modifier.isdefault = false
                            OrderHelper.selectedModifiersDedault.add(modifier.id)
                            checkBox.isChecked = true
                            var groupQuantity = 0
                            for (itm in OrderHelper.groupQuantityModifiers.keys) {
                                if (itm == group.id) {
                                    groupQuantity =
                                        OrderHelper.groupQuantityModifiers.get(itm)!!
                                }
                            }

                            if (groupQuantity == 0) {
                                if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                                    quantity.text = group.minchoicequantity
                                    OrderHelper.quantityModifiers.put(
                                        modifier.id,
                                        group.minchoicequantity!!.toInt()
                                    )
                                    OrderHelper.groupQuantityModifiers.put(
                                        group.id,
                                        group.minchoicequantity!!.toInt()
                                    )
                                }
                            } else {
                                if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                                    groupQuantity += group.minchoicequantity!!.toInt()
                                    quantity.text = group.minchoicequantity
                                    OrderHelper.quantityModifiers.put(
                                        modifier.id,
                                        group.minchoicequantity!!.toInt()
                                    )
                                    OrderHelper.groupQuantityModifiers.put(
                                        group.id,
                                        groupQuantity
                                    )
                                }
                            }

                            OrderHelper.checkedRadioGruopWithModifiers.put(
                                group.id,
                                modifier.id
                            )
                            OrderHelper.selectedModifiersIds.add(modifier.id)
                            listenerSub.onCheckBoxSubModifiersCheck(
                                group,
                                modifier,
                                true
                            )
                        }

                    } else {
                        checkBox.isChecked = true
                        if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                            quantity.text = group.minchoicequantity
                            OrderHelper.quantityModifiers.put(
                                modifier.id,
                                group.minchoicequantity!!.toInt()
                            )
                        }
                    }
                }
            }
            itemViewLayout.setOnClickListener {
                OrderHelper.fromEdit = false
                var count = 0
                if (modifier.id !in OrderHelper.selectedModifiersIds) {
                    OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                    OrderHelper.checkedCheckBoxWithModifiers.put(modifier.id, group.id)
                    if (group.maxselects != null) {
                        for (keys in OrderHelper.checkedCheckBoxWithModifiers.values) {
                            if (keys == group.id) {
                                count += 1
                            }
                        }
                        if (count > group.maxselects!!.toInt()) {
                            OrderHelper.checkedCheckBoxWithModifiers.remove(modifier.id)
                        } else {
                            checkBox.isChecked = true
                            OrderHelper.selectedModifiersIds.add(modifier.id)
                            listenerSub.onCheckBoxSubModifiersCheck(
                                group,
                                modifier,
                                true
                            )
                            if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                                quantity.text = group.minchoicequantity
                                OrderHelper.quantityModifiers.put(
                                    modifier.id,
                                    group.minchoicequantity!!.toInt()
                                )
                            }
                        }
                    } else {
                        checkBox.isChecked = true
                        OrderHelper.selectedModifiersIds.add(modifier.id)
                        listenerSub.onCheckBoxSubModifiersCheck(
                            group,
                            modifier,
                            true
                        )
                        if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                            quantity.text = group.minchoicequantity
                            OrderHelper.quantityModifiers.put(
                                modifier.id,
                                group.minchoicequantity!!.toInt()
                            )
                        }
                    }
                } else {
                    OrderHelper.checkedCheckBoxWithModifiers.remove(modifier.id)

                    var curQroupQuantity = 0
                    curQroupQuantity = quantity.text.toString().toInt()
                    var groupQuantity = 0
                    for (itm in OrderHelper.groupQuantityModifiers.keys) {
                        if (itm == group.id) {
                            groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                        }
                    }
                    groupQuantity -= curQroupQuantity
                    OrderHelper.groupQuantityModifiers.put(
                        group.id,
                        groupQuantity
                    )


                    checkBox.isChecked = false
                    quantity.text = "0"
                    OrderHelper.quantityModifiers.put(
                        modifier.id,
                        0
                    )
                    listenerSub.onCheckBoxSubModifiersCheck(
                        group,
                        modifier,
                        false
                    )
                    OrderHelper.selectedModifiersIds.remove(modifier.id)
                }
            }

            checkBox.setOnClickListener {
                OrderHelper.fromEdit = false
                var count1 = 0L
                if (modifier.id !in OrderHelper.selectedModifiersIds) {
                    OrderHelper.checkedRadioGruopWithModifiers.put(group.id, modifier.id)
                    OrderHelper.checkedCheckBoxWithModifiers.put(modifier.id, group.id)
                    if (group.maxselects != null) {
                        for (keys in OrderHelper.checkedCheckBoxWithModifiers.values) {
                            if (keys == group.id) {
                                count1 += 1
                            }
                        }
                        if (count1 > group.maxselects!!.toLong()) {
                            checkBox.isChecked = false
                            quantity.text = "0"
                            OrderHelper.quantityModifiers.put(
                                modifier.id,
                                0
                            )
                            count1 = 0
                            OrderHelper.checkedCheckBoxWithModifiers.remove(modifier.id)
                        } else {
                            count1 = 0
                            var groupQuantity = 0
                            for (itm in OrderHelper.groupQuantityModifiers.keys) {
                                if (itm == group.id) {
                                    groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                                }
                            }
                            if (groupQuantity != group.maxaggregatequantity!!.toInt()) {
                                checkBox.isChecked = true
                                OrderHelper.selectedModifiersIds.add(modifier.id)
                                listenerSub.onCheckBoxSubModifiersCheck(
                                    group,
                                    modifier,
                                    true
                                )
                                if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                                    quantity.text = group.minchoicequantity
                                    OrderHelper.quantityModifiers.put(
                                        modifier.id,
                                        group.minchoicequantity!!.toInt()
                                    )
                                    var groupQuantity = 0
                                    for (itm in OrderHelper.groupQuantityModifiers.keys) {
                                        if (itm == group.id) {
                                            groupQuantity =
                                                OrderHelper.groupQuantityModifiers.get(itm)!!
                                        }
                                    }
                                    if (groupQuantity == 0) {
                                        OrderHelper.groupQuantityModifiers.put(
                                            group.id,
                                            group.minchoicequantity!!.toInt()
                                        )
                                    } else {
                                        groupQuantity += group.minchoicequantity!!.toInt()
                                        OrderHelper.groupQuantityModifiers.put(
                                            group.id,
                                            groupQuantity
                                        )
                                    }

                                }
                            } else {
                                checkBox.isChecked = false
                            }
                        }
                    } else {
                        checkBox.isChecked = true
                        OrderHelper.selectedModifiersIds.add(modifier.id)
                        listenerSub.onCheckBoxSubModifiersCheck(
                            group,
                            modifier,
                            true
                        )
                        if (group.minchoicequantity != null && group.minchoicequantity!!.isNotEmpty()) {
                            quantity.text = group.minchoicequantity
                            OrderHelper.quantityModifiers.put(
                                modifier.id,
                                group.minchoicequantity!!.toInt()
                            )

                            var groupQuantity = 0
                            for (itm in OrderHelper.groupQuantityModifiers.keys) {
                                if (itm == group.id) {
                                    groupQuantity =
                                        OrderHelper.groupQuantityModifiers.get(itm)!!
                                }
                            }
                            if (groupQuantity == 0) {
                                OrderHelper.groupQuantityModifiers.put(
                                    group.id,
                                    group.minchoicequantity!!.toInt()
                                )
                            } else {
                                groupQuantity += group.minchoicequantity!!.toInt()
                                OrderHelper.groupQuantityModifiers.put(group.id, groupQuantity)
                            }
                        }
                    }
                } else {
                    OrderHelper.checkedCheckBoxWithModifiers.remove(modifier.id)
                    var curQroupQuantity = 0
                    curQroupQuantity = quantity.text.toString().toInt()
                    var groupQuantity = 0
                    for (itm in OrderHelper.groupQuantityModifiers.keys) {
                        if (itm == group.id) {
                            groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                        }
                    }
                    groupQuantity = groupQuantity - curQroupQuantity


                    OrderHelper.groupQuantityModifiers.put(
                        group.id,
                        groupQuantity
                    )

                    checkBox.isChecked = false
                    quantity.text = "0"
                    OrderHelper.quantityModifiers.put(modifier.id, 0)
                    listenerSub.onCheckBoxSubModifiersCheck(
                        group,
                        modifier,
                        false
                    )
                    OrderHelper.selectedModifiersIds.remove(modifier.id)
                }
            }

            plus.setOnClickListener {
                if (checkBox.isChecked) {
                    var currentQuanity = quantity.text.toString().toInt()
                    currentQuanity += 1

                    var groupQuantity = 0
                    for (itm in OrderHelper.groupQuantityModifiers.keys) {
                        if (itm == group.id) {
                            groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                        }
                    }

                    if (currentQuanity <= group.maxchoicequantity.toInt() && groupQuantity < group.maxaggregatequantity!!.toInt()) {
                        quantity.text = currentQuanity.toString()
                        OrderHelper.quantityModifiers.put(
                            modifier.id,
                            currentQuanity
                        )
                        groupQuantity += 1
                        OrderHelper.groupQuantityModifiers.put(
                            group.id,
                            groupQuantity
                        )
                    } else {
                        currentQuanity -= 1
                    }
                }
            }

            minus.setOnClickListener {
                if (checkBox.isChecked) {
                    var currentQuanity = quantity.text.toString().toInt()
                    currentQuanity -= 1
                    var groupQuantity = 0
                    for (itm in OrderHelper.groupQuantityModifiers.keys) {
                        if (itm == group.id) {
                            groupQuantity = OrderHelper.groupQuantityModifiers.get(itm)!!
                        }
                    }
                    if (currentQuanity >= group.minchoicequantity!!.toInt()) {
                        OrderHelper.quantityModifiers.put(
                            modifier.id,
                            currentQuanity
                        )
                        groupQuantity -= 1
                        OrderHelper.groupQuantityModifiers.put(
                            group.id,
                            groupQuantity
                        )
                        quantity.text = currentQuanity.toString()
                    } else {
                        currentQuanity += 1
                    }
                }
            }


        }
    }
}