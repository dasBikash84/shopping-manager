package com.dasbikash.book_keeper_repo.exceptions

import java.lang.RuntimeException

abstract class StorageServerException: RuntimeException {

    constructor() {}

    constructor(message: String) : super(message) {}

    constructor(cause: Throwable) : super(cause) {}

}

open class ImageUploadException: StorageServerException {

    constructor() {}

    constructor(message: String) : super(message) {}

    constructor(cause: Throwable) : super(cause) {}

}

class ProductImageUploadException: ImageUploadException {

    constructor() {}

    constructor(message: String) : super(message) {}

    constructor(cause: Throwable) : super(cause) {}

}

class ImageDeletionException: StorageServerException {

    constructor() {}

    constructor(message: String) : super(message) {}

    constructor(cause: Throwable) : super(cause) {}

}

class FileDownloadException: StorageServerException {

    constructor() {}

    constructor(message: String) : super(message) {}

    constructor(cause: Throwable) : super(cause) {}

}




