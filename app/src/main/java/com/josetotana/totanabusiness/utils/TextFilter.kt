package com.josetotana.totanabusiness.utils

import java.text.Normalizer
import java.util.*

class TextFilter {
    companion object {
         fun getNormalizeText(text: String): String {

            var textAux = Normalizer.normalize(text, Normalizer.Form.NFD)
             textAux = textAux.replace("[\\p{InCombiningDiacriticalMarks}]", "")
             val re = Regex("[^a-z0-9 ]")
            textAux = re.replace(textAux.toLowerCase(Locale.ROOT), "")

            return textAux
        }

    }

}