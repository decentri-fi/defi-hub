package io.defitrack

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

object PageUtils {
    fun <T> createPageFromList(list: List<T>, pageable: Pageable): Page<T> {
        val startOfPage = pageable.pageNumber * pageable.pageSize
        if (startOfPage > list.size) {
            return PageImpl(ArrayList(), pageable, 0)
        }
        val endOfPage = Math.min(startOfPage + pageable.pageSize, list.size)
        return PageImpl(list.subList(startOfPage, endOfPage), pageable, list.size.toLong())
    }

}