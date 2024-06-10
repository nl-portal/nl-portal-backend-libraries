package nl.nlportal.zakenapi.client.request

import nl.nlportal.zakenapi.domain.ResultPage

interface PagedRetrieve<O : PagedRetrieve<O, T>, T> : Retrieve<ResultPage<T>> {
    fun page(page: Int): O

    suspend fun retrieveAll(): List<T> {
        val results = mutableListOf<T>()
        do {
            val result = this.retrieve()
            val next = result.next
            results.addAll(result.results)
        } while (next != null)
        return results
    }
}