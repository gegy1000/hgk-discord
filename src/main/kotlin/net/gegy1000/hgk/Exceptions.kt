package net.gegy1000.hgk

import net.gegy1000.hgk.model.ErrorModel

class InvalidUsageException : Exception()

class RequestErrorException(errorModel: ErrorModel? = null, cause: Exception? = null) : Exception(errorModel?.error, cause)
