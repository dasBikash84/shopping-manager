package com.dasbikash.book_keeper.utils

import java.util.*

class ValidationUtils {

    companion object {

        private val emailValidator =
            Regex("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
        private val bdMobileNumberValidator = Regex("((([+])|([0][0]))88)?01[1-9][0-9]{8}")
        private val mobileNumberValidator = Regex("((([+])|([0][0]))\\d\\d)?\\d+")

        fun validateBdMobileNumber(mobileNumber: CharSequence) = mobileNumber.trim().matches(
            bdMobileNumberValidator
        )

        fun validateEmailAddress(emailAddress: CharSequence) =
            emailAddress.trim().toString().toLowerCase(Locale.getDefault()).matches(emailValidator)


        fun sanitizeNumber(phoneNumber: String): String {
            if (!validateBdMobileNumber(phoneNumber)){return phoneNumber}
            return "+88${phoneNumber.substring(phoneNumber.length-11,phoneNumber.length)}"
        }

        fun validateMobileNumber(mobileNumber: CharSequence) = mobileNumber.trim().matches(
            mobileNumberValidator
        )
    }
}