package com.tts.gueststar.utility

import java.util.regex.Pattern

enum class CardType {

    UNKNOWN,
    VISA("^4$"),
    MASTERCARD("^5$"),
    AMERICAN_EXPRESS("^3$"),
    //DINERS_CLUB("^3(?:0[0-5]\\d|095|6\\d{0,2}|[89]\\d{2})\\d{12,15}$"),
    DISCOVER("^6$");
//    JCB("^(?:2131|1800|35\\d{3})\\d{11}$"),
//    CHINA_UNION_PAY("^62[0-9]{14,17}$");

    private var pattern: Pattern? = null

    constructor() {
        this.pattern = null
    }

    constructor(pattern: String) {
        this.pattern = Pattern.compile(pattern)
    }

    companion object {

        fun detect(cardNumber: String): CardType {
            for (cardType in values()) {
                if (null == cardType.pattern) continue
                if (cardType.pattern!!.matcher(cardNumber).matches()) return cardType
            }

            return UNKNOWN
        }
    }

}