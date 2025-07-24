package nl.nlportal.documentenapi.exceptions

class MimeTypeDeniedException(
    message: String,
) : RuntimeException(
        message,
    )