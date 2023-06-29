package com.example.chatapp

data class Contacts(
    var name: String = "",
    var status: String = "",
    var image: String = ""
) {
    constructor() : this("", "", "")
}
