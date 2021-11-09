package io.codechef.common.utils

import kotlinx.coroutines.*

class ListExtensions {
   companion object {
       suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
           map { async { f(it) } }.awaitAll()
       }

       suspend fun <A, B> Iterable<A>.pflatMap(f: suspend (A) -> List<B>): List<B> = coroutineScope {
           flatMap {
               withContext(Dispatchers.IO) { f(it) }
           }
       }
   }
}