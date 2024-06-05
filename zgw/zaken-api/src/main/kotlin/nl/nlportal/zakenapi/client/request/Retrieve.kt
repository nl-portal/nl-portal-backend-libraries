package nl.nlportal.zakenapi.client.request

interface Retrieve<T> {
    suspend fun retrieve(): T
}