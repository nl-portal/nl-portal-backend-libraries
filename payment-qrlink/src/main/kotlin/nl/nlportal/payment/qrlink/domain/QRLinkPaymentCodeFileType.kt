package nl.nlportal.payment.qrlink.domain

import com.fasterxml.jackson.annotation.JsonValue

enum class QRLinkPaymentCodeFileType(
    @JsonValue val value: String,
) {
    PNG("png"),
    JPG("jpg"),
    GIF("gif"),
    BMP("bmp"),
    ;

    override fun toString() = this.value
}
